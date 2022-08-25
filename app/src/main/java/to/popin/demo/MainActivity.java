package to.popin.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
                        Toast.makeText(MainActivity.this,"CONNECTED",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAllExpertsBusy() {
                        Toast.makeText(MainActivity.this,"EXPERT BUSY",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCallConnected() {
                        Toast.makeText(MainActivity.this,"CALL_CONNECTED",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCallDisconnected() {
                        Toast.makeText(MainActivity.this,"CALL_DISCONNECTED",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCallFail() {
                        Toast.makeText(MainActivity.this,"CALL_FAIL",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }
}