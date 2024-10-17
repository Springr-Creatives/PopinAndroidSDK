package to.popin.androidsdk.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

@Keep
public class FastCallModel implements Parcelable {
    @SerializedName("status")
    public int status;

    @SerializedName("id")
    public int id;

    @SerializedName("room")
    public String name;

    @SerializedName("token")
    public String accessToken;

    @SerializedName("websocket")
    public String websocket;


    protected FastCallModel(Parcel in) {
        status = in.readInt();
        id = in.readInt();
        name = in.readString();
        accessToken = in.readString();
        websocket = in.readString();
    }

    public static final Creator<FastCallModel> CREATOR = new Creator<FastCallModel>() {
        @Override
        public FastCallModel createFromParcel(Parcel in) {
            return new FastCallModel(in);
        }

        @Override
        public FastCallModel[] newArray(int size) {
            return new FastCallModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(status);
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(accessToken);
        dest.writeString(websocket);
    }
}
