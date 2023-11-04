package com.upm.jgp.healthywear.ui.main.fragments.socks;

import static android.util.Log.DEBUG;

import static io.sensoria.sdk.SensoriaSdk.sdkLog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.upm.jgp.healthywear.R;
import com.upm.jgp.healthywear.ui.main.activity.MainActivity;
import com.upm.jgp.healthywear.ui.main.activity.TabWearablesActivity;

import java.util.Locale;

import io.sensoria.sdk.DeviceDescriptor;
import io.sensoria.sdk.SensoriaSdk;
import io.sensoria.sdk.data.DataPoint;
import io.sensoria.sdk.data.SessionInfo;
import io.sensoria.sdk.enums.SdkError;
import io.sensoria.sdk.enums.ServiceEvent;
import io.sensoria.sdk.enums.ServiceType;
import io.sensoria.sdk.interfaces.IStreamingServiceCallback;
import io.sensoria.sdk.loggers.ProcessedDataLogger;
import io.sensoria.sdk.services.StreamingService;

public class SensoriaCoreStreamingServiceActivity2 extends AppCompatActivity
        implements IStreamingServiceCallback {

    public static final String SENSORIASTREAMINGSERVICE = "SASensoriaStreamingService";
    public static final String BALANCESTREAMINGSERVICE = "SABalanceStreamingService";

    private StreamingService streamingService1;
    private ProcessedDataLogger processedDataLogger;
    private StreamingService streamingService2;
    TextView actualSampleRate, packetsLost, nominalSampleRate;
    TextView tick, s0, s1, s2, accX, accY, accZ, magX, magY, magZ, gyrX, gyrY, gyrZ;
    private String mDeviceMac = "";
    private Button btnStop;
    private Button btnResume;
    private Button btnPause;
    private Button btnStartSession;
    private Button btnEndSession;

    private String incoming_device_type;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensoria_core_streaming_service2);
        btnStop = findViewById(R.id.buttonStreamingStopService);
        btnResume = findViewById(R.id.buttonStreamingResume);
        btnPause = findViewById(R.id.buttonStreamingPause);
        btnStartSession = findViewById(R.id.buttonStartSession);
        btnEndSession = findViewById(R.id.buttonEndSession);
        actualSampleRate = findViewById(R.id.textViewActualSampleRateValue);
        nominalSampleRate = findViewById(R.id.textViewDefinedSampleRateValue);
        packetsLost = findViewById(R.id.textViewPacketLostValue);
        tick = findViewById(R.id.textViewTickValue);
        s0 = findViewById(R.id.textViewS0Value);
        s1 = findViewById(R.id.textViewS1Value);
        s2 = findViewById(R.id.textViewS2Value);
        accX = findViewById(R.id.textViewAccXValue);
        accY = findViewById(R.id.textViewAccYValue);
        accZ = findViewById(R.id.textViewAccZValue);
        magX = findViewById(R.id.textViewMagnXValue);
        magY = findViewById(R.id.textViewMagnYValue);
        magZ = findViewById(R.id.textViewMagnZValue);
        gyrX = findViewById(R.id.textViewGyroXValue);
        gyrY = findViewById(R.id.textViewGyroYValue);
        gyrZ = findViewById(R.id.textViewGyroZValue);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(SENSORIASTREAMINGSERVICE)) {
                streamingService2 = (StreamingService) getIntent().getSerializableExtra(SENSORIASTREAMINGSERVICE);
            } else if (extras.containsKey(BALANCESTREAMINGSERVICE)) {

            } else {
                streamingService2 = new StreamingService(mDeviceMac);
            }

//            if (extras.containsKey(SENSORIASTREAMINGSERVICE + "_2")) {
//                streamingService2 = (SASensoriaStreamingService) getIntent().getSerializableExtra(SENSORIASTREAMINGSERVICE + "_2");
//            } else if (extras.containsKey(BALANCESTREAMINGSERVICE + "_2")) {
//                streamingService2 = (SABalanceStreamingService) getIntent().getSerializableExtra(BALANCESTREAMINGSERVICE + "_2");
//            }
//            else {
//                streamingService2 = new SASensoriaStreamingService(mDeviceMac);
//            }
        } else {
            streamingService2 = new StreamingService(mDeviceMac);
//            streamingService2 = new SASensoriaStreamingService(mDeviceMac);
        }

        // Sample of an externally created logger for logging additional processed data within the context of streaming service
        processedDataLogger = new ProcessedDataLogger("TEST-PROCESSED-LOG", DataPoint.getRawDataHeaders() + ",X,Y,Z");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            streamingService2.start(this, this);
