package com.example.dbzs;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.CalendarView;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class CustomCalendarView extends CalendarView {

    private Map<Date, String> shiftMap;
    private Paint paint;

    public CustomCalendarView(Context context) {
        super(context);
        init();
    }

    public CustomCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomCalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(20);
    }

    public void setShiftMap(Map<Date, String> shiftMap) {
        this.shiftMap = shiftMap;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (shiftMap != null) {
            for (Map.Entry<Date, String> entry : shiftMap.entrySet()) {
                Date date = entry.getKey();
                String shift = entry.getValue();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                // 这里简单示例，可根据实际布局计算绘制位置
                int x = (day % 7) * 100 + 50;
                int y = (day / 7) * 100 + 50;
                canvas.drawText(shift, x, y, paint);
            }
        }
    }
}