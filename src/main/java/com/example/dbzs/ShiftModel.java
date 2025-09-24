package com.example.dbzs;

public class ShiftModel {
    private String shiftName;
    private boolean isSelected;

    public ShiftModel(String shiftName) {
        this.shiftName = shiftName;
        this.isSelected = false;
    }

    public String getShiftName() {
        return shiftName;
    }

    public void setShiftName(String shiftName) {
        this.shiftName = shiftName;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