//            streamingService2.start(this, this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        streamingService2.resumeStreaming();
//            streamingService2.resume();
    }


    @Override
    protected void onPause() {
        super.onPause();
        didServicePause(null, null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        didServiceDisconnect(null, null);
    }

    @Override
    protected void onDestroy() {
        streamingService2.pauseStreaming();
        streamingService2.stop();
//        streamingService2.pause();
//        streamingService2.stop();
        super.onDestroy();
    }

    public void stop(View view) {
        finish();
    }

    public void pause(View view) {
        // Disable pause, resume will be enabled when confirmed pause of service is issued
        btnPause.setEnabled(false);
        streamingService2.pauseStreaming();
//        streamingService2.pause();
    }

    public void resume(View view) {
        // Disable resume, pause will be enabled when confirmed resume of service is issued
        btnResume.setEnabled(false);
        streamingService2.resumeStreaming();
//            streamingService2.resume();
    }

    public void startSession(View view) {
        try {
            SensoriaSdk.startSession(getApplicationContext(), "test");

            Toast.makeText(this, "raw data being captured", Toast.LENGTH_SHORT).show();

            btnStartSession.setEnabled(false);
            btnEndSession.setEnabled(true);
        }
        catch (Exception exception) {
            Toast.makeText(this, "Error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void endSession(View view) {
        try {
            SessionInfo sessionInfo = SensoriaSdk.endSession();

            Toast.makeText(this, "Data saved at " + sessionInfo.getRootFolder().getCanonicalPath(), Toast.LENGTH_LONG).show();

            btnStartSession.setEnabled(true);
            btnEndSession.setEnabled(false);
        }
        catch (Exception exception) {
            Toast.makeText(this, "Error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String createErrorMessage(String serviceName, String functionName, String error) {
        return serviceName + "." + functionName + ": " + error;
    }

    public void didServiceConnect(DeviceDescriptor device, ServiceType service) {
        btnStop.setEnabled(true);
        btnResume.setEnabled(false);
        btnPause.setEnabled(true);
    }

    public void didServiceDisconnect(DeviceDescriptor device, ServiceType service) {
    }

    public void didServicePause(DeviceDescriptor device, ServiceType service) {
        btnPause.setEnabled(false);
        btnStop.setEnabled(true);
        btnResume.setEnabled(true);
    }

    public void didServiceResume(DeviceDescriptor device, ServiceType service) {
        btnPause.setEnabled(true);
        btnResume.setEnabled(false);
        btnStop.setEnabled(true);
    }

    public void didServiceReady(DeviceDescriptor device, ServiceType service) {

    }

    public void didServiceReset(DeviceDescriptor device, ServiceType service) {

    }

    public void otrosDispositivos (View view) {
        if (MainActivity.isSmartbandConnected()) {
            incoming_device_type = "SmartBand";
        } else if (MainActivity.isMmrConnected()){
            incoming_device_type = "MMR";
        } else if (MainActivity.isMmr2Connected()){
            incoming_device_type = "MMR2";
        } else {
            incoming_device_type = "SmartBand";
        }
        Intent intent = new Intent(this, TabWearablesActivity.class);
        //Intent intent = new Intent(this, SensoriaCoreStreamingServiceActivity.class);
        Bundle bundle = new Bundle();
        intent.putExtra(TabWearablesActivity.DEVICE_TYPE, incoming_device_type);

        intent.putExtras(bundle);

        startActivity(intent);
    }

    @Override
    public void didUpdateData(DeviceDescriptor deviceDescriptor, ServiceType serviceType, DataPoint dataPoint) {

        sdkLog(DEBUG, "APP", String.format("Received data from %s, tick: %d", deviceDescriptor.deviceName));

        s0.setText(String.format(Locale.US, "%d", dataPoint.channels[0]));
        s1.setText(String.format(Locale.US, "%d", dataPoint.channels[1]));
        s2.setText(String.format(Locale.US, "%d", dataPoint.channels[2]));
        accX.setText(String.format(Locale.US, "%f", dataPoint.accelerometer[0]));
        accY.setText(String.format(Locale.US, "%f", dataPoint.accelerometer[1]));
        accZ.setText(String.format(Locale.US, "%f", dataPoint.accelerometer[2]));
        magX.setText(String.format(Locale.US, "%f", dataPoint.magnetometer[0]));
        magY.setText(String.format(Locale.US, "%f", dataPoint.magnetometer[1]));
        magZ.setText(String.format(Locale.US, "%f", dataPoint.magnetometer[2]));
        gyrX.setText(String.format(Locale.US, "%f", dataPoint.gyroscope[0]));
        gyrY.setText(String.format(Locale.US, "%f", dataPoint.gyroscope[1]));
        gyrZ.setText(String.format(Locale.US, "%f", dataPoint.gyroscope[2]));

        processedDataLogger.logDataPoint(dataPoint.toRawDataString() + ",1,2,3");
    }


    @Override
    public void didChangeStreaming(DeviceDescriptor deviceDescriptor, ServiceType serviceType, boolean b) {

    }

    @Override
    public void didServiceError(DeviceDescriptor deviceDescriptor, ServiceType serviceType, String s, String s1, SdkError sdkError, String s2) {
        AlertDialog.Builder builder;

        switch (sdkError) {
            case ERROR_INVALID_CHANNEL_PROTOCOL:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_INVALID_CHANNEL_PROTOCOL)
                                + "\nInner Exception: " + s2)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                break;


            default:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_FAILURE)
                                + "\nInner Exception: " + s2)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                break;
        }
    }

    @Override
    public void didServiceEvent(DeviceDescriptor deviceDescriptor, ServiceType serviceType, ServiceEvent serviceEvent) {
        switch (serviceEvent) {
            case STARTED:
                try {
                    streamingService1.connect();
                    streamingService2.connect();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case STOPPED:
                streamingService1.stop();
                streamingService2.stop();
                break;
            case CONNECTED:
                didServiceConnect(deviceDescriptor, serviceType);
                break;
            case DISCONNECTED:
                didServiceDisconnect(deviceDescriptor, serviceType);
                break;
            case SIGNAL_LOST:
                break;
            case READY:
                didServiceReady(deviceDescriptor, serviceType);
                break;
        }
    }
}
