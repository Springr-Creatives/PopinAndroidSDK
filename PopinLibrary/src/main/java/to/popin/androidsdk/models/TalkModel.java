package to.popin.androidsdk.models;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

@Keep
public class TalkModel {
    @SerializedName("id")
    public int id;
    @SerializedName("token")
    public String access_token;
    @SerializedName("room")
    public String room;
    @SerializedName("status")
    public int status;
}
