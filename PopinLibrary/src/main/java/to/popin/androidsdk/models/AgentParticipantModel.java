package to.popin.androidsdk.models;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

@Keep
public class AgentParticipantModel {
    @SerializedName("id")
    public int id;

    @SerializedName("name")
    public String name;

    @SerializedName("is_tele_caller")
    public int is_tele_caller;
}
