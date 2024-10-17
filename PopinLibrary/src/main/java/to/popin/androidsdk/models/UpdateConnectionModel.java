package to.popin.androidsdk.models;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;


@Keep
public class UpdateConnectionModel {
    @SerializedName("status")
    public int status;

    @SerializedName("position")
    public int position;

    @SerializedName("call_id")
    public int call_id;

    @SerializedName("message")
    public String message;
}
