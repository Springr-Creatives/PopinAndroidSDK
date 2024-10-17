package to.popin.androidsdk.schedule;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import to.popin.androidsdk.PopinCreateScheduleListener;
import to.popin.androidsdk.PopinScheduleListener;
import to.popin.androidsdk.R;
import to.popin.androidsdk.common.APIInterface;
import to.popin.androidsdk.common.AuthInterceptor;
import to.popin.androidsdk.common.Device;
import to.popin.androidsdk.models.ScheduleSlotsModel;
import to.popin.androidsdk.models.StatusModel;

public class ScheduleInteractor {
    private final Device myPhone;
    private final APIInterface apiInterface;

    public ScheduleInteractor(Context context, Device myPhone) {
        this.myPhone = myPhone;
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder().addInterceptor(new AuthInterceptor(myPhone));
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.server_url) + "/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        this.apiInterface = retrofit.create(APIInterface.class);
    }

    public void loadScheduleSlots(final PopinScheduleListener scheduleListener) {
        Call<ScheduleSlotsModel> call = apiInterface.getScheduleSlots();
        call.enqueue(new Callback<ScheduleSlotsModel>() {
            @Override
            public void onResponse(@NonNull Call<ScheduleSlotsModel> call, @NonNull Response<ScheduleSlotsModel> response) {
                if (response.code() == 200) {
                    ScheduleSlotsModel scheduleSlotsModel = response.body();
                    if (scheduleSlotsModel != null) {
                        scheduleListener.onAvailableScheduleLoaded(scheduleSlotsModel.scheduleSlots);
                        return;
                    }
                }
                scheduleListener.onScheduleLoadError();
            }

            @Override
            public void onFailure(@NonNull Call<ScheduleSlotsModel> call, @NonNull Throwable t) {
                Log.e("ERR", t.getMessage());
                scheduleListener.onScheduleLoadError();
            }
        });
    }

    public void createSchedule(String time, final PopinCreateScheduleListener popinCreateScheduleListener) {
        Call<StatusModel> call = apiInterface.setSchedule(myPhone.getSeller(), time);
        call.enqueue(new Callback<StatusModel>() {
            @Override
            public void onResponse(@NonNull Call<StatusModel> call, @NonNull Response<StatusModel> response) {
                if (response.code() == 200) {
                    StatusModel statusModel = response.body();
                    if (statusModel != null) {
                        popinCreateScheduleListener.onScheduleCreated();
                        return;
                    }
                }
                popinCreateScheduleListener.onScheduleLoadError();
            }

            @Override
            public void onFailure(@NonNull Call<StatusModel> call, @NonNull Throwable t) {
                Log.e("ERR", t.getMessage());
                popinCreateScheduleListener.onScheduleLoadError();
            }
        });
    }

}
