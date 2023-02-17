package com.seers.servicecheck.graph_new;

import java.util.Date;

public class StepCountData {
    private Date dateTime;
    private int stepCount;

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }
}
