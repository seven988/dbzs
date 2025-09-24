package com.example.dbzs;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ShiftSelectDialog extends DialogFragment {

    private static final String ARG_SCHEDULE = "schedule";
    private ShiftSchedule existingSchedule;

    private List<String> selectedShifts = new ArrayList<>();
    private OnScheduleSavedListener listener;

    private Calendar selectedDate = Calendar.getInstance();
    private TextView tvSelectedDate;

    private LinearLayout selectedShiftsContainer;

    public TextView getTvSelectedDate() {
        return tvSelectedDate;
    }

    public void setTvSelectedDate(TextView tvSelectedDate) {
        this.tvSelectedDate = tvSelectedDate;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public List<String> getShifts() {
        return shifts;
    }

    public void setShifts(List<String> shifts) {
        this.shifts = shifts;
    }

    // 新增静态工厂方法
    public static ShiftSelectDialog newInstance(ShiftSchedule schedule) {
        ShiftSelectDialog dialog = new ShiftSelectDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SCHEDULE, schedule);
        dialog.setArguments(args);
        return dialog;
    }

    private long startDate;  // 精确到天的开始时间戳
    private List<String> shifts;

    // 添加日期格式化方法
    public String getFormattedDate(Context context) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",
                Locale.getDefault());
        return sdf.format(new Date(startDate));
    }

    // 定义回调接口
    public interface OnScheduleSavedListener {
        void onScheduleSaved();
    }

    public void setOnScheduleSavedListener(OnScheduleSavedListener listener) {
        this.listener = listener;
    }

   /* @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            existingSchedule = (ShiftSchedule) getArguments().getSerializable(ARG_SCHEDULE);
        }
    }*/

    private ShiftSchedule currentSchedule;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        if (existingSchedule != null) {
            selectedDate.setTimeInMillis(existingSchedule.getStartDate());
        } else {
            selectedDate.setTimeInMillis(System.currentTimeMillis());
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_shift_select, null);

        // 初始化UI组件
        TextView tvCount = view.findViewById(R.id.tvCount);
        TextView yxbc = view.findViewById(R.id.yxbc);
        Button btnSave = view.findViewById(R.id.btnSave);
        LinearLayout buttonContainer = view.findViewById(R.id.buttonContainer);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        Button btnSelectDate = view.findViewById(R.id.btnSelectDate);
        selectedShiftsContainer = view.findViewById(R.id.selectedShiftsContainer);
        // 动态添加班次按钮
        String[] shifts = {"早班", "中班", "晚班", "休息"};
        SharedPreferences prefs = getContext().getSharedPreferences("schedule", MODE_PRIVATE);
        String json = prefs.getString("schedule", null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ShiftSchedule>(){}.getType();
            currentSchedule = gson.fromJson(json, type);
        }
        for (String shift : shifts) {
            Button button = new Button(requireContext());
            button.setText(shift);
            button.setOnClickListener(v -> {
                if (selectedShifts.size() < 8) {
                    selectedShifts.add(shift);
                    tvCount.setText("已选：" + selectedShifts.size() + "/8");
                    yxbc.setText(BcJxUtils.joinText(selectedShifts));

                    if (selectedShifts.size() == 8) {
                        btnSave.setEnabled(true);
                    }
                }
            });
            button.setBackgroundColor(Color.parseColor(ColorUtils.getShiftColorResId(shift)));
//            button.setOnClickListener(v -> handleShiftSelection(shift));
            buttonContainer.addView(button);
        }
        if (currentSchedule!=null){
            tvCount.setText("已选：" + currentSchedule.getShifts().size() + "/8");
        }

        if (currentSchedule!=null){
            yxbc.setText(BcJxUtils.joinText(currentSchedule.getShifts()) );
        }

        etScheduleName = view.findViewById(R.id.et_schedule_name);

        // 如果是编辑模式，设置现有名称
        if (isEditing && originalName != null) {
            etScheduleName.setText(originalName);
            builder.setTitle("编辑排班计划");
        } else {
            builder.setTitle("添加新排班计划");
        }
        btnSave.setOnClickListener(v -> saveSchedule());

        builder.setView(view)
                .setTitle("选择8个班次")
                .setNegativeButton("取消", (dialog, which) -> dismiss());
//        btnSelectDate.setOnClickListener(v -> showDatePicker());
        updateDateDisplay();

        // 日期选择点击监听
        btnSelectDate.setOnClickListener(v -> showDatePicker());
