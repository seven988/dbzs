package com.example.dbzs;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ShiftSelectionActivity extends AppCompatActivity {
    private List<String> selectedShifts = new ArrayList<>();

    private ShiftSchedule currentSchedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shift_selection);

        Button btnSave = findViewById(R.id.btnSave);
        TextView tvCount = findViewById(R.id.tvCount);
        SharedPreferences prefs = getSharedPreferences("schedule", MODE_PRIVATE);
        String json = prefs.getString("schedule", null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ShiftSchedule>(){}.getType();
            currentSchedule = gson.fromJson(json, type);
        }
        // 初始化按钮点击监听
        int[] buttonIds = {R.id.btnEarly, R.id.btnMid, R.id.btnLate, R.id.btnOff};
        String[] shifts = {"早班", "中班", "晚班", "休息"};

        for (int i = 0; i < buttonIds.length; i++) {
            Button btn = findViewById(buttonIds[i]);
            String shift = shifts[i];
            btn.setOnClickListener(v -> {
                if (selectedShifts.size() < 8) {
                    selectedShifts.add(shift);
                    tvCount.setText("已选：" + currentSchedule.getShifts().size()/*selectedShifts.size()*/ + "/8");
                    if (selectedShifts.size() == 8) {
                        btnSave.setEnabled(true);
                    }
                }
            });
        }

        btnSave.setOnClickListener(v -> saveSchedule());
    }

    private void saveSchedule() {
        Calendar startCal = Calendar.getInstance();
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        // ... 其他时间字段设为0

        ShiftSchedule schedule = new ShiftSchedule(
                startCal.getTimeInMillis(),
                new ArrayList<>(selectedShifts)
        );

        SharedPreferences.Editor editor = getSharedPreferences("schedule", MODE_PRIVATE).edit();
        editor.putString("schedule", new Gson().toJson(schedule));
        editor.apply();

        finish();
    }
}