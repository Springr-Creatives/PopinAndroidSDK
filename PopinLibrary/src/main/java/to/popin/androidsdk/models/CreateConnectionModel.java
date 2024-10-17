package to.popin.androidsdk.models;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

@Keep
public class CreateConnectionModel {
    @SerializedName("status")
    public int status;

    @SerializedName("position")
    public int position;

    @SerializedName("call_queue_id")
    public int call_queue_id;

}
