package to.popin.demo;

import android.Manifest;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button buttonInitialize = findViewById(R.id.buttonInitialize);
        buttonInitialize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Popin.initialize(MainActivity.this);
            }
        });

        Button buttonConnect = findViewById(R.id.buttonConnect);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Popin.getInstance().startConnection(new PopinEventsListener() {
                    @Override
                    public void onConnectionEstablished() {
                        Popin.getInstance().startCall();
                        //Toast.makeText(MainActivity.this,"CONNECTED",Toast.LENGTH_SHORT).show();
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

                    @Override
                    public void onCallFail() {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "CALL_FAIL", Toast.LENGTH_SHORT).show());
                    }
                });
            }
        });

        Dexter.withContext(this)
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                    }
                }).check();
    }
}