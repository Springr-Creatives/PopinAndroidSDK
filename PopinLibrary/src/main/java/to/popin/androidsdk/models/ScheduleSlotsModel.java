package to.popin.androidsdk.models;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.util.List;

@Keep
public class ScheduleSlotsModel {

    @SerializedName("availability")
    public List<ScheduleSlot> scheduleSlots;

    @Keep
    public class ScheduleSlot {
        @SerializedName("date")
        public String date;
        @SerializedName("slots")
        public List<String> timeSlots;
    }

}
