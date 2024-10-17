package to.popin.demo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import to.popin.androidsdk.Popin;
import to.popin.androidsdk.PopinEventsListener;
import to.popin.androidsdk.PopinScheduleListener;
import to.popin.androidsdk.models.ScheduleSlotsModel;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button buttonInitialize = findViewById(R.id.buttonInitialize);
        buttonInitialize.setOnClickListener(view -> {
            Popin.init(MainActivity.this);

        });

        Button buttonConnect = findViewById(R.id.buttonConnect);

        buttonConnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Popin.getInstance().startCall(new PopinEventsListener() {
                    @Override
                    public void onCallStart() {
                        Log.e("POPIN", "CALL_START");
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "CALL_START", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onQueuePositionChanged(int position) {
                        Log.e("POPIN", "QUEUE POSITION >" + position);
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "QUEUE POSITION >" + position, Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onAllExpertsBusy() {
                        Log.e("POPIN", "EXPERT BUSY");
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "EXPERT BUSY", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onCallConnected() {
                        Log.e("POPIN", "CALL_CONNECTED");
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "CALL_CONNECTED", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onCallFailed() {
                        Log.e("POPIN", "CALL_FAILED");
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "CALL_FAILED", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onCallDisconnected() {
                        Log.e("POPIN", "CALL_DISCONNECTED");
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "CALL_DISCONNECTED", Toast.LENGTH_SHORT).show());
                    }
                });
            }
        });


        Button buttonSchedule = findViewById(R.id.buttonSchedule);

        buttonSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Popin.getInstance().getAvailableScheduleSlots(new PopinScheduleListener() {
                    @Override
                    public void onAvailableScheduleLoaded(List<ScheduleSlotsModel.ScheduleSlot> scheduleSlots) {
                        Log.e("SCHEDULE_SIZE",">" + scheduleSlots.size());
                    }

                    @Override
                    public void onScheduleLoadError() {

                    }
                });
            }
        });


    }
}