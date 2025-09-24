package com.example.dbzs;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {
    private List<DayItem> days;
    private ShiftSchedule currentSchedule;
    private final Calendar todayCalendar;

    public CalendarAdapter(List<DayItem> days) {
        this.days = new ArrayList<>(days);
        this.todayCalendar = Calendar.getInstance();
        todayCalendar.set(Calendar.HOUR_OF_DAY, 0);
        todayCalendar.set(Calendar.MINUTE, 0);
        todayCalendar.set(Calendar.SECOND, 0);
        todayCalendar.set(Calendar.MILLISECOND, 0);
    }

    public void updateSchedule(ShiftSchedule schedule) {
        this.currentSchedule = schedule;
        notifyDataSetChanged();
    }

    public void updateDays(List<DayItem> newDays) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new CalendarDiffCallback(days, newDays));
        days.clear();
        days.addAll(newDays);
        result.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        DayItem dayItem = days.get(position);
        if (dayItem == null) {
            bindEmptyView(holder);
            return;
        }
        if (dayItem.getYear()==0){
            bindWeekView(holder,dayItem);
            return;
        }

        bindDateView(holder, dayItem);
        if (currentSchedule != null) {
            Calendar dayCal = dayItem.getCalendar();
            Calendar startCal = Calendar.getInstance();
            startCal.setTimeInMillis(currentSchedule.getStartDate());

            // 精确到日的比较
            startCal.set(Calendar.HOUR_OF_DAY, 0);
            dayCal.set(Calendar.HOUR_OF_DAY, 0);

            // 仅处理起始日期及之后的日期
            if (!dayCal.before(startCal)) {
                long diff = dayCal.getTimeInMillis() - startCal.getTimeInMillis();
                int daysDiff = (int) (diff / (24 * 60 * 60 * 1000));

                // 修正模运算
                int shiftIndex = daysDiff % currentSchedule.getShifts().size();
                shiftIndex = shiftIndex < 0 ? shiftIndex + currentSchedule.getShifts().size() : shiftIndex;

                holder.tvShift.setText(currentSchedule.getShifts().get(shiftIndex));
            } else {
                holder.tvShift.setText(""); // 起始日期前不显示班次
            }
        }
    }

    private void bindEmptyView(CalendarViewHolder holder) {
        holder.tvWeekday.setVisibility(View.INVISIBLE);
        holder.tvDate.setVisibility(View.INVISIBLE);
        holder.tvLunar.setVisibility(View.INVISIBLE);
        holder.tvShift.setVisibility(View.GONE);
        holder.itemView.setBackgroundColor(Color.TRANSPARENT);
    }

    private void bindWeekView(CalendarViewHolder holder, DayItem dayItem) {
        holder.tvWeekday.setVisibility(View.INVISIBLE);
        holder.tvDate.setVisibility(View.VISIBLE);
        holder.tvLunar.setVisibility(View.INVISIBLE);
        holder.tvShift.setVisibility(View.GONE);
        holder.tvDate.setText(getWeek(dayItem.getDay()));
        holder.itemView.setBackgroundColor(Color.TRANSPARENT);
    }

    private String getWeek(int i){
        String result = "";
        switch (i) {
            case 1:
                result = "日";
                break;
            case 2:
                result = "一";
                break;
            case 3:
                result = "二";
                break;
            case 4:
                result = "三";
                break;
            case 5:
                result = "四";
                break;
            case 6:
                result = "五";
                break;
            case 7:
                result = "六";
                break;
        }
        return result;
    }
    private void bindDateView(CalendarViewHolder holder, DayItem dayItem) {
        // 显示基础日期信息
        holder.tvDate.setVisibility(View.VISIBLE);
        holder.tvShift.setVisibility(View.VISIBLE);
        holder.tvLunar.setVisibility(View.VISIBLE);
        holder.tvWeekday.setVisibility(View.INVISIBLE);
        //不在日历格中显示星期
//        holder.tvWeekday.setText(dayItem.getWeekdayName(dayItem.getYear(),dayItem.getMonth(),dayItem.getDay(),holder.itemView.getContext()));
        holder.tvDate.setText(String.valueOf(dayItem.getDay()));
        holder.tvLunar.setText(dayItem.getLunarDate());
        // 高亮今日日期
        if (isToday(dayItem)) {
            holder.itemView.setBackgroundResource(R.drawable.bg_today);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        // 显示班次信息
        if (currentSchedule != null) {
            Calendar dayCalendar = dayItem.getCalendar();
            Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTimeInMillis(currentSchedule.getStartDate());

            // 计算天数差
            long diff = dayCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();
            int daysDiff = (int) (diff / (24 * 60 * 60 * 1000));

            if (daysDiff >= 0) {
                int shiftIndex = daysDiff % currentSchedule.getShifts().size();
                String shift = currentSchedule.getShifts().get(shiftIndex);
                holder.tvShift.setText(shift);
                applyShiftStyle(holder, shift);
            } else {
                holder.tvShift.setText("");
                holder.tvShift.setTextColor(Color.BLACK);
            }
        }
    }

    private boolean isToday(DayItem dayItem) {
        return dayItem.getYear() == todayCalendar.get(Calendar.YEAR) &&
                dayItem.getMonth() == todayCalendar.get(Calendar.MONTH) &&
                dayItem.getDay() == todayCalendar.get(Calendar.DAY_OF_MONTH);
    }

    private void applyShiftStyle(CalendarViewHolder holder, String shift) {
        switch (shift) {
            case "早班":
                holder.tvShift.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.early_shift));
                break;
            case "中班":
                holder.tvShift.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.mid_shift));
                break;
            case "晚班":
                holder.tvShift.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.late_shift));
                break;
            case "休息":
                holder.tvShift.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.day_off));
                break;
            default:
                holder.tvShift.setTextColor(Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class CalendarViewHolder extends RecyclerView.ViewHolder {
        TextView tvWeekday, tvDate, tvShift,tvLunar;

        CalendarViewHolder(View itemView) {
            super(itemView);
            tvWeekday = itemView.findViewById(R.id.tvWeekday);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvShift = itemView.findViewById(R.id.tvShift);
            tvLunar = itemView.findViewById(R.id.tvLunar);
        }
    }

    private static class CalendarDiffCallback extends DiffUtil.Callback {
        private final List<DayItem> oldList, newList;

        CalendarDiffCallback(List<DayItem> oldList, List<DayItem> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() { return oldList.size(); }

        @Override
        public int getNewListSize() { return newList.size(); }

        @Override
        public boolean areItemsTheSame(int oldPos, int newPos) {
            DayItem oldItem = oldList.get(oldPos);
            DayItem newItem = newList.get(newPos);
            if (oldItem == null || newItem == null) return false;
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(int oldPos, int newPos) {
            DayItem oldItem = oldList.get(oldPos);
            DayItem newItem = newList.get(newPos);
            return Objects.equals(oldItem, newItem);
        }
    }
    public List<DayItem> getValidDays() {
        List<DayItem> validDays = new ArrayList<>();
        for (DayItem day : days) {
            if (day != null) validDays.add(day);
        }
        return validDays;
    }
}