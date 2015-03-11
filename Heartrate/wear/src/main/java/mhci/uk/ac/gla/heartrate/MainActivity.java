package mhci.uk.ac.gla.heartrate;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends Activity implements DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String WORKOUT_KEY = "mhci.uk.ac.gla.heartrate.key.workout";
    private DataMap workoutTimeAndHeartRate = new DataMap();
    private GoogleApiClient mGoogleApiClient;

    private TextView mHeartBeatTextView;
    private TextView mDurationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mHeartBeatTextView = (TextView) stub.findViewById(R.id.heartrate);
                mDurationTextView = (TextView) stub.findViewById(R.id.duration);
                startCountdown();
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // heart rate
        VibrationController vc = new VibrationController(this);
        vc.setRange(800, 1000);
        vc.start();
    }

    private void startCountdown() {
        new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
                String zero = (millisUntilFinished / 1000 >= 10) ? "" : "0";
                mDurationTextView.setText("00:00:" + zero + millisUntilFinished / 1000);
            }

            public void onFinish() {
                mDurationTextView.setText("done!");
            }
        }.start();
    }

    @Override
    protected void onResume() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {}

    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // workoutTimeAndHeartRate changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/workout") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    updateWorkout(dataMap.getDataMap(WORKOUT_KEY));
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // workoutTimeAndHeartRate deleted
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    private void updateWorkout(DataMap workout){
        workoutTimeAndHeartRate=workout;
    }

    public void update(Float heartRate) {
        if(mHeartBeatTextView != null) {
            if(heartRate > 0)
                mHeartBeatTextView.setText(heartRate.toString());
            else
                mHeartBeatTextView.setText("---");
        }
    }

}
