package to.popin.androidsdk.common;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import to.popin.androidsdk.models.StatusModel;
import to.popin.androidsdk.models.TalkModel;

public interface APIInterface {


    @FormUrlEncoded
    @POST("v1/seller/call/details")
    Call<TalkModel> getCallAccessToken(
            @Field("call_id") int call_id
    );

    @FormUrlEncoded
    @POST("v1/seller/call/end")
    Call<StatusModel> setCallEnded(
            @Field("call_id") int call_id
    );
}
