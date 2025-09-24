package com.example.dbzs;

import java.io.Serializable;
import java.util.List;

public class ShiftSchedule implements Serializable {
    private long startDate;
    private List<String> shifts;

    public ShiftSchedule(long startDate, List<String> shifts) {
        this.startDate = startDate;
        this.shifts = shifts;
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
    // Getters and setters
}