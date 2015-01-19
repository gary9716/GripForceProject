package com.ui.customizedwidgets;
 
import java.util.Calendar;
import android.app.DatePickerDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
 
public class DateDisplayPicker extends TextView implements DatePickerDialog.OnDateSetListener{
 
    private Context mContext;
 
    public DateDisplayPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }
 
    public DateDisplayPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setAttributes();
    }
 
    public DateDisplayPicker(Context context) {
        super(context);
        mContext = context;
        setAttributes();
    }
 
    private void setAttributes() {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateDialog();
            }
        });
    }
 
    private void showDateDialog() {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog dp = new DatePickerDialog(mContext, this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dp.show();
    }
 
 
    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear,
            int dayOfMonth) {
    		int currentMonthNum = monthOfYear + 1;
    		if(currentMonthNum < 10) {
    			setText(String.format("0%s/%s/%s", currentMonthNum, dayOfMonth, year));      
    		}
    		else {
    			setText(String.format("%s/%s/%s", currentMonthNum, dayOfMonth, year));
    		}
    }
}
