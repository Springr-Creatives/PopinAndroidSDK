package to.popin.androidsdk.schedule;

import java.util.List;

import to.popin.androidsdk.PopinCreateScheduleListener;
import to.popin.androidsdk.PopinScheduleListener;
import to.popin.androidsdk.models.ScheduleSlotsModel;

public class SchedulePresenter {
    private final ScheduleInteractor scheduleInteractor;

    public SchedulePresenter(ScheduleInteractor scheduleInteractor) {
        this.scheduleInteractor = scheduleInteractor;
    }

    public void getScheduleSlots(PopinScheduleListener popinScheduleListener) {
        scheduleInteractor.loadScheduleSlots(popinScheduleListener);
    }

    public void createSchedule(String time, PopinCreateScheduleListener popinCreateScheduleListener) {
        scheduleInteractor.createSchedule(time,popinCreateScheduleListener);
    }

}
