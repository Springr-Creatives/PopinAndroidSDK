package to.popin.androidsdk.models;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

@Keep
public class UserModel {
    @SerializedName("status")
    public int status;

    @SerializedName("token")
    public String token;
}
