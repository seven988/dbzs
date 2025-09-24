// ScheduleListDialog.java
package com.example.dbzs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.List;

public class ScheduleListDialog extends DialogFragment {

    private OnScheduleListInteractionListener listener;
    private List<NamedShiftSchedule> schedules;

    public interface OnScheduleListInteractionListener {
        void onAddNewSchedule();
        void onEditSchedule(NamedShiftSchedule schedule);
        void onApplySchedule(NamedShiftSchedule schedule);
    }

    // 新增：设置监听器的方法
    public void setOnScheduleListInteractionListener(OnScheduleListInteractionListener listener) {
        this.listener = listener;
    }

    public static ScheduleListDialog newInstance() {
        return new ScheduleListDialog();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        schedules = ScheduleStorage.getSchedules(requireContext());
        try {
            listener = (OnScheduleListInteractionListener) getActivity();
        } catch (ClassCastException e) {
            System.out.println(e.getMessage());
            throw new ClassCastException("Activity must implement OnScheduleListInteractionListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_schedule_list, null);

        ListView listView = view.findViewById(R.id.schedule_list);
        Button addButton = view.findViewById(R.id.btn_add_schedule);

        ScheduleAdapter adapter = new ScheduleAdapter(schedules);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view1, position, id) -> {
            NamedShiftSchedule schedule = schedules.get(position);
            showScheduleOptions(schedule);
        });

        addButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddNewSchedule();
                dismiss();
            }
        });

        builder.setView(view)
                .setTitle("排班计划列表")
                .setNegativeButton("取消", (dialog, which) -> dismiss());

        return builder.create();
    }

    private void showScheduleOptions(NamedShiftSchedule schedule) {
        new AlertDialog.Builder(requireContext())
                .setTitle(schedule.getName())
                .setItems(new String[]{"编辑", "应用到日历"}, (dialog, which) -> {
                    if (which == 0) {
                        listener.onEditSchedule(schedule);
                        dismiss();
                    } else if (which == 1) {
                        listener.onApplySchedule(schedule);
                        dismiss();
                    }
                })
                .show();
    }

    private class ScheduleAdapter extends ArrayAdapter<NamedShiftSchedule> {
        public ScheduleAdapter(List<NamedShiftSchedule> schedules) {
            super(requireContext(), 0, schedules);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_schedule, parent, false);
            }

            NamedShiftSchedule schedule = getItem(position);
            TextView nameTextView = convertView.findViewById(R.id.schedule_name);
            TextView statusTextView = convertView.findViewById(R.id.schedule_status);

            if (schedule != null) {
                nameTextView.setText(schedule.getName());
                statusTextView.setText(schedule.isActive() ? "当前应用" : "未应用");
            }

            return convertView;
        }
    }
}