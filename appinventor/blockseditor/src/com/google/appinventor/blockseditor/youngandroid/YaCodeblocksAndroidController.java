// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.blockseditor.youngandroid;

import com.google.appinventor.common.youngandroid.YaHttpServerConstants;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IClientChangeListener;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.Client;
import com.android.ddmlib.DdmPreferences;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.SyncService;
import com.android.ddmlib.TimeoutException;

import openblocks.yacodeblocks.AndroidController;
import openblocks.yacodeblocks.AndroidControllerException;
import openblocks.yacodeblocks.ExternalStorageException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to handle callbacks from codeblocks for Android-specific communication
 *
 * @author kerr@google.com (Debby Wallach)
 */
// @VisibleForTesting
public final class YaCodeblocksAndroidController implements AndroidController,
    IDeviceChangeListener, IClientChangeListener {

  private static final int ADB_SHELL_COMMAND_TIMEOUT_MILLIS = 5 * 60 * 1000;
  private static final int DDM_TIMEOUT_MILLIS = 5 * 60 * 1000;

  /*
   * Inner class to manage state and functionality related to the "starter
   * application" (a.ka., the REPL).
   */
  private final class StarterAppManager {
    private IDevice selectedDevice = null;
    private SyncService syncService = null;  // sync service for selected device
    // true iff we tried to start the starter app since selectedDevice connected
    private boolean startedSinceLastConnected = false;
    private boolean appRunning = false; // true if we have detected that the app is
                                        // running since the last time we connected
    private int appPid = 0; // if appRunning is true, this is its pid

    private final String apkFilePath;  // location of apk on client filesystem
    private final String appName = YaHttpServerConstants.STARTER_PHONEAPP_NAME + ".apk";
    // The following package and class names must match what is used in the define-repl-form
    // expression in appinventor/aiphoneapp/src/com/google/appinventor/aiphoneapp/Screen1.yail
    private final String packageName = "com.google.appinventor.aiphoneapp";
    private final String className = "Screen1";

    StarterAppManager(String apkFilePath) {
      this.apkFilePath = apkFilePath;
      if (DEBUG) {
        System.out.println("filepath: " + apkFilePath + " packageName: " + packageName);
      }
    }

    // Forget that app started
    void reset(IDevice device) {
      if (device != selectedDevice) return;
      setStartedSinceLastConnected(false);
    }

    // Change the selected device. If we previously had a selected device, reset
    // first.
    void selectDevice(IDevice device) {
      if (selectedDevice != null) {
        killApp();
        reset(selectedDevice);
      }
      selectedDevice = device;
      if (syncService != null) {
        syncService.close();
      }
      syncService = null;
    }

    IDevice getSelectedDevice() {
      return selectedDevice;
    }

    SyncService getSyncService() {
      if (syncService == null) {
        try {
          syncService = selectedDevice.getSyncService();
        } catch (IOException e) {
          System.out.println("Warning: can't get sync service for selected device: " +
              "IOException: " + e.getMessage());
        } catch (TimeoutException e) {
          System.out.println("Warning: can't get sync service for selected device: " +
              "TimeoutException: " + e.getMessage());
        } catch (AdbCommandRejectedException e) {
          System.out.println("Warning: can't get sync service for selected device: " +
              "AdbCommandRejectedException: " + e.getMessage());
        }
      }
      return syncService;
    }

    /*
     *  Returns true if the app is running based on info in device. As a side-effect,
     *  records app running state and fires workspace event if the state changed
     */
    boolean detectIsAppRunning(IDevice device) {
      if (device != selectedDevice) return false;
      if (!startedSinceLastConnected) return false;
      Client starterClient = device.getClient(packageName);
      if (starterClient != null) {
        if (!appRunning) {
          System.out.println("Repl app is running but we didn't detect it. " +
              "Setting starterAppRunning to true");
          detectedAppRunning(starterClient);
        }
        return true;
      } else {
        if (appRunning) {
          for (Client client: device.getClients()) {
            if (client.getClientData().getPid() == appPid) {
              // its still running but we didn't find it by name for some reason
              return true;
            }
          }
          detectedAppNotRunning();
        }
        return false;
      }
    }

    // Call detectIsAppRunning for currently selected device (if any)
    boolean detectIsAppRunning() {
      if (selectedDevice == null) return false;
      return detectIsAppRunning(selectedDevice);
    }

    /*
     *  Returns true if the app is running based on info in client. As a side-effect,
     *  records app running state.
     */
    boolean detectIsAppRunning(Client client) {
      if (client.getDevice() != selectedDevice) return false;
      if (!startedSinceLastConnected) return false;
      if (packageName.equals(client.getClientData().getClientDescription())) {
        detectedAppRunning(client);
        return true;
      }
      return false;
    }

    void syncAndInstallApp() throws AndroidControllerException, ExternalStorageException {
      syncApplication(apkFilePath, appName, packageName, true);
    }

    /**
     * Starts the application on the device/emulator.
     *
     */
    void startApp() throws AndroidControllerException {
      StringBuilder cmd = new StringBuilder();
      cmd.append("am start");
      cmd.append(" -n ");

      cmd.append(packageName);
      cmd.append("/.");
      cmd.append(className.replaceAll("\\$", "\\\\\\$"));

      MultiLineReceiver receiver = new MultiLineReceiver() {
        @Override
        public void processNewLines(String[] lines) {
        }
        @Override
        public boolean isCancelled() {
          return false;
        }
      };

      if (DEBUG) {
        System.out.println("*** Executing command: " + cmd.toString());
      }
      try {
        selectedDevice.executeShellCommand(cmd.toString(), receiver);
        // TODO(markf): We could respond differently based on the particular type of exception
      } catch (Exception e) {
        throw new AndroidControllerException("executeShellCommand failed: '"
                                             + cmd.toString() + "' " + e.getMessage());
      }
      setStartedSinceLastConnected(true);
    }

    void killApp() {
      killApplication(packageName);
    }

    private void detectedAppRunning(Client client) {
      appRunning = true;
      appPid = client.getClientData().getPid();
      System.out.println("Repl app is now running");
   }

    private void detectedAppNotRunning() {
      appRunning = false;
      appPid = 0;
      System.out.println("Repl app is now not running");
    }


    private void setStartedSinceLastConnected(boolean started) {
      startedSinceLastConnected = started;
      if (started) {
        appRunning = false; // reset until we detect it is really running
        appPid = 0;
      }
    }
  }

  private static final boolean DEBUG = false;

  private static final String MOUNT_COMMAND = "mount";

  private StarterAppManager starterAppManager;

  // Where to put an application on the device during install.
  final String installPath = "/data/local/tmp/";

  private volatile String adbLocation;
  private volatile boolean adbInitDone;

  private volatile Map<String, IDevice> devices;
  private volatile DeviceConnectionListener listener = null;

  public YaCodeblocksAndroidController(String apkFilePath) {
    devices = new HashMap<String, IDevice>();
    DdmPreferences.setTimeOut(DDM_TIMEOUT_MILLIS);
    starterAppManager = new StarterAppManager(apkFilePath);
  }

  public void setDeviceListener(DeviceConnectionListener listener) {
    this.listener = listener;
  }

  public boolean androidInitializeCommunicationBridge(String adbLocation) {
    this.adbLocation = adbLocation;
    return startAdb();
  }

  // IDeviceChangeListener interface
  @Override
  public void deviceConnected(IDevice device) {
    String serialnum = device.getSerialNumber();
    System.out.println("Device connected: " + serialnum);
    // Only connect to a device if it's the only one plugged in.
    if (!devices.containsKey(serialnum)) {
      devices.put(serialnum, device);
    }
    if (listener != null) {
      listener.deviceConnected(serialnum);
    }
  }

  @Override
  public void deviceDisconnected(IDevice device) {
    String serialnum = device.getSerialNumber();
    System.out.println("Device disconnected: " + serialnum);
    if (listener != null) {
      listener.deviceDisconnected(serialnum);
    }
    if (devices.containsKey(serialnum)) {
      devices.remove(serialnum);
    }
    starterAppManager.reset(device);
  }
  /*
   * Documentation stolen from ddmlib source code:
   * @param changeMask the bit mask describing the changed properties. It can contain
   *    any of the following values:
   *    <ul>
   *    <li>{@link IDevice#CHANGE_BUILD_INFO}</li>
   *    <li>{@link IDevice#CHANGE_STATE}</li>
   *    <li>{@link IDevice#CHANGE_CLIENT_LIST}</li>
   *    </ul>
   */
  @Override
  public void deviceChanged(IDevice device, int changeMask) {
    if ((changeMask & IDevice.CHANGE_CLIENT_LIST) != 0) {
      starterAppManager.detectIsAppRunning(device);
    }
  }

  /*
   * Documentation stolen from ddmlib source code:
   * @param changeMask the bit mask describing the changed properties. It can contain
   *    any of the following values:
   *    <ul>
   *    <li>{@link Client#CHANGE_INFO}</li>
   *    <li>{@link Client#CHANGE_DEBUGGER_INTEREST}</li>
   *    <li>{@link Client#CHANGE_THREAD_MODE}</li>
   *    <li>{@link Client#CHANGE_THREAD_DATA}</li>
   *    <li>{@link Client#CHANGE_HEAP_MODE}</li>
   *    <li>{@link Client#CHANGE_HEAP_DATA}</li>
   *    <li>{@link Client#CHANGE_NATIVE_HEAP_DATA}</li>
   *    </ul>
   */
  public void clientChanged(Client client, int changeMask) {
    if ((changeMask & Client.CHANGE_NAME) != 0) {
      starterAppManager.detectIsAppRunning(client);
    }
  }

  public boolean androidIsStarterApplicationRunning() {
    return starterAppManager.detectIsAppRunning();
  }

  public void androidKillStarterApplication() {
    starterAppManager.killApp();
  }

  public void androidKillSpecificApplication(String packageName) {
    killApplication(packageName);
  }

  // Interface to sync the application to the device and install the application
  public void androidSyncAndInstallStarterApplication()
      throws AndroidControllerException, ExternalStorageException {
    starterAppManager.syncAndInstallApp();
  }

  public void androidSyncAndInstallSpecificApplication(String apkFilePath, String appName,
      String packageName) throws AndroidControllerException, ExternalStorageException {
    if (DEBUG) {
      System.out.println("Trying to Sync and Install application: " + appName);
    }
    syncApplication(apkFilePath, appName + ".apk", packageName, false);
  }

  // Interface to create a port forwarding between a local and a remote port.
  public boolean androidForwardTcpPort(int localPort, int remotePort) {
    IDevice device = starterAppManager.getSelectedDevice();
    if (device == null) return false;
    try {
      device.createForward(localPort, remotePort);
      return true;
      // TODO(markf): We could respond differently based on the particular type of exception
    } catch (Exception e) {
      return false;
    }
  }

  // Interface to start the application running.
  public void androidStartStarterApplication() throws AndroidControllerException {
    starterAppManager.startApp();
  }

  // Must be called before the code exits
  public void androidCleanUpBeforeExit() {
    if (adbInitDone) {
      AndroidDebugBridge.terminate();
      adbInitDone = false;
    }
  }

  private static boolean sameDevice(IDevice d1, IDevice d2) {
    return (d1 != null && d2 != null)
        ? d1.getSerialNumber() == d2.getSerialNumber()
        : d1 == d2;
  }

  /**
   * Starts up the Android Debug Bridge.
   */
  private boolean startAdb() {
    // TODO(kerr): Is there any case where we'd need to call
    // AndroidDebugBridge.restart() for a hard reset on state?
    if (!adbInitDone) {
      AndroidDebugBridge.init(true);
      adbInitDone = androidRestartBridge();
    }
    return adbInitDone;
  }

  public boolean androidRestartBridge() {
    if (AndroidDebugBridge.createBridge(adbLocation, false) == null) return false;
    AndroidDebugBridge.addDeviceChangeListener(this);
    AndroidDebugBridge.addClientChangeListener(this);
    return true;
  }

  /**
   * Uploads the application to the device/emulator and then calls
   * {@link #installApplication} to install the application.
   */
  private void syncApplication(String apkFilePath, String appName, String packageName,
      boolean uninstallRequired) throws AndroidControllerException, ExternalStorageException {
    // Upload the app to the device.
    final String remoteInstallPath = installPath + appName;
    pushFileToDevice(apkFilePath, remoteInstallPath);

    // Install the app.
    installApplication(remoteInstallPath, packageName, uninstallRequired);
  }

  public void pushFileToDevice(String fileToPush, String remotePath)
      throws AndroidControllerException, ExternalStorageException {
    IDevice device = starterAppManager.getSelectedDevice();
    SyncService sync = starterAppManager.getSyncService();
    if (device == null) {
      throw new AndroidControllerException("No selected device");
    }
    if (sync == null) {
      throw new AndroidControllerException("Unable to get SyncService for device");
    }
    try {
      sync.pushFile(fileToPush, remotePath, SyncService.getNullProgressMonitor());
      // TODO(markf): We could respond differently based on the particular type of exception
    } catch (Exception e) {
      if (externalStorageNotMounted()) {
        throw new ExternalStorageException(
            "It appears that the device cannot access its external storage.\n" +
            "Check that the device is not in USB Mass Storage mode.\n" +
            "Please Restart the Phone App after fixing the device.");
      } else {
        throw new AndroidControllerException("Failed to upload file '" + fileToPush
                                             + "' to device. Details: " + e.getMessage());
      }
    }
  }

  // Returns true iff we can detect that the external storage is *not* mounted
  // on the phone.
  private boolean externalStorageNotMounted() {
    IDevice device = starterAppManager.getSelectedDevice();
    if (device != null) {
      final boolean externalStorageMounted[] = { false };
      // TODO(sharon): in later version of the library might be able to use
      // device.getMountPoint(IDevice.MNT_EXTERNAL_STORAGE) instead of
      // hard coding "sdcard"
      final String externalMountPoint = "sdcard";
      try {
        device.executeShellCommand(MOUNT_COMMAND, new MultiLineReceiver() {
          @Override
          public void processNewLines(String[] lines) {
            // If processNewLines is called multiple times check that we
            // haven't already found the external storage mount point.
            if (externalStorageMounted[0]) return;  // nothing more to do
            for (String line: lines) {
              if (line.contains(externalMountPoint)) {
                externalStorageMounted[0] = true;
                return;
              }
            }
          }
          @Override
          public boolean isCancelled() {
            return externalStorageMounted[0];
          }
        });
        // TODO(markf): We could respond differently based on the particular type of exception
      } catch (Exception e) {
        System.out.println("Attempt to check external storage on device gave Exception "
            + e.getMessage());
        return false;
      }
      // if we get here we correctly processed the output of the mount command
      // so trust whether we found the external storage via that command.
      return !externalStorageMounted[0];
    }
    return false;
  }

  /**
   * Kill the application if it is running.
   */
  private void killApplication(String packageName) {
    IDevice device = starterAppManager.getSelectedDevice();
    Client application = device.getClient(packageName);
    if (DEBUG) {
      System.out.println("killing app if necessary...");
    }
    if (application != null) {
      if (DEBUG) {
        System.out.println("...is necessary...");
      }
      application.kill();
    }
  }

  /**
   * Installs the application on the device/emulator.
   */
  private void installApplication(String remotePath, String packageName, boolean uninstallRequired)
      throws AndroidControllerException {
    class ShellReceiver extends MultiLineReceiver {
      public boolean success = false;
      public StringBuilder result = new StringBuilder();

      @Override
      public void processNewLines(String[] lines) {
        for (String line : lines) {
          result.append(line).append('\n');
          if (line.startsWith("Success")) {
            success = true;
          }
        }
      }
      @Override
      public boolean isCancelled() {
        return false;
      }
    }

    IDevice device = starterAppManager.getSelectedDevice();
    if (device == null) {
      throw new AndroidControllerException("No selected device");
    }

    // Execute the uninstall (if required) followed by the install commands.
    ShellReceiver receiverUninstall = new ShellReceiver();
    ShellReceiver receiverInstall = new ShellReceiver();
    String shellCmdStr = null;
    try {
      // TODO(sharon): The "-k" option to uninstall should cause the app data to be kept.
      // In particular, we want to keep around data stored in shared preferences like OAuth
      // access tokens and TinyDB data. We have to be careful about using this
      // for the REPL app.
      if (uninstallRequired) {
        // TODO(sharon): it is possible that the general setting for DdmPreferences
        // would take care of the timeout on executeShellCommand too. If so,
        // we can remove this separate explicit timeout.
        shellCmdStr = "pm uninstall \"" + packageName + "\"";
        device.executeShellCommand(shellCmdStr, receiverUninstall,
                                   ADB_SHELL_COMMAND_TIMEOUT_MILLIS);
        shellCmdStr = "pm install \"" + remotePath + "\"";
        device.executeShellCommand(shellCmdStr, receiverInstall,
                                   ADB_SHELL_COMMAND_TIMEOUT_MILLIS);
      } else {
        shellCmdStr = "pm install -r \"" + remotePath + "\"";
        device.executeShellCommand(shellCmdStr, receiverInstall,
                                   ADB_SHELL_COMMAND_TIMEOUT_MILLIS);
      }
      // TODO(markf): We could respond differently based on the particular type of exception
    } catch (Exception e) {
      if (DEBUG) {
        e.printStackTrace();
      }
      throw new AndroidControllerException("executeShellCommand failed: '" +
                                           shellCmdStr + "'" + e.getMessage());
    }
    if (uninstallRequired) {
      if (receiverUninstall.success) {
        if (DEBUG) {
          System.out.println("Uninstalled previous version of package " + packageName);
        }
      }
    }
    if (!receiverInstall.success) {
      if (DEBUG) {
        System.out.println("installation was not successful!");
      }
      /* TODO(kerr): I used to see the following failure not uncommonly:
       * Failure [INSTALL_FAILED_INSUFFICIENT_STORAGE], but then it went
       * away.  If it resurfaces, we'll have to deal with it.
       */
      throw new AndroidControllerException(receiverInstall.result.toString());
    }
  }

  public void selectDevice(String device) throws AndroidControllerException {
    if (devices.containsKey(device)) {
      starterAppManager.selectDevice(devices.get(device));
    } else {
      throw new AndroidControllerException("Device is not connected");
    }
  }

  public String getSelectedDevice() {
    IDevice selectedDevice = starterAppManager.getSelectedDevice();
    if (selectedDevice != null) {
      return selectedDevice.getSerialNumber();
    } else {
      return null;
    }
  }
}
