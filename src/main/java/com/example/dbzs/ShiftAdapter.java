package com.example.dbzs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ShiftAdapter extends RecyclerView.Adapter<ShiftAdapter.ShiftViewHolder> {
    private Context context;
    private List<ShiftModel> shiftList;
    private List<ShiftModel> selectedShifts = new ArrayList<>();

    public ShiftAdapter(Context context, List<ShiftModel> shiftList) {
        this.context = context;
        this.shiftList = shiftList;
    }

    @NonNull
    @Override
    public ShiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.shift_item, parent, false);
        return new ShiftViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShiftViewHolder holder, int position) {
        ShiftModel shift = shiftList.get(position);
        holder.shiftName.setText(shift.getShiftName());
        holder.shiftCheckbox.setChecked(shift.isSelected());

        holder.shiftCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shift.setSelected(holder.shiftCheckbox.isChecked());
                if (shift.isSelected()) {
                    selectedShifts.add(shift);
                } else {
                    selectedShifts.remove(shift);
                }
                if (selectedShifts.size() > 8) {
                    holder.shiftCheckbox.setChecked(false);
                    shift.setSelected(false);
                    selectedShifts.remove(shift);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return shiftList.size();
    }

    public List<ShiftModel> getSelectedShifts() {
        return selectedShifts;
    }

    public static class ShiftViewHolder extends RecyclerView.ViewHolder {
        CheckBox shiftCheckbox;
        TextView shiftName;

        public ShiftViewHolder(@NonNull View view) {
            super(view);
            shiftCheckbox = view.findViewById(R.id.shift_checkbox);
            shiftName = view.findViewById(R.id.shift_name);
        }
    }
}