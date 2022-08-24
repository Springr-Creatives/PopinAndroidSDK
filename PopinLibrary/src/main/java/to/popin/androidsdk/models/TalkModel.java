package to.popin.androidsdk.models;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

@Keep
public class TalkModel {
    @SerializedName("id")
    public int id;
    @SerializedName("access_token")
    public String access_token;
    @SerializedName("user_id")
    public int user_id;
    @SerializedName("user_name")
    public String user_name;
    @SerializedName("room")
    public String room;
    @SerializedName("artifact")
    public String artifact;
    @SerializedName("status")
    public int status;
    @SerializedName("created_at")
    public long created_at;
}
