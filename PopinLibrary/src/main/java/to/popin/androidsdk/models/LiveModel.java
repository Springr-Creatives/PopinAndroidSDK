package to.popin.androidsdk.models;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


@Keep
public class LiveModel implements Serializable {
    @SerializedName("status")
    public int status;
    @SerializedName("id")
    public int id;
    @SerializedName("room")
    public String room;
    @SerializedName("agent_name")
    public String agent_name;
    @SerializedName("token")
    public String token;
}