//        setupShiftButtons(selectedShiftsContainer);
        return builder.create();
    }

    private void handleShiftSelection(String shift) {
        if (selectedShifts.size() < 8) {
            selectedShifts.add(shift);
//            updateSelectedShiftsDisplay();
            updateCountDisplay();
        }
    }

    private void setupShiftButtons(LinearLayout container) {
//        String[] shifts = {"早班", "中班", "晚班", "休息"};

        for (String shift : selectedShifts) {
            Button btn = new Button(requireContext(), null, 0, R.style.ShiftButtonStyle);

            // 设置按钮文本和颜色
            btn.setText(shift);
            btn.setBackgroundColor(Color.parseColor(ColorUtils.getShiftColorResId(shift)));

            // 设置点击监听
            btn.setOnClickListener(v -> handleShiftSelection(shift));

            container.addView(btn);
        }
    }

    /*private void updateSelectedShiftsDisplay() {
//        selectedShiftsContainer.removeAllViews();

        for (int i = 0; i < selectedShifts.size(); i++) {
            String shift = selectedShifts.get(i);

            Button btn = new Button(requireContext(), null, 0, R.style.SelectedShiftButtonStyle);
            btn.setText(shift + " ×");
            btn.setTag(i); // 保存索引位置

            // 设置颜色样式
            btn.setBackgroundColor(Color.parseColor(ColorUtils.getShiftColorResId(selectedShifts.get(i))));

            // 设置删除功能
            btn.setOnClickListener(v -> {
                int position = (int) v.getTag();
                selectedShifts.remove(position);
                updateSelectedShiftsDisplay();
                updateCountDisplay();
            });

            // 添加布局参数
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(2, 0, 2, 0);
            btn.setLayoutParams(params);

            selectedShiftsContainer.addView(btn);
        }
    }*/
    private void updateSelectedShiftsDisplay() {
        selectedShiftsContainer.removeAllViews();
        Context context = requireContext();

        for (int i = 0; i < selectedShifts.size(); i++) {
            // 添加班次文字
            TextView tvShift = new TextView(context);
            String shift = selectedShifts.get(i);

            tvShift.setText(shift);
            tvShift.setTextColor(Color.parseColor(ColorUtils.getShiftColorResId(shift)));
            tvShift.setTypeface(null, Typeface.BOLD);
            tvShift.setTextAppearance(R.style.ShiftTextStyle);

            // 长按删除功能
            tvShift.setOnLongClickListener(v -> {
                selectedShifts.remove(shift);
                updateSelectedShiftsDisplay();
                updateCountDisplay();
                return true;
            });

            selectedShiftsContainer.addView(tvShift);

            // 添加分隔符（最后一个不添加）
            if (i < selectedShifts.size() - 1) {
                View divider = new View(context);
                divider.setLayoutParams(new LinearLayout.LayoutParams(
                        getResources().getDimensionPixelSize(R.dimen.divider_width),
                        getResources().getDimensionPixelSize(R.dimen.divider_height)
                ));
                divider.setBackgroundColor(Color.red(1));
                selectedShiftsContainer.addView(divider);
            }
        }
    }

    // 添加接口
    public interface OnScheduleCreatedListener {
        void onScheduleCreated(NamedShiftSchedule schedule);
    }

    // 添加成员变量
    private EditText etScheduleName;
    private OnScheduleCreatedListener createListener;
    private boolean isEditing = false;
    private String originalName;

    // 新增：定义参数常量
    private static final String ARG_NAME = "schedule_name";
    private static final String ARG_IS_EDITING = "is_editing";

    // 新增：设置监听器的方法
    public void setOnScheduleCreatedListener(OnScheduleCreatedListener listener) {
        this.createListener = listener;
    }

    // 修改newInstance方法支持编辑模式
    public static ShiftSelectDialog newInstanceForEdit(NamedShiftSchedule schedule) {
        ShiftSelectDialog dialog = new ShiftSelectDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SCHEDULE, schedule.getShiftSchedule());
        args.putString(ARG_NAME, schedule.getName());
        args.putBoolean(ARG_IS_EDITING, true);
        dialog.setArguments(args);
        return dialog;
    }
    // 在onCreate中获取参数
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            existingSchedule = (ShiftSchedule) getArguments().getSerializable(ARG_SCHEDULE);
            originalName = getArguments().getString(ARG_NAME);
            isEditing = getArguments().getBoolean(ARG_IS_EDITING, false);
        }
    }


    private void updateCountDisplay() {
        TextView tvCount = getView().findViewById(R.id.tvCount);
        tvCount.setText(String.format(Locale.CHINA, "已选：%d/8", selectedShifts.size()));

        Button btnSave = getView().findViewById(R.id.btnSave);
        btnSave.setEnabled(selectedShifts.size() == 8);
    }

    private void showDatePicker() {
        // 使用Material Design日期选择器
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder
                .datePicker()
                .setTitleText("选择开始日期")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            selectedDate.setTimeInMillis(selection);
            selectedDate.set(Calendar.HOUR_OF_DAY, 0);
            selectedDate.set(Calendar.MINUTE, 0);
            selectedDate.set(Calendar.SECOND, 0);
            selectedDate.set(Calendar.MILLISECOND, 0);
            updateDateDisplay();
        });

        datePicker.show(getChildFragmentManager(), "DATE_PICKER");
    }

    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        tvSelectedDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void saveSchedule() {
        String scheduleName = etScheduleName.getText().toString().trim();
        if (scheduleName.isEmpty()) {
            etScheduleName.setError("请输入计划名称");
            return;
        }

        Calendar startCal = Calendar.getInstance();
        startCal.setTimeInMillis(selectedDate.getTimeInMillis());
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);

        ShiftSchedule schedule = new ShiftSchedule(
                startCal.getTimeInMillis(),
                new ArrayList<>(selectedShifts)
        );

        NamedShiftSchedule namedSchedule = new NamedShiftSchedule(scheduleName, schedule);

        // 如果是新创建的计划，默认不激活；如果是编辑的计划，保持原有激活状态
        if (isEditing) {
            List<NamedShiftSchedule> schedules = ScheduleStorage.getSchedules(requireContext());
            for (NamedShiftSchedule s : schedules) {
                if (s.getName().equals(originalName)) {
                    namedSchedule.setActive(s.isActive());
                    break;
                }
            }
        }

        if (createListener != null) {
            createListener.onScheduleCreated(namedSchedule);
        }

        dismiss();
    }
}