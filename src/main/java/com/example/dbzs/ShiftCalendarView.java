package com.example.dbzs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.CalendarView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ShiftCalendarView extends CalendarView {

    private List<String> shifts;
    private Date startDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private Paint paint;

    public ShiftCalendarView(Context context) {
        super(context);
        init();
    }

    public ShiftCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ShiftCalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(20);
        paint.setTextAlign(Paint.Align.CENTER);
    }

    public void setShiftInfo(List<String> shifts, Date startDate) {
        this.shifts = shifts;
        this.startDate = startDate;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (shifts == null || startDate == null) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);
        for (int i = 0; i < shifts.size(); i++) {
            calendar.setTime(startDate);
            calendar.add(Calendar.DAY_OF_YEAR, i);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);

            // 这里简单假设每个日期的绘制位置，实际需要根据 CalendarView 的布局来调整
            int x = getWidth() / 7 * ((day - 1) % 7 + 1);
            int y = getHeight() / 6 * ((day - 1) / 7 + 1) + 30;

            String shift = shifts.get(i % shifts.size());
            canvas.drawText(shift, x, y, paint);
        }
    }
}