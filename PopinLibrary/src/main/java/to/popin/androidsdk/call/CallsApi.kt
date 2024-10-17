package to.popin.androidsdk.call

import to.popin.androidsdk.models.AgentParticipantModel
import to.popin.androidsdk.models.ProductModel
import to.popin.androidsdk.models.StatusModel
import to.popin.androidsdk.models.TalkModel
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface CallsApi {

    @FormUrlEncoded
    @POST("v1/seller/participant/details")
    suspend fun getParticipantAccessToken(
        @Field("call_participant_id") call_participant_id: Int
    ): Response<TalkModel>

    @GET("v1/seller/clip/products")
    fun getClipProducts(): Call<List<ProductModel>>



    @GET("v1/seller/agents")
    fun getAgents(): Call<List<AgentParticipantModel>>

    @FormUrlEncoded
    @POST("v1/seller/participant")
    fun addParticipant(
        @Field("call_id") call_id: Int,
        @Field("seller_id") seller_id: Int
    ): Call<StatusModel>
}