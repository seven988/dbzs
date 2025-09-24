package com.example.dbzs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ScheduleListDialog.OnScheduleListInteractionListener {

    private ImageButton btnPrev, btnNext;
    private AnimatorSet buttonScaleAnim;

    // 手势检测
    private GestureDetector gestureDetector;
    private static final int SWIPE_THRESHOLD_DP = 50;
    private static final int SWIPE_VELOCITY_THRESHOLD_DP = 100;
    private int swipeThresholdPx;
    private int swipeVelocityThresholdPx;

    private RecyclerView calendarRecyclerView;
    private CalendarAdapter adapter;
    private TextView tvMonth;
    private Calendar currentDisplayMonth;
    private ShiftSchedule currentSchedule;
    private Calendar todayCalendar; // 添加成员变量

    private TextView tvTotalWork, tvEarly, tvMid, tvLate, tvRest;

    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        // 初始化时间组件
        currentDisplayMonth = Calendar.getInstance();
        todayCalendar = Calendar.getInstance(); // 正确初始化
        todayCalendar.set(Calendar.HOUR_OF_DAY, 0);
//        gestureDetector = new GestureDetector(this, new GestureListener());
        // 初始化手势检测参数（基于屏幕密度）
        float density = getResources().getDisplayMetrics().density;
        swipeThresholdPx = (int)(SWIPE_THRESHOLD_DP * density);
        swipeVelocityThresholdPx = (int)(SWIPE_VELOCITY_THRESHOLD_DP * density);
        gestureDetector = new GestureDetector(this, new GestureListener());

        initializeViews();
        setupCalendarRecyclerView();
        setupButtonListeners();

        loadSchedule();
        refreshCalendar();
        initializeStatsViews();
        updateStatistics();
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        setupButtonAnimations();
        // 加载当前激活的排班计划
        loadActiveSchedule();
    }

    private void loadActiveSchedule() {
        NamedShiftSchedule active = ScheduleStorage.getActiveSchedule(this);
        if (active != null) {
            currentSchedule = active.getShiftSchedule();
        } else {
            // 如果没有激活的计划，尝试加载旧的单计划数据
            SharedPreferences prefs = getSharedPreferences("schedule", MODE_PRIVATE);
            String json = prefs.getString("schedule", null);
            if (json != null) {
                Gson gson = new Gson();
                Type type = new TypeToken<ShiftSchedule>(){}.getType();
                currentSchedule = gson.fromJson(json, type);

                // 迁移到新的多计划模式
                if (currentSchedule != null) {
                    NamedShiftSchedule defaultSchedule = new NamedShiftSchedule("默认计划", currentSchedule);
                    defaultSchedule.setActive(true);
                    List<NamedShiftSchedule> schedules = new ArrayList<>();
                    schedules.add(defaultSchedule);
                    ScheduleStorage.saveSchedules(this, schedules);
                    // 清除旧数据
                    prefs.edit().remove("schedule").apply();
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public void onAddNewSchedule() {
        showShiftDialogForNew();
    }

    @Override
    public void onEditSchedule(NamedShiftSchedule schedule) {
        showShiftDialogForEdit(schedule);
    }

    @Override
    public void onApplySchedule(NamedShiftSchedule schedule) {
        applySchedule(schedule);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float velocityX, float velocityY) {
            try {
                float diffX = e2.getX() - e1.getX();

                if (Math.abs(diffX) > swipeThresholdPx &&
                        Math.abs(velocityX) > swipeVelocityThresholdPx) {

                    if (diffX > 0) { // 向右滑动
                        triggerButtonFeedback(btnPrev);
                        changeMonth(-1);
                    } else { // 向左滑动
                        triggerButtonFeedback(btnNext);
                        changeMonth(1);
                    }
                    return true;
                }
            } catch (Exception e) {
                Log.e("SwipeError", "手势检测异常", e);
            }
            return false;
        }

        private void triggerButtonFeedback(ImageButton button) {
            buttonScaleAnim.setTarget(button);
            buttonScaleAnim.start();

            // 添加Ripple效果（API 21+）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                button.drawableHotspotChanged(
                        button.getWidth()/2f,
                        button.getHeight()/2f
                );
                button.performClick();
            }
        }
    }

    private void setupButtonAnimations() {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(null, "scaleX", 1f, 0.9f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(null, "scaleY", 1f, 0.9f);
        scaleDownX.setDuration(100);
        scaleDownY.setDuration(100);

        buttonScaleAnim = new AnimatorSet();
        buttonScaleAnim.playTogether(scaleDownX, scaleDownY);
        buttonScaleAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
//                View view = (View) ((ObjectAnimator) animation.getChildAnimations().get(0)).getTarget();
//                view.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
            }
        });
    }
    private void initializeStatsViews() {
        tvTotalWork = findViewById(R.id.tvTotalWork);
        tvEarly = findViewById(R.id.tvEarly);
        tvMid = findViewById(R.id.tvMid);
        tvLate = findViewById(R.id.tvLate);
        tvRest = findViewById(R.id.tvRest);
    }

    private void updateStatistics() {
        if (currentSchedule == null || adapter == null) return;

        HashMap<String, Integer> stats = new HashMap<>();
        stats.put("早班", 0);
        stats.put("中班", 0);
        stats.put("晚班", 0);
        stats.put("休息", 0);

        Calendar startCal = Calendar.getInstance();
        startCal.setTimeInMillis(currentSchedule.getStartDate());

        // 遍历当前显示月份的所有有效日期
        for (DayItem day : adapter.getValidDays()) {
            Calendar dayCal = day.getCalendar();
            if (dayCal.before(startCal)) continue;

            long diff = dayCal.getTimeInMillis() - startCal.getTimeInMillis();
            int daysDiff = (int) (diff / (24 * 60 * 60 * 1000));
            int shiftIndex = daysDiff % currentSchedule.getShifts().size();
            String shift = currentSchedule.getShifts().get(shiftIndex);

            if (stats.containsKey(shift)) {
                stats.put(shift, stats.get(shift) + 1);
            }
        }

        // 更新UI
        int early = stats.get("早班");
        int mid = stats.get("中班");
        int late = stats.get("晚班");
        int rest = stats.get("休息");

        runOnUiThread(() -> {
            tvTotalWork.setText(String.format("应班：%d", early + mid + late));
            tvEarly.setText(String.format("早班：%d", early));
            tvMid.setText(String.format("中班：%d", mid));
            tvLate.setText(String.format("晚班：%d", late));
            tvRest.setText(String.format("休息：%d", rest));
        });
    }


    private void initializeViews() {
        tvMonth = findViewById(R.id.tvMonth);
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
    }

    private void setupCalendarRecyclerView() {
        calendarRecyclerView.setLayoutManager(new GridLayoutManager(this, 7));
        calendarRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));
        calendarRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        // 解决滑动冲突的关键配置
        calendarRecyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                gestureDetector.onTouchEvent(e); // 将事件传递给手势检测器
                return isChangingMonth; // 允许继续处理滚动事件
            }
        });
    }

    private void setupButtonListeners() {
        // 月份切换按钮
        findViewById(R.id.btnPrev).setOnClickListener(v -> changeMonth(-1));
        findViewById(R.id.btnNext).setOnClickListener(v -> changeMonth(1));

        // 编辑按钮
//        findViewById(R.id.fabEdit).setOnClickListener(v -> showShiftDialog());
        findViewById(R.id.fabEdit).setOnClickListener(v -> showScheduleList());
    }

    //

    private void showScheduleList() {
        ScheduleListDialog dialog = ScheduleListDialog.newInstance();
        dialog.setOnScheduleListInteractionListener(new ScheduleListDialog.OnScheduleListInteractionListener() {
            @Override
            public void onAddNewSchedule() {
                showShiftDialogForNew();
            }

            @Override
            public void onEditSchedule(NamedShiftSchedule schedule) {
                showShiftDialogForEdit(schedule);
            }

            @Override
            public void onApplySchedule(NamedShiftSchedule schedule) {
                applySchedule(schedule);
            }
        });
        dialog.show(getSupportFragmentManager(), "schedule_list");
    }

    private void showShiftDialogForNew() {
        ShiftSelectDialog dialog = ShiftSelectDialog.newInstance(null);
        dialog.setOnScheduleCreatedListener(this::saveNewSchedule);
        dialog.show(getSupportFragmentManager(), "shift_dialog");
    }

    private void showShiftDialogForEdit(NamedShiftSchedule schedule) {
        ShiftSelectDialog dialog = ShiftSelectDialog.newInstanceForEdit(schedule);
        dialog.setOnScheduleCreatedListener(this::updateSchedule);
        dialog.show(getSupportFragmentManager(), "shift_dialog");
    }

    private void saveNewSchedule(NamedShiftSchedule newSchedule) {
        List<NamedShiftSchedule> schedules = ScheduleStorage.getSchedules(this);
        schedules.add(newSchedule);
        ScheduleStorage.saveSchedules(this, schedules);

        // 如果是第一个计划，自动应用
        if (schedules.size() == 1) {
            applySchedule(newSchedule);
        }
    }

    private void updateSchedule(NamedShiftSchedule updatedSchedule) {
        List<NamedShiftSchedule> schedules = ScheduleStorage.getSchedules(this);
        for (int i = 0; i < schedules.size(); i++) {
            if (schedules.get(i).getName().equals(updatedSchedule.getName())) {
                schedules.set(i, updatedSchedule);
                break;
            }
        }
        ScheduleStorage.saveSchedules(this, schedules);

        // 如果更新的是当前激活的计划，刷新界面
        if (updatedSchedule.isActive()) {
            currentSchedule = updatedSchedule.getShiftSchedule();
            refreshCalendar();
            updateStatistics();
        }
    }

    private void applySchedule(NamedShiftSchedule schedule) {
        ScheduleStorage.setActiveSchedule(this, schedule.getName());
        currentSchedule = schedule.getShiftSchedule();
        refreshCalendar();
        updateStatistics();
    }

    // 修改OnScheduleSavedListener接口实现（如果需要）
    /*@Override
    public void onScheduleSaved() {
        // 这个方法可能不再需要，因为我们使用了新的回调机制
        loadActiveSchedule();
        refreshCalendar();
        updateStatistics();
    }
*/
    /*private void showShiftDialog() {
        ShiftSelectDialog dialog = ShiftSelectDialog.newInstance(currentSchedule);
        dialog.setOnScheduleSavedListener(this);
        dialog.show(getSupportFragmentManager(), "shift_dialog");
    }*/

    private boolean isChangingMonth = false;
    private void changeMonth(int delta) {
        if (isChangingMonth) return;
        isChangingMonth = true;
        currentDisplayMonth.add(Calendar.MONTH, delta);
        refreshCalendar();
        updateStatistics();
        calendarRecyclerView.postDelayed(() -> {
            isChangingMonth = false;
        }, 300);
    }

    private void refreshCalendar() {
        updateMonthDisplay();
        generateCalendarData(); // 会自动调用updateAdapter
    }

    private void updateMonthDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月", Locale.getDefault());
        tvMonth.setText(sdf.format(currentDisplayMonth.getTime()));
    }

    private void generateCalendarData() {
        List<DayItem> days = new ArrayList<>();
        Calendar cal = (Calendar) currentDisplayMonth.clone();

        // 设置当月第一天
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // 计算前置空白
        int startOffset = (firstDayOfWeek - cal.getFirstDayOfWeek() + 7) % 7;
        for (int i = 0; i < 7; i++) {
            days.add(new DayItem(0,0,i+1));
        }
        for (int i = 0; i < startOffset; i++) {
            days.add(null);
        }

        // 填充当月日期
        for (int day = 1; day <= daysInMonth; day++) {
            days.add(new DayItem(
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    day
            ));
        }

        // 补充尾部空白
        int totalCells = (int) Math.ceil(days.size() / 7.0) * 7;
        while (days.size() < totalCells) {
            days.add(null);
        }

        updateAdapter(days);
    }

    private void updateAdapter(List<DayItem> newDays) {
        if (adapter == null) {
            adapter = new CalendarAdapter(newDays);
            calendarRecyclerView.setAdapter(adapter);
        } else {
            adapter.updateDays(newDays);
        }
        if (currentSchedule != null) {
            adapter.updateSchedule(currentSchedule);
        }
    }

    private void loadSchedule() {
        SharedPreferences prefs = getSharedPreferences("schedule", MODE_PRIVATE);
        String json = prefs.getString("schedule", null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ShiftSchedule>(){}.getType();
            currentSchedule = gson.fromJson(json, type);
            Log.d("LoadSchedule", "已加载排班开始日期：" +
                    new SimpleDateFormat("yyyy-MM-dd").format(new Date(currentSchedule.getStartDate())));
        }
    }

    /*@Override
    public void onScheduleSaved() {
        loadSchedule();
        if (adapter != null) {
            adapter.updateSchedule(currentSchedule);
        }
        updateStatistics();
    }*/

    // 处理配置变化（如屏幕旋转）
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("currentMonth", currentDisplayMonth.getTimeInMillis());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentDisplayMonth.setTimeInMillis(savedInstanceState.getLong("currentMonth"));
        refreshCalendar();
    }

    public static Calendar getStartOfDay(Calendar calendar) {
        Calendar newCal = (Calendar) calendar.clone();
        newCal.set(Calendar.HOUR_OF_DAY, 0);
        newCal.set(Calendar.MINUTE, 0);
        newCal.set(Calendar.SECOND, 0);
        newCal.set(Calendar.MILLISECOND, 0);
        return newCal;
    }

    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
                && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }
}
