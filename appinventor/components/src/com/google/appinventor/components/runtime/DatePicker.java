package com.google.appinventor.components.runtime;

import android.view.View;
import android.widget.DatePicker.OnDateChangedListener;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.PropertyTypeConstants;

public class DatePicker extends AndroidViewComponent implements OnDateChangedListener {

    private final android.widget.DatePicker view;

    protected DatePicker(ComponentContainer container) {
        super(container);
        view = new android.widget.DatePicker(container.$context());

        // Listen to focus changes
        view.init(view.getYear(), view.getMonth(), view.getDayOfMonth(), this);

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
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "")
    @SimpleProperty
    public void Year(int year) {
        view.init(year, view.getMonth(), view.getDayOfMonth(), this);
        view.invalidate();
    }

    /**
     * DayOfMonth property setter method.
     * 
     * @param day
     *            the day of the month
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "")
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
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "")
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

    @Override
    public void onDateChanged(android.widget.DatePicker view, int year, int month, int day) {
        Changed();
    }

}
