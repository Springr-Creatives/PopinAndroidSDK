package to.popin.androidsdk.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ScheduleSlotsModel {

    @SerializedName("availability")
    public List<ScheduleSlot> scheduleSlots;

    public class ScheduleSlot {
        @SerializedName("date")
        public String date;
        @SerializedName("slots")
        public List<String> timeSlots;
    }

}
