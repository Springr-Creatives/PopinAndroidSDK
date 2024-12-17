package to.popin.androidsdk.common;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import to.popin.androidsdk.models.CreateConnectionModel;
import to.popin.androidsdk.models.UpdateConnectionModel;
import to.popin.androidsdk.models.FastCallModel;
import to.popin.androidsdk.models.ScheduleSlotsModel;
import to.popin.androidsdk.models.StatusModel;
import to.popin.androidsdk.models.UserModel;

public interface APIInterface {


    @FormUrlEncoded
    @POST("v1/website/user/login")
    Call<UserModel> registerUser(
            @Field("seller_id") int seller_id,
            @Field("is_mobile") int is_mobile,
            @Field("device") String device,
            @Field("name") String name,
            @Field("mobile") String mobile
    );

    @FormUrlEncoded
    @POST("v1/user/connect")
    Call<CreateConnectionModel> startConnection(
            @Field("seller_id") int seller_id,
            @Field("session") String session
    );

    @GET("v1/user/schedule")
    Call<ScheduleSlotsModel> getScheduleSlots();

    @FormUrlEncoded
    @POST("v1/user/schedule")
    Call<StatusModel> setSchedule(
            @Field("seller_id") int seller_id,
            @Field("time") String time
    );

    @GET("v1/user/call/{id}")
    Call<FastCallModel> getCallDetails(
            @Path("id") int call_id
    );


    @FormUrlEncoded
    @POST("v1/user/connect/update")
    Call<UpdateConnectionModel> getCallUpdate(
            @Field("call_queue_id") int call_queue_id
    );

    @FormUrlEncoded
    @POST("/v1/user/screen/close")
    Call<StatusModel> closeConnection(
            @Field("session") String session
    );
}
