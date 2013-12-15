// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.UsesLibraries;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;

/**
 * Components that can contain other components need to implement this
 * interface.
 *
 */
@UsesLibraries(libraries = "android-support-v13.jar")
public interface ComponentContainer {
  /**
   * Returns the activity context (which can be retrieved from the root
   * container - aka the form).
   *
   * @return  activity context
   */
  FragmentActivity $context();

  /**
   * Returns the form that ultimately contains this container.
   *
   * @return  form
   */
  Form $form();

  /**
   * Adds a component to a container.
   *
   * <p/>After this method is finished executing, the given component's view
   * must have LayoutParams, even if the component cannot be added to the
   * container until later.
   *
   * @param component  component associated with view
   */
  void $add(AndroidViewComponent component);

  void setChildWidth(AndroidViewComponent component, int width);

  void setChildHeight(AndroidViewComponent component, int height);
}
