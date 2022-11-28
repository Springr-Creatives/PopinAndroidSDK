package to.popin.androidsdk;

import java.util.List;

import to.popin.androidsdk.models.ScheduleSlotsModel;

public interface PopinScheduleListener {
    void onAvailableScheduleLoaded(List<ScheduleSlotsModel.ScheduleSlot> scheduleSlots);
    void onScheduleLoadError();
}
