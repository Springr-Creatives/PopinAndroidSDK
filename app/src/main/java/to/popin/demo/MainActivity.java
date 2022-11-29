package to.popin.demo;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

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
        buttonInitialize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Popin.init(MainActivity.this);
                Popin.getInstance().startConnection(new PopinEventsListener() {
                    @Override
                    public void onCallStart() {

                    }

                    @Override
                    public void onAllExpertsBusy() {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "EXPERT BUSY", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onCallConnected() {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "CALL_CONNECTED", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onCallDisconnected() {
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