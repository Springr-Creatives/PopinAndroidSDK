package to.popin.androidsdk;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import to.popin.androidsdk.common.APIInterface;
import to.popin.androidsdk.common.AuthInterceptor;
import to.popin.androidsdk.common.Device;
import to.popin.androidsdk.models.UpdateConnectionModel;

public class CallAcceptanceWaitHandler extends Handler {
    private static final long INTERVAL = 10000; // 10 seconds
    private static final long MAX_DURATION = 300000; // 5 minutes
    private final APIInterface apiInterface;
    private final CallAcceptanceListener callAcceptanceListener;
    private boolean isRunning = false;
    private long elapsedTime = 0;
    private int callQueueId;  // Call ID to track the specific call
    private int queuePosition = -1;

    // Constructor
    public CallAcceptanceWaitHandler(Context context, Device myPhone, Looper looper, int callQueueId, CallAcceptanceListener callAcceptanceListener) {
        super(looper);
        this.callQueueId = callQueueId;
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder().addInterceptor(new AuthInterceptor(myPhone));
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.server_url) + "/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        this.apiInterface = retrofit.create(APIInterface.class);
        this.callAcceptanceListener = callAcceptanceListener;
    }

    // Method to start the waiting task
    public void startWaitingForAcceptance() {
        isRunning = true;
        elapsedTime = 0;
        post(runnableTask);
    }

    // Method to stop the waiting task
    public void stopWaitingForAcceptance() {
        isRunning = false;
        removeCallbacks(runnableTask);
    }

    // Runnable that performs the repeated task
    private final Runnable runnableTask = new Runnable() {
        @Override
        public void run() {
            if (isRunning && elapsedTime < MAX_DURATION) {
                Call<UpdateConnectionModel> call = apiInterface.getCallUpdate(callQueueId);
                call.enqueue(new retrofit2.Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<UpdateConnectionModel> call, @NonNull Response<UpdateConnectionModel> response) {
                        if (response.code() == 200) {
                            UpdateConnectionModel updateConnectionModel = response.body();
                            if (updateConnectionModel != null) {
                                if (updateConnectionModel.status == 1) {
                                    if (updateConnectionModel.position != queuePosition) {
                                        queuePosition = updateConnectionModel.position;
                                        callAcceptanceListener.onQueuePositionChange(queuePosition);
                                    }
                                } else if (updateConnectionModel.status == 2) {
                                    stopWaitingForAcceptance();
                                    callAcceptanceListener.onCallAccepted(updateConnectionModel.call_id);
                                } else if (updateConnectionModel.status == 3) {
                                    stopWaitingForAcceptance();
                                    callAcceptanceListener.onExpertBusy();
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UpdateConnectionModel> call, @NonNull Throwable t) {
                        Log.e("ERR", t.getMessage());
                    }
                });

                elapsedTime += INTERVAL;
                postDelayed(this, INTERVAL);
            } else {
                Log.e("QUEUE","FINISHED");
                callAcceptanceListener.onExpertBusy();
                stopWaitingForAcceptance();
            }
        }
    };

    public interface CallAcceptanceListener {
        void onQueuePositionChange(int position);

        void onCallAccepted(int call_id);

        void onExpertBusy();
    }
}
