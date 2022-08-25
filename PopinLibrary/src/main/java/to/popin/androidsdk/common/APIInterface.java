package to.popin.androidsdk.common;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import to.popin.androidsdk.models.StatusModel;
import to.popin.androidsdk.models.TalkModel;
import to.popin.androidsdk.models.UserModel;

public interface APIInterface {


    @FormUrlEncoded
    @POST("v1/website/user/login")
    Call<UserModel> registerUser(
            @Field("seller_id") int seller_id,
            @Field("is_mobile") int is_mobile,
            @Field("device") String device
    );

    @FormUrlEncoded
    @POST("v1/user/connect")
    Call<StatusModel> startConnection(
            @Field("seller_id") int seller_id
    );

    @FormUrlEncoded
    @POST("v1/user/call")
    Call<TalkModel> createCall(
            @Field("seller_id") int seller_id
    );

    @FormUrlEncoded
    @POST("v1/user/call/end")
    Call<StatusModel> setCallEnded(
            @Field("call_id") int call_id
    );
}
