package com.google.appinventor.components.runtime;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

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

/**
 * Date picker with the ability to detect focus change (mousing on or off of
 * it), and date changes.
 * 
 */
@DesignerComponent(version = YaVersion.DATEPICKER_COMPONENT_VERSION, description = "Datepicker that raises an event when the user clicks on it. There are many properties "
        + "affecting its appearance that can be set in the Designer or Blocks Editor.", category = ComponentCategory.USERINTERFACE)
@SimpleObject
@UsesLibraries(libraries = "android-support-v13.jar")
public final class DatePicker extends AndroidViewComponent implements OnFocusChangeListener {
    private boolean isEmbedded;

    private EditText mEdit;

    final Calendar c = Calendar.getInstance();

    int year, month, day;

    protected DatePicker(ComponentContainer container) {
        super(container);

        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

        mEdit.setKeyListener(null);
        mEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDate();
            }
        });

        // Adds the component to its designated container
        container.$add(this);

        Width(LENGTH_FILL_PARENT);
        Height(LENGTH_FILL_PARENT);
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
        return mEdit;
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
        return mEdit.isEnabled();
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
        mEdit.setEnabled(enabled);
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
        this.year = year;
    }

    @SimpleProperty
    public int Year() {
        return year;
    }

    /**
     * Sets whether the component is embedded or not
     * 
     * @param isEmbedded
     *            if the component is embedded
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "false")
    @SimpleProperty
    public void IsEmbedded(boolean isEmbedded) {
        this.isEmbedded = isEmbedded;
    }

    @SimpleProperty
    public boolean IsEmbedded() {
        return isEmbedded;
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
        this.day = day;
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
        this.month = month;
    }

    /**
     * Returns the day of month represented by the DatePicker
     * 
     * @return the selected day of month.
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public int Day() {
        return this.day;
    }

    /**
     * Returns the month represented by the DatePicker
     * 
     * @return the month (0-11)
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public int Month() {
        return this.month;
    }

    // OnFocusChangeListener implementation
    @Override
    public void onFocusChange(View previouslyFocused, boolean gainFocus) {
        if (gainFocus) {
            GotFocus();
            selectDate();
        } else {
            LostFocus();
        }
    }

    public void populateSetDate(int year, int month, int day) {
        mEdit.setText(month + "/" + day + "/" + year);
    }

    public void selectDate() {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(container.$form().getSupportFragmentManager(), "DatePicker");
    }

    private class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(android.widget.DatePicker view, int year, int month, int day) {
            DatePicker.this.year = year;
            DatePicker.this.month = month;
            DatePicker.this.day = day;

            populateSetDate(year, month + 1, day);
            Changed();
        }
    }

}
