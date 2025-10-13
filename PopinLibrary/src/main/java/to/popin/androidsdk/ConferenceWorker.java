package to.popin.androidsdk;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import to.popin.androidsdk.common.APIInterface;
import to.popin.androidsdk.common.AuthInterceptor;
import to.popin.androidsdk.common.Device;
import to.popin.androidsdk.models.CreateConnectionModel;
import to.popin.androidsdk.models.FastCallModel;

public class ConferenceWorker {

    private final Device myPhone;
    private final Context context;
    private APIInterface apiInterface;

    public ConferenceWorker(Context context, Device myPhone) {
        this.myPhone = myPhone;
        this.context = context;

    }


    private void loadApiClient(Device myPhone, String xApiKey, int xToken) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();

                    Request request = original.newBuilder()
                            .header("X-API-KEY", xApiKey)
                            .header("X-TOKEN", String.valueOf(xToken))
                            .method(original.method(), original.body())
                            .build();

                    return chain.proceed(request);
                });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.server_url) + "/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        this.apiInterface = retrofit.create(APIInterface.class);
    }

    public void joinConference(String slug, int agent_id, String xAPIKey, final ConferenceJoinListener conferenceJoinListener) {
        loadApiClient(myPhone, xAPIKey, myPhone.getSeller());
        Call<FastCallModel> call = apiInterface.joinConferenceDetails(agent_id, slug);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<FastCallModel> call, @NonNull Response<FastCallModel> response) {
                Log.e("RES_CODE",">" + response.code());
                if (response.code() == 200) {
                    FastCallModel fastCallModel = response.body();
                    conferenceJoinListener.onConferenceJoined(fastCallModel);
                }
            }

            @Override
            public void onFailure(@NonNull Call<FastCallModel> call, @NonNull Throwable t) {
                Log.e("ERR", t.getMessage());
                conferenceJoinListener.onConferenceJoinFailed();
            }
        });
    }

    interface ConferenceJoinListener {
        void onConferenceJoined(FastCallModel fastCallModel);

        void onConferenceJoinFailed();
    }
}
