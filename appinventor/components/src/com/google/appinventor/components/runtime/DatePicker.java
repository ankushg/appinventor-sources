package com.google.appinventor.components.runtime;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.DatePicker.OnDateChangedListener;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.OnInitializeListener;

/**
 * Date picker with the ability to detect initialization, focus change (mousing
 * on or off of it), and date changes.
 * 
 */
@DesignerComponent(version = YaVersion.DATEPICKER_COMPONENT_VERSION, description =
                    "Datepicker that raises an event when the user clicks on it. There are many properties " +
                    "affecting its appearance that can be set in the Designer or Blocks Editor.", 
                    category = ComponentCategory.USERINTERFACE)
@SimpleObject
@UsesLibraries(libraries = "android-support-v13.jar") 
public final class DatePicker extends AndroidViewComponent implements OnResumeListener, OnInitializeListener,
        OnPauseListener, OnDateChangedListener, OnFocusChangeListener {
    private final Activity context;
    private final Form form;
    private static final String TAG = "DatePicker";

    // Layout
    // We create this LinerLayout and add our DatePickerDialogFragment in it.
    private android.widget.LinearLayout view;

    // translates App Inventor alignment codes to Android gravity
    // private final AlignmentUtil alignmentSetter;
    private final String DATEPICKER_FRAGMENT_TAG;
    private DatePickerFragment mFragment = DatePickerFragment.newInstance();
    private Bundle savedInstanceState;

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
    private final Handler androidUIHandler = new Handler();

    protected DatePicker(ComponentContainer container) {
        super(container);
        Log.i(TAG, "In the constructor of DatePicker");
        context = container.$context();
        form = container.$form();
        savedInstanceState = form.getOnCreateBundle();
        Log.i(TAG, "savedInstanceState in GM: " + savedInstanceState);

        // try raw mapView with in the fragmment
        view = new android.widget.LinearLayout(context);
        view.setId(generateViewId());

        DATEPICKER_FRAGMENT_TAG = "map_" + System.currentTimeMillis();
        Log.i(TAG, "map_tag:" + DATEPICKER_FRAGMENT_TAG);

        mFragment = (DatePickerFragment) form.getSupportFragmentManager().findFragmentByTag(DATEPICKER_FRAGMENT_TAG);

        // We only create a fragment if it doesn't already exist.
        if (mFragment == null) {

            Log.i(TAG, "mMapFragment is null.");

            // To programmatically add the map, we first create a
            // SupportMapFragment.
            mFragment = DatePickerFragment.newInstance();

            // mMapFragment = new SomeFragment();
            FragmentTransaction fragmentTransaction = form.getSupportFragmentManager().beginTransaction();
            Log.i(TAG, "here before adding fragment");
            // try to use replace to see if we solve the issue
            fragmentTransaction.replace(view.getId(), mFragment, DATEPICKER_FRAGMENT_TAG);

            fragmentTransaction.commit();
        }

        // Adds the component to its designated container
        container.$add(this);

        Width(LENGTH_FILL_PARENT);
        Height(LENGTH_FILL_PARENT);

        form.registerForOnInitialize(this);
        form.registerForOnResume(this);
        form.registerForOnResume(this);
        form.registerForOnPause(this);
    }

    // Currently this doesn't work
    @Override
    @SimpleProperty()
    public void Width(int width) {
        if (width == LENGTH_PREFERRED) {
            width = LENGTH_FILL_PARENT;
        }
        super.Width(width);
    }

    @Override
    @SimpleProperty()
    public void Height(int height) {
        if (height == LENGTH_PREFERRED) {
            height = LENGTH_FILL_PARENT;
        }
        super.Height(height);
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void onResume() {
        Log.i(TAG, "in onResume...DatePicker redraw");
    }

    @Override
    public void onInitialize() {
    }

    private void prepareFragmentView() {
        mFragment = DatePickerFragment.newInstance();

        androidUIHandler.post(new Runnable() {
            public void run() {
                boolean dispatchEventNow = false;
                if (mFragment != null) {
                    dispatchEventNow = true;
                }
                if (dispatchEventNow) {

                    // Then we add it using a FragmentTransaction.
                    // add fragment to the view
                    FragmentTransaction fragmentTransaction = form.getSupportFragmentManager().beginTransaction();

                    // fragmentTransaction.add(view.getLayoutManager().getId(),
                    fragmentTransaction.add(view.getId(), mFragment, DATEPICKER_FRAGMENT_TAG);

                    fragmentTransaction.commit();
                } else {
                    // Try again later.
                    androidUIHandler.post(this);
                }
            }
        });
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        Log.i(TAG, "in onPause...DatePicker");
    }

    /**
     * Default Changed event handler.
     */
    @SimpleEvent
    public void Changed() {
        EventDispatcher.dispatchEvent(this, "Changed");
    }

    /**
     * Default GotFocus event handler.
     */
    @SimpleEvent
    public void GotFocus() {
        EventDispatcher.dispatchEvent(this, "GotFocus");
    }

    /**
     * Default LostFocus event handler.
     */
    @SimpleEvent
    public void LostFocus() {
        EventDispatcher.dispatchEvent(this, "LostFocus");
    }

    /**
     * Returns true if the DatePicker is active and clickable.
     * 
     * @return {@code true} indicates enabled, {@code false} disabled
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public boolean Enabled() {
        return view.isEnabled();
    }

    /**
     * Specifies whether the DatePicker should be active and clickable.
     * 
     * @param enabled
     *            {@code true} for enabled, {@code false} disabled
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    @SimpleProperty
    public void Enabled(boolean enabled) {
        view.setEnabled(enabled);
    }

    /**
     * Year property setter method.
     * 
     * @param year
     *            the year
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "1970")
    @SimpleProperty
    public void Year(int year) {
        mFragment.year = year;
    }

    @SimpleProperty
    public int Year() {
        return mFragment.year;
    }

    /**
     * DayOfMonth property setter method.
     * 
     * @param day
     *            the day of the month
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "0")
    @SimpleProperty
    public void Day(int day) {
        mFragment.day = day;
    }

    /**
     * Month property setter method.
     * 
     * @param month
     *            the month of the year
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "1")
    @SimpleProperty
    public void Month(int month) {
        mFragment.month = month;
    }

    /**
     * Returns the day of month represented by the DatePicker
     * 
     * @return the selected day of month.
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public int Day() {
        return mFragment.day;
    }

    /**
     * Returns the month represented by the DatePicker
     * 
     * @return the month (0-11)
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public int Month() {
        return mFragment.month;
    }

    // onDateChanged implementation
    @Override
    public void onDateChanged(android.widget.DatePicker view, int year, int month, int day) {
        Changed();
    }

    // OnFocusChangeListener implementation
    @Override
    public void onFocusChange(View previouslyFocused, boolean gainFocus) {
        if (gainFocus) {
            GotFocus();
        } else {
            LostFocus();
        }
    }

    /**
     * Generate a value suitable for use in . This value will not collide with
     * ID values generated at build time by aapt for R.id.
     * 
     * @return a generated ID value
     */
    private static int generateViewId() {
        for (;;) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range
            // under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF)
                newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    private static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        
        static DatePickerFragment newInstance() {
            return new DatePickerFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Create an instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(android.widget.DatePicker view, int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;

        }
    }

}
