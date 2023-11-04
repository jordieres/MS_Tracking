package com.upm.jgp.healthywear.ui.main.fragments.socks;

import static android.provider.Settings.System.DATE_FORMAT;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.upm.jgp.healthywear.R;
import com.upm.jgp.healthywear.ui.main.activity.MainActivity;
import com.upm.jgp.healthywear.ui.main.activity.TabWearablesActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPOutputStream;

import io.sensoria.sdk.Core;
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

public class SensoriaCoreStreamingServiceActivity extends AppCompatActivity
        implements IStreamingServiceCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final String SENSORIASTREAMINGSERVICE = "SASensoriaStreamingService";
    public static final String BALANCESTREAMINGSERVICE = "SABalanceStreamingService";

    private long time_nowSock = 0;
    private long time_stamp = 0;
    private static final DateFormat DATE_FORMAT_GPS = new SimpleDateFormat("yyyyMMddHHmmssSSS"); //Set the format of the .txt file name.

    private StreamingService streamingService1;
    private ProcessedDataLogger processedDataLogger;
    private StreamingService streamingService2;
    TextView actualSampleRate, packetsLost, nominalSampleRate;
    TextView tick, s0, s1, s2, accX, accY, accZ, magX, magY, magZ, gyrX, gyrY, gyrZ, device1;
    TextView tick2, s02, s12, s22, accX2, accY2, accZ2, magX2, magY2, magZ2, gyrX2, gyrY2, gyrZ2, device2;
    TextView ubicacion;

    private String mDeviceMac = "";
    private String mDeviceMac2 = "";
    private Button btnStop;
    private Button btnStart;
    private Button btnResume;
    private Button btnPause;
    private Button btnStartSession;
    private Button btnEndSession;

    private static String localizationF = "'lat':'-000.000000','lng':'-000.000000'";

    Core core1 = MainActivity.getSock1_Core_global();
    Core core2 = MainActivity.getSock2_Core_global();

    DeviceDescriptor deviceDisco = MainActivity.getSock1_Device_globalD();
    DeviceDescriptor deviceDisco2 = MainActivity.getSock2_Device_globalD();

    private static String mac1 = null;
    private static String mac2 = null;

    private String incoming_device_type;

    TimerTask timerTask; //Timer task for uploading data
    private static Boolean timerstatus = false;
    Timer timer;
    Lock lock = new ReentrantLock(true); //Define a lock to avoid the concurrency problem when writing data to txt file
    private static long time_now;
    private String folderPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "tmp" + File.separator + "cache" + File.separator;


    private static String json_str = "", json_str2 = ""; //"{'t':'0','hr':'0','bph':'0','bpl':'0'}";
    private static int Nrows = 0, Nrows2 = 0;
    //TODO Modify USER, PASSWORD and DBNAME from your MongoDB
    private static String toplines = "{'us':'USER','pass':'PASSWORD','db':'DBNAME','collection':'scks_P1','mac':', 'tmp'}";
    private static String toplines2 = "{'us':'USER','pass':'PASSWORD','db':'DBNAME','collection':'scks_P2','mac':', 'tmp'}";

    private static final String FILE_NAME = "example.txt";

    private GoogleApiClient googleApiClient;

    private LocationRequest locationRequest;
    public final static int MILLISECONDS_PER_SECOND = 1000;
    public final static int MINUTE = 60 * MILLISECONDS_PER_SECOND;
    double fusedLatitude;
    double fusedLongitude;


    String loc;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensoria_core_streaming_service);
        btnStop = findViewById(R.id.buttonStreamingStopService);
        btnStart = findViewById(R.id.buttonStreamingStartService);
        btnResume = findViewById(R.id.buttonStreamingResume);
        btnPause = findViewById(R.id.buttonStreamingPause);
        btnStartSession = findViewById(R.id.buttonStartSession);
        btnEndSession = findViewById(R.id.buttonEndSession);
        actualSampleRate = findViewById(R.id.textViewActualSampleRateValue);
        nominalSampleRate = findViewById(R.id.textViewDefinedSampleRateValue);
        packetsLost = findViewById(R.id.textViewPacketLostValue);
        device1 = findViewById(R.id.device1);
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
        device2 = findViewById(R.id.device2);
        tick2 = findViewById(R.id.textViewTickValue2);
        s02 = findViewById(R.id.textViewS0Value2);
        s12 = findViewById(R.id.textViewS1Value2);
        s22 = findViewById(R.id.textViewS2Value2);
        accX2 = findViewById(R.id.textViewAccXValue2);
        accY2 = findViewById(R.id.textViewAccYValue2);
        accZ2 = findViewById(R.id.textViewAccZValue2);
        magX2 = findViewById(R.id.textViewMagnXValue2);
        magY2 = findViewById(R.id.textViewMagnYValue2);
        magZ2 = findViewById(R.id.textViewMagnZValue2);
        gyrX2 = findViewById(R.id.textViewGyroXValue2);
        gyrY2 = findViewById(R.id.textViewGyroYValue2);
        gyrZ2 = findViewById(R.id.textViewGyroZValue2);
        ubicacion = findViewById(R.id.ubicacionValue);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this)
                .build();
        locationRequest = new LocationRequest();
        locationRequest.setInterval(MINUTE);
        locationRequest.setFastestInterval(15 * MILLISECONDS_PER_SECOND);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (checkPlayServices()) {

            startFusedLocation();

            registerRequestUpdate(this);
        }

        if (MainActivity.isSockConnected()) {
            tick2.setVisibility(View.GONE);
            s02.setVisibility(View.GONE);
            s12.setVisibility(View.GONE);
            s22.setVisibility(View.GONE);
            accX2.setVisibility(View.GONE);
            accY2.setVisibility(View.GONE);
            accZ2.setVisibility(View.GONE);
            magX2.setVisibility(View.GONE);
            magY2.setVisibility(View.GONE);
            magZ2.setVisibility(View.GONE);
            gyrX2.setVisibility(View.GONE);
            gyrY2.setVisibility(View.GONE);
            gyrZ2.setVisibility(View.GONE);
        }

        mDeviceMac = MainActivity.getSock1_mac_global();
        mDeviceMac2 = MainActivity.getSock2_mac_global();
        System.out.println("CONECTADO O NO 1 " + MainActivity.isSockConnected());
        System.out.println("CONECTADO O NO 2 " + MainActivity.isSock2Connected());
        if (MainActivity.isSockConnected()) {
            System.out.println("CONECTADO O NO 1 " + MainActivity.isSock1Connected1());
            streamingService1 = new StreamingService(mDeviceMac);
        }
        if (MainActivity.isSock2Connected()) {
            System.out.println("CONECTADO O NO 2 " + MainActivity.isSock1Connected1());
            streamingService2 = new StreamingService(mDeviceMac2);
        }
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (MainActivity.isSockConnected()) {
                if (extras.containsKey(SENSORIASTREAMINGSERVICE)) {
                    streamingService1 = (StreamingService) getIntent().getSerializableExtra(SENSORIASTREAMINGSERVICE);
                } else if (extras.containsKey(BALANCESTREAMINGSERVICE)) {

                } else {
                    streamingService1 = new StreamingService(mDeviceMac);
                }
            } else if (MainActivity.isSock2Connected()) {
                if (extras.containsKey(SENSORIASTREAMINGSERVICE)) {
                    streamingService1 = (StreamingService) getIntent().getSerializableExtra(SENSORIASTREAMINGSERVICE);
                } else if (extras.containsKey(BALANCESTREAMINGSERVICE)) {

                } else {
                    streamingService1 = new StreamingService(mDeviceMac);
                }
                if (extras.containsKey(SENSORIASTREAMINGSERVICE + "_2")) {
                    streamingService2 = (StreamingService) getIntent().getSerializableExtra(SENSORIASTREAMINGSERVICE + "_2");
                } else if (extras.containsKey(BALANCESTREAMINGSERVICE + "_2")) {

                } else {
                    streamingService2 = new StreamingService(mDeviceMac2);
                }
            }


        } else {
            if (MainActivity.isSockConnected()) {
                streamingService1 = new StreamingService(mDeviceMac);
            } else if (MainActivity.isSock2Connected()) {
                streamingService1 = new StreamingService(mDeviceMac);
                streamingService2 = new StreamingService(mDeviceMac2);
            }
        }

        // Sample of an externally created logger for logging additional processed data within the context of streaming service
        processedDataLogger = new ProcessedDataLogger("TEST-PROCESSED-LOG", DataPoint.getRawDataHeaders() + ",X,Y,Z");

        startSession();
        startTimer();
        time_now = System.currentTimeMillis();

        btnStart.setEnabled(false);
        btnStop.setEnabled(true);

        File cachefolder = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "tmp" + File.separator + "cache");
        if (!cachefolder.exists())
            cachefolder.mkdirs();
        File backfolder = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "tmp" + File.separator + "backup");
        if (!backfolder.exists())
            backfolder.mkdirs();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            if (MainActivity.isSockConnected()) {
                streamingService1.start(this, this);
            } else if (MainActivity.isSock2Connected()) {
                streamingService1.start(this, this);
                streamingService2.start(this, this);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MainActivity.isSockConnected()) {
            streamingService1.resumeStreaming();
        } else if (MainActivity.isSock2Connected()) {
            streamingService1.resumeStreaming();
            streamingService2.resumeStreaming();
        }
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
        if (MainActivity.isSockConnected()) {
            streamingService1.pauseStreaming();
            streamingService1.stop();
        } else if (MainActivity.isSock2Connected()) {
            streamingService1.pauseStreaming();
            streamingService1.stop();
            streamingService2.pauseStreaming();
            streamingService2.stop();
        }


        super.onDestroy();
    }

    public void start(View view) {
        SensoriaSdk.startSession(getApplicationContext(), "test");

        device1.setTextColor(Color.parseColor("#FF99CC00"));

        device2.setTextColor(Color.parseColor("#FF99CC00"));

        try {
            if (MainActivity.isSockConnected()) {
                System.out.println("CONECTADO O NO 1 " + MainActivity.isSock1Connected1());
                streamingService1 = new StreamingService(mDeviceMac);
                streamingService1.start(this, this);
            } else {
                streamingService1.resumeStreaming();
            }
            if (MainActivity.isSock2Connected()) {
                System.out.println("CONECTADO O NO 2 " + MainActivity.isSock1Connected1());
                streamingService2 = new StreamingService(mDeviceMac2);
                streamingService2.start(this, this);
            } else {
                streamingService1.resumeStreaming();
            }
            btnStart.setEnabled(false);
            btnStop.setEnabled(true);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stop(View view) {
        MainActivity.setSockConnected(false);
        MainActivity.setSock2Connected(false);
        finish();
    }

    public void pause(View view) {
        // Disable pause, resume will be enabled when confirmed pause of service is issued
        btnPause.setEnabled(false);
        SessionInfo sessionInfo = SensoriaSdk.endSession();

        if (MainActivity.isSockConnected()) {
            device1.setTextColor(Color.parseColor("#FF99CC00"));
            streamingService1.pauseStreaming();
        } else if (MainActivity.isSock2Connected()) {
            device1.setTextColor(Color.parseColor("#FF99CC00"));
            streamingService1.pauseStreaming();

            device2.setTextColor(Color.parseColor("#FF99CC00"));
            streamingService2.pauseStreaming();
        }
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
    }

    public void resume(View view) {
        // Disable resume, pause will be enabled when confirmed resume of service is issued
        btnResume.setEnabled(false);
        streamingService1.resumeStreaming();
        streamingService2.resumeStreaming();
    }

    public void startSession() {
        try {
            SensoriaSdk.startSession(getApplicationContext(), "test");
            device1.setTextColor(Color.parseColor("#FF99CC00"));

            device2.setTextColor(Color.parseColor("#FF99CC00"));
            //Toast.makeText(this, "raw data being captured", Toast.LENGTH_SHORT).show();

            btnStartSession.setEnabled(false);
            btnEndSession.setEnabled(true);
        } catch (Exception exception) {
            Toast.makeText(this, "Error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void endSession(View view) {
        try {
            SessionInfo sessionInfo = SensoriaSdk.endSession();
            if (MainActivity.isSockConnected()) {
                device1.setTextColor(Color.parseColor("#FFCC0000"));
                streamingService1.pauseStreaming();
            } else if (MainActivity.isSock2Connected()) {
                device1.setTextColor(Color.parseColor("#FFCC0000"));
                streamingService1.pauseStreaming();

                device2.setTextColor(Color.parseColor("#FFCC0000"));
                streamingService2.pauseStreaming();
            }
            Toast.makeText(this, "Data saved at " + sessionInfo.getRootFolder().getCanonicalPath(), Toast.LENGTH_LONG).show();

            btnStart.setEnabled(true);
            btnStop.setEnabled(false);

        } catch (Exception exception) {
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
        if (mac1 == null) {
            mac1 = device.deviceMac;
        } else {
            mac2 = device.deviceMac;
        }
        MainActivity.setSock1_mac_global(mac1);
        MainActivity.setSock2_mac_global(mac2);
        System.out.println("qqqqq la mac didServiceReady 1 " + mac1 + " 2 " + mac2);
    }

    @Override
    public void didUpdateData(DeviceDescriptor device, ServiceType service, final DataPoint dataPoint) {
        System.out.println("qqqqq la mac del device " + device.deviceMac + " 1 " + mac1 + " 2 " + mac2);
        //SdkLog(DEBUG, "APP", String.format("Received data from %s, tick: %d", device.deviceName, dataPoint.getTickCount()));
        //System.out.println("qqqqq la mac del did " + device.deviceMac);


        if (device.deviceMac == mac1) {
            device1.setText(mac1);
            System.out.println("qqqqq la mac1 1 " + mac1 + " 2 " + device.deviceMac);
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
        } else {
            device2.setText(mac2);
            System.out.println("qqqqq la mac2 2 " + mac2 + " 2 " + device.deviceMac);
            s02.setText(String.format(Locale.US, "%d", dataPoint.channels[0]));
            s12.setText(String.format(Locale.US, "%d", dataPoint.channels[1]));
            s22.setText(String.format(Locale.US, "%d", dataPoint.channels[2]));
            accX2.setText(String.format(Locale.US, "%f", dataPoint.accelerometer[0]));
            accY2.setText(String.format(Locale.US, "%f", dataPoint.accelerometer[1]));
            accZ2.setText(String.format(Locale.US, "%f", dataPoint.accelerometer[2]));
            magX2.setText(String.format(Locale.US, "%f", dataPoint.magnetometer[0]));
            magY2.setText(String.format(Locale.US, "%f", dataPoint.magnetometer[1]));
            magZ2.setText(String.format(Locale.US, "%f", dataPoint.magnetometer[2]));
            gyrX2.setText(String.format(Locale.US, "%f", dataPoint.gyroscope[0]));
            gyrY2.setText(String.format(Locale.US, "%f", dataPoint.gyroscope[1]));
            gyrZ2.setText(String.format(Locale.US, "%f", dataPoint.gyroscope[2]));
        }
        ubicacion.setText(localiz());
        //processedDataLogger.logDataPoint(dataPoint.toRawDataString() + ",1,2,3," + device.deviceName);

        //Log.d("AAAAAAAAA","Received data from" + device.deviceName + " " + dataPoint.toRawDataString());

        time_nowSock = System.currentTimeMillis();
        time_stamp = System.currentTimeMillis();
        String shortnameSock = getUintAsTimestamp(time_nowSock);
        System.out.println("HOOORAAA " + shortnameSock);

        String timestamp = getUintAsTimestampGPS(time_stamp);


        if (device.deviceMac == mac1) {
            System.out.println("qqqqq fich " + mac1 + " 2 " + device.deviceMac);
            toplines = "{'us': 'USER', 'pass': 'PASSWORD', 'db': 'DBNAME', 'collection': 'scks_P1', 'mac':'" + mac1 + "', 't_end': '" + shortnameSock + "', " + localiz() + ", 'appVersion':'" + MainActivity.getStringAppVersion() + "' }\n";
            json_str = json_str + "{'Device':'" + mac1 + "','S0':'" + dataPoint.channels[0] + "','S1':'" + dataPoint.channels[1] + "','S2':'" + dataPoint.channels[2] + "','Ax':'" + dataPoint.accelerometer[0] + "','Ay':'" + dataPoint.accelerometer[1] + "','Az':'" + dataPoint.accelerometer[2] + "','Gx':'" + dataPoint.gyroscope[0] + "','Gy':'" + dataPoint.gyroscope[1] + "','Gz':'" + dataPoint.gyroscope[2] + "','Mx':'" + dataPoint.magnetometer[0] + "','My':'" + dataPoint.magnetometer[1] + "','Mz':'" + dataPoint.magnetometer[2] + "','Timestamp':'" + timestamp + "'}\n";
            Nrows++;

            Log.i("*******Nrows*******:", Integer.toString(Nrows));
            //define how many rows in one txt file
            if (Nrows % 1000 == 0) {
                String location2 = localiz();
                String input = toplines + json_str;
                System.out.println("location ess " + location2);
                //***Create text file and write data into it***
                System.out.println("writting to text file");
                lock.lock(); //Lock begin
                FileWriter fw = null;
                String shortname = device.deviceName + "_" + String.valueOf(System.currentTimeMillis());
                shortname = shortname.replaceAll(":", "");
                shortname = shortname.replaceAll("-", "");

                String txtname = folderPath + shortname;
                //create folder if not exist
                File folderfile = new File(folderPath);
                if (!folderfile.exists()) {
                    folderfile.mkdirs();
                }
                File txtfile = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "tmp" + File.separator + "cache" + File.separator + shortname + ".txt");
                if (txtfile.exists()) {                 //check if txt file already existed
                    File ftxt = new File(folderPath);    //if exist count the number of files with this name
                    int ntxt = 0;
                    for (File file : ftxt.listFiles()) {
                        if (file.isFile() && (file.getName().startsWith(shortname)) && (file.getName().endsWith(".txt"))) {
                            ntxt++;
                        }
                    }
                    txtname = txtname + Integer.toString(ntxt);
                }

                try {
                    fw = new FileWriter(txtname + ".txt", true);
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                BufferedWriter bufferWritter = new BufferedWriter(fw);
                try {
                    bufferWritter.write(input);
                    bufferWritter.close();
                    json_str = "";
                } catch (IOException e) {
                    System.out.println("Error writing to and closing file:" + e.getMessage());
                    lock.unlock(); //Release lock
                    return;
                }

                lock.unlock();  //Release lock

            }
        } else {
            System.out.println("qqqqq fich2 " + mac2 + " 2 " + device.deviceMac);
            toplines2 = "{'us': 'USER', 'pass': 'PASSWORD', 'db': 'DBNAME', 'collection': 'scks_P2', 'mac':'" + mac2 + "', 't_end': '" + shortnameSock + "', " + localiz() + ", 'appVersion':'" + MainActivity.getStringAppVersion() + "' }\n";
            json_str2 = json_str2 + "{'Device':'" + mac2 + "','S0':'" + dataPoint.channels[0] + "','S1':'" + dataPoint.channels[1] + "','S2':'" + dataPoint.channels[2] + "','Ax':'" + dataPoint.accelerometer[0] + "','Ay':'" + dataPoint.accelerometer[1] + "','Az':'" + dataPoint.accelerometer[2] + "','Gx':'" + dataPoint.gyroscope[0] + "','Gy':'" + dataPoint.gyroscope[1] + "','Gz':'" + dataPoint.gyroscope[2] + "','Mx':'" + dataPoint.magnetometer[0] + "','My':'" + dataPoint.magnetometer[1] + "','Mz':'" + dataPoint.magnetometer[2] + "','Timestamp':'" + timestamp + "'}\n";
            Nrows2++;

            Log.i("*******Nrows2*******:", Integer.toString(Nrows));
            if (Nrows2 % 1000 == 0) {
                String location3 = localiz();
                String input = toplines2 + json_str2;
                System.out.println("location ess 2 " + location3);
                //***Create text file and write data into it***
                System.out.println("writting to text file");
                lock.lock(); //Lock begin
                FileWriter fw = null;
                String shortname = device.deviceName + "_" + String.valueOf(System.currentTimeMillis());
                shortname = shortname.replaceAll(":", "");
                shortname = shortname.replaceAll("-", "");

                String txtname = folderPath + shortname;
                //create folder if not exist
                File folderfile = new File(folderPath);
                if (!folderfile.exists()) {
                    folderfile.mkdirs();
                }
                File txtfile = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "tmp" + File.separator + "cache" + File.separator + shortname + ".txt");
                if (txtfile.exists()) {                 //check if txt file already existed
                    File ftxt = new File(folderPath);    //if exist count the number of files with this name
                    int ntxt = 0;
                    for (File file : ftxt.listFiles()) {
                        if (file.isFile() && (file.getName().startsWith(shortname)) && (file.getName().endsWith(".txt"))) {
                            ntxt++;
                        }
                    }
                    txtname = txtname + Integer.toString(ntxt);
                }

                try {
                    fw = new FileWriter(txtname + ".txt", true);
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                BufferedWriter bufferWritter = new BufferedWriter(fw);
                try {
                    bufferWritter.write(input);
                    bufferWritter.close();
                    json_str2 = "";
                } catch (IOException e) {
                    System.out.println("Error writing to and closing file:" + e.getMessage());
                    lock.unlock(); //Release lock
                    return;
                }

                lock.unlock();  //Release lock

            }
        }

    }

    private String localiz() {
        double latitude = 0.0;
        double longitude = 0.0;

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            System.out.println("LASTA KNOWN " + loc);
            if (loc != null) {
                latitude = loc.getLatitude();
                longitude = loc.getLongitude();
            }
            localizationF = "'lat':'" + (latitude) + "','lng':'" + longitude + "'";
            return localizationF;
        } else {
            Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            System.out.println("LASTA KNOWN2 " + loc);
            if (loc != null) {
                latitude = loc.getLatitude();
                longitude = loc.getLongitude();
            }
            localizationF = "'lat':'" + (latitude) + "','lng':'" + longitude + "'";
            return localizationF;
        }
    }

    public void otrosDispositivos(View view) {
        if (MainActivity.isSmartbandConnected()) {
            incoming_device_type = "SmartBand";
        } else if (MainActivity.isMmrConnected()) {
            incoming_device_type = "MMR";
        } else if (MainActivity.isMmr2Connected()) {
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

    public void startTimer() {
        timerstatus = true;
        timer = new Timer(); //set a new Timer
        initializeTimerTask(); //initialize the TimerTask's job
        //schedule the timer, after the first 5000ms the TimerTask will run every 60000ms
        timer.schedule(timerTask, 5000, 60000);
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                //***Get file list in the folder // stackoverflow.com/questions/8646984/how-to-list-files-in-an-android-directory
                String folderpath = Environment.getExternalStorageDirectory().getPath() + File.separator + "tmp" + File.separator + "cache";
                String bkpfolder = Environment.getExternalStorageDirectory().getPath() + File.separator + "tmp" + File.separator + "backup";

                try {
                    //File file[] = f.listFiles();
                    File filegz[] = findergz(folderpath);   //get all the .gz file
                    //if (filegz.length>0) {			// If there are .gz files, upload them
                    if (filegz != null && filegz.length > 0) {
                        for (int j = 0; j < filegz.length; j++) {
                            String datapathgz = bkpfolder + File.separator + filegz[j].getName();
                            File bkpfile = new File(datapathgz);
                            //new RetrieveFeedTask_mmr().execute(datapathgz);
                            filegz[j].renameTo(bkpfile);
                        }
                    } else {
                        try {
                            File file[] = finder(folderpath);  //get all the .txt file
                            //if (file.length > 0) {
                            if (file != null && file.length > 0) {
                                for (int i = 0; i < file.length; i++) //Send all the files to the server one by one.
                                {
                                    Log.d("Files", "FileName:" + file[i].getName());
                                    boolean complete = isCompletelyWritten(file[i]); //Check if the file has completely written
                                    String srcpath = folderpath + File.separator + file[i].getName();
                                    String bkppath = bkpfolder + File.separator + file[i].getName();
                                    if (complete) {
                                        //Log.d("Files", "path" + datapath);
                                        //new RetrieveFeedTask_mmr().execute(datapath); //execute new thread 执行同步线程
                                        //Log.d("Files", "i:" + i);
                                        //compress the .txt file to .gz file
                                        String despath0 = srcpath.substring(0, srcpath.indexOf(".")) + ".gz";
                                        //String despath=datapath[0]+".gz";
                                        String gzfile = gzipFile(srcpath, despath0);
                                        //Log.d("GZFILE", gzfile);
                                        // if (!isNullOrEmpty(gzfile)) {   //in case that gzfile is null
                                        File zip = new File(gzfile);
                                        String despath = bkpfolder + File.separator + zip.getName();
                                        File newzip = new File(despath);
                                        zip.renameTo(newzip);
                                        //  }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            Log.d("Files", e.getLocalizedMessage());
                        }
                    }

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.d("Files", e.getLocalizedMessage());
                }
            }
        };
    }

    public String gzipFile(String source_filepath, String destinaton_zip_filepath) {
        System.out.println("Compressing " + source_filepath + "...........");
        byte[] buffer = new byte[1024];
        File textfile = new File(source_filepath);
        if (textfile.exists() && textfile.length() > 1000) {
            String gzfile = countgz(source_filepath);
            destinaton_zip_filepath = source_filepath.substring(0,
                    source_filepath.indexOf(".")) + "_" + gzfile + ".gz";
            //System.out.println("gzfile:"+gzfile);
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(
                        destinaton_zip_filepath);
                GZIPOutputStream gzipOuputStream = new GZIPOutputStream(
                        fileOutputStream);
                FileInputStream fileInput = new FileInputStream(source_filepath);
                int bytes_read;
                while ((bytes_read = fileInput.read(buffer)) > 0) {
                    gzipOuputStream.write(buffer, 0, bytes_read);
                }
                try {
                    fileInput.close();
                } catch (Exception e) {
                    // TODO: handle exception
                }
                try {
                    gzipOuputStream.finish();
                    gzipOuputStream.close();
                } catch (Exception e) {
                    // TODO: handle exception
                }
                //System.out.println("The file was compressed successfully!");
                File gzf = new File(destinaton_zip_filepath);//check if the generated gzfile is larger than 1kb.
                if (gzf.length() > 1000) {
                    textfile.delete();
                    return destinaton_zip_filepath;
                } else {
                    gzf.delete();
                    return null;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            }
        } else {
            if (textfile.exists()) {
                textfile.delete();
            }
            return null;
        }
    }

    public String countgz(String filepath) {
        File txtfile = new File(filepath);
        String fullname = txtfile.getName();
        String folderpath = Environment.getExternalStorageDirectory().getPath() + File.separator + "tmp" + File.separator + "cache" + File.separator;
        String firstname = fullname.substring(0, fullname.indexOf("."));
        //System.out.println("fullname:"+fullname+"folderpath:"+folderpath+"firstname:"+firstname);
        File gzf = new File(folderpath);
        int count = 0;
        for (File file : gzf.listFiles()) {
            if (file.isFile() && (file.getName().startsWith(firstname)) && (file.getName().endsWith(".gz"))) {
                count++;
            }
        }
        return Integer.toString(count);
    }

    public static File[] finder(String dirName) {
        File dir = new File(dirName);
        return dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".txt");
            }
        });
    }

    //find .gz file
    public File[] findergz(String dirName) {
        File dir = new File(dirName);
        return dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".gz");
            }
        });
    }

    //Check if a file is been written.10 seconds since last modification.
    private boolean isCompletelyWritten(File file) {
        long currenttime = System.currentTimeMillis();
        long lastmodify = file.lastModified();
        if (currenttime - lastmodify > (10000)) {
            return true;
        } else {
            return false;
        }
    }

    private static String getUintAsTimestamp(long uint) {
        //return DATE_FORMAT.format(new Date(uint.longValue() * 1000L)).toString();
        //uint=uint+tmadrid.getOffset(uint);
        //DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT+1"));
        return DATE_FORMAT_FICH.format(new Date(uint)).toString();
    }

    private static final DateFormat DATE_FORMAT_FICH = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); //Set the format of the .txt file name.

    private String getUintAsTimestampGPS(Long uint) {
        return DATE_FORMAT_GPS.format(new Date(uint)).toString();
    }

    @Override
    public void didChangeStreaming(DeviceDescriptor deviceDescriptor, ServiceType serviceType, boolean b) {

    }

    @Override
    public void didServiceError(DeviceDescriptor deviceDescriptor, ServiceType serviceType, String s, String s1, SdkError sdkError, String s2) {
        AlertDialog.Builder builder;

        switch (sdkError) {
 /*           case ERROR_INVALID_CHANNEL_PROTOCOL:
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

  */
        }
    }

    @Override
    public void didServiceEvent(DeviceDescriptor deviceDescriptor, ServiceType serviceType, ServiceEvent serviceEvent) {
        switch (serviceEvent) {
            case STARTED:
                try {
                    streamingService1.connect();
                    if (MainActivity.isSock2Connected()) {
                        streamingService2.connect();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case STOPPED:
                streamingService1.stop();
                if (MainActivity.isSock2Connected()) {
                    streamingService2.stop();
                }
                break;
            case CONNECTED:
                didServiceConnect(deviceDescriptor, serviceType);
                break;
            case DISCONNECTED:
                didServiceDisconnect(deviceDescriptor, serviceType);
                break;
            case SIGNAL_LOST:
                if (mDeviceMac != null) {
                    core1.connect(deviceDisco, true);
                }
                if (mDeviceMac2 != null) {
                    core2.connect(deviceDisco2, true);
                }
                break;
            case READY:
                didServiceReady(deviceDescriptor, serviceType);
                break;
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        requestLocationUpdates();
    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }







    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                Toast.makeText(getApplicationContext(),
                                "This device is supported. Please download google play services", Toast.LENGTH_LONG)
                        .show();
            } else {
                Toast.makeText(getApplicationContext(),
                                "This device is not supported.", Toast.LENGTH_LONG)
                        .show();

            }
            return false;
        }
        return true;
    }
    public void startFusedLocation() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnectionSuspended(int cause) {
                        }

                        @Override
                        public void onConnected(Bundle connectionHint) {

                        }
                    }).addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {

                        @Override
                        public void onConnectionFailed(ConnectionResult result) {

                        }
                    }).build();
            googleApiClient.connect();
        } else {
            googleApiClient.connect();
        }
    }

    public void stopFusedLocation() {
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
    }

    public void registerRequestUpdate(final LocationListener listener) {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationRequest.setInterval(1000); // every second
        //mLocationRequest.setFastestInterval(NOTIFY_INTERVAL/2);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, listener);
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                    if (!isGoogleApiClientConnected()) {
                        googleApiClient.connect();
                    }

                    registerRequestUpdate(listener);


                }
            }
        }, 1000);
    }

    public boolean isGoogleApiClientConnected() {
        return googleApiClient != null && googleApiClient.isConnected();
    }

    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        Toast.makeText(getApplicationContext(), "NEW LOCATION RECEIVED", Toast.LENGTH_LONG).show();


        String Provider=location.getProvider();

        Log.d("location", "IN ON LOCATION CHANGE, lat=" + lat + ", lon=" + lon);

        //locationManager.removeUpdates(this);

    }

    public void setFusedLatitude(double lat) {
        fusedLatitude = lat;
    }

    public void setFusedLongitude(double lon) {
        fusedLongitude = lon;
    }

    public double getFusedLatitude() {
        return fusedLatitude;
    }

    public double getFusedLongitude() {
        return fusedLongitude;
    }
}
