// NamedShiftSchedule.java
package com.example.dbzs;

import java.io.Serializable;

public class NamedShiftSchedule implements Serializable {
    private String name;
    private ShiftSchedule shiftSchedule;
    private boolean isActive; // 标记当前是否应用

    public NamedShiftSchedule(String name, ShiftSchedule shiftSchedule) {
        this.name = name;
        this.shiftSchedule = shiftSchedule;
        this.isActive = false;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public ShiftSchedule getShiftSchedule() { return shiftSchedule; }
    public void setShiftSchedule(ShiftSchedule shiftSchedule) { this.shiftSchedule = shiftSchedule; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}