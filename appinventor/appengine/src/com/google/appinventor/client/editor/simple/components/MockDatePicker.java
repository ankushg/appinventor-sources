// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Mock DatePicker component.
 *
 * @author ankush@mit.edu (Ankush Gupta)
 */
public final class MockDatePicker extends MockVisibleComponent {

  /**
   * Component type name.
   */
  public static final String TYPE = "DatePicker";

  // Large icon image for use in designer.  Smaller version is in the palette.
  private final Image largeImage = new Image(images.datepickerbig());

  /**
   * Creates a new MockWebViewer component.
   *
   * @param editor  editor of source file the component belongs to
   */
  public MockDatePicker(SimpleEditor editor) {
    super(editor, TYPE, images.webviewer());

    // Initialize mock DatePickerUI
    SimplePanel datePickerWidget = new SimplePanel();
    datePickerWidget.setStylePrimaryName("ode-SimpleMockContainer");
    datePickerWidget.addStyleDependentName("centerContents");
    datePickerWidget.setWidget(largeImage);
    initComponent(datePickerWidget);
  }

  // If these are not here, then we don't see the icon as it's
  // being dragged from the pelette
  @Override
  public int getPreferredWidth() {
    return largeImage.getWidth();
  }

  @Override
  public int getPreferredHeight() {
    return largeImage.getHeight();
  }


  // override the width and height hints, so that automatic will in fact be fill-parent
  @Override
  int getWidthHint() {
    int widthHint = super.getWidthHint();
    if (widthHint == LENGTH_PREFERRED) {
      widthHint = LENGTH_FILL_PARENT;
    }
    return widthHint;
  }

  @Override int getHeightHint() {
    int heightHint = super.getHeightHint();
    if (heightHint == LENGTH_PREFERRED) {
      heightHint = LENGTH_FILL_PARENT;
    }
    return heightHint;
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);
  }
}
