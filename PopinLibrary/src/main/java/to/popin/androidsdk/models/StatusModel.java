package to.popin.androidsdk.models;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;


@Keep
public class StatusModel {
    @SerializedName("status")
    public int status;

    @SerializedName("message")
    public String message;
}
