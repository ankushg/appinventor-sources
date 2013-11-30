package com.google.appinventor.components.runtime;

import java.util.Calendar;

import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.DatePicker.OnDateChangedListener;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

/**
 * Date picker with the ability to detect initialization, focus change (mousing
 * on or off of it), and date changes.
 * 
 */
@DesignerComponent(version = YaVersion.DATEPICKER_COMPONENT_VERSION, description = "Datepicker that raises an event when the user clicks on it. "
        + "There are many properties affecting its appearance that can be set in " + "the Designer or Blocks Editor.", category = ComponentCategory.USERINTERFACE)
@SimpleObject
public final class DatePicker extends AndroidViewComponent implements OnDateChangedListener, OnFocusChangeListener {
    private final android.widget.DatePicker view;

    protected DatePicker(ComponentContainer container) {
        super(container);
        view = new android.widget.DatePicker(container.$context());
        
        // Listen to focus changes
        Calendar calendar = Calendar.getInstance();
        view.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), this);
        
        // Adds the component to its designated container
        container.$add(this);
        Enabled(true);
    }

    @Override
    public View getView() {
        return view;
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
        view.updateDate(year, view.getMonth(), view.getDayOfMonth());
    }
    
    @SimpleProperty
    public int Year() {
          return view.getYear();
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
        view.init(view.getYear(), view.getMonth(), day, this);
        view.invalidate();
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
        view.init(view.getYear(), month, view.getDayOfMonth(), this);
        view.invalidate();
    }

    /**
     * Returns the day of month represented by the DatePicker
     * 
     * @return the selected day of month.
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public int Day() {
        return view.getDayOfMonth();
    }
    
    /**
     * Returns the month represented by the DatePicker
     * 
     * @return the month (0-11)
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public int Month() {
        return view.getMonth();
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

}
