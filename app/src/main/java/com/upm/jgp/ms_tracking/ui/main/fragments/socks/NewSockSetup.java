package com.upm.jgp.healthywear.ui.main.fragments.socks;

import static android.util.Log.VERBOSE;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.upm.jgp.healthywear.R;
import com.upm.jgp.healthywear.ui.main.activity.MainActivity;

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

import io.sensoria.sdk.SensoriaSdk;
import io.sensoria.sdk.Core;
import io.sensoria.sdk.DeviceDescriptor;
import io.sensoria.sdk.data.DataPoint;
import io.sensoria.sdk.enums.SdkError;
import io.sensoria.sdk.enums.ServiceEvent;
import io.sensoria.sdk.enums.ServiceType;
import io.sensoria.sdk.interfaces.IServiceCallback;
import io.sensoria.sdk.interfaces.IStreamingServiceCallback;
import io.sensoria.sdk.services.StreamingService;

public class NewSockSetup extends Fragment implements IStreamingServiceCallback, ServiceConnection, IServiceCallback {
    public interface FragmentSettings {
        DeviceDescriptor getBtDevice_sock1();
    }
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        if (settings.getBtDevice_sock1() != null) {
            device = settings.getBtDevice_sock1();
            System.out.println("Metawearrrrrr before: " + device);
            mac = device.deviceMac;
        } else {
            //Try to get it from global variable, probably when it is the second device connected, it was not taken from the Intent...
            device = MainActivity.getSock1_Device_globalD();
            if (device != null) {
                mac = device.deviceMac;
            }
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }



    private FragmentSettings settings;
    private static Activity owner_sock;
    static Context mContext = null;
    private static String sockAddress = null;
    Core core;
    private static DeviceDescriptor device = null;

    private static String sockAddress2 = null;
    Core core2;
    private static DeviceDescriptor device2 = null;

    public NewSockSetup() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        owner_sock = getActivity();
        if (!( owner_sock instanceof FragmentSettings)) {
            throw new ClassCastException("Owning activity must implement the FragmentSettings interface");
        }

        settings = (FragmentSettings)  owner_sock;
        owner_sock.getApplicationContext().bindService(new Intent(owner_sock, BluetoothDevice.class), this, Context.BIND_AUTO_CREATE);

        System.out.println("Entro a onCreate");

        stream = MainActivity.getSock1_stream_global();
        stream2 = MainActivity.getSock2_stream_global();

       mContext = owner_sock.getApplicationContext();

        sockAddress = getActivity().getIntent().getStringExtra("deviceaddress");
        System.out.println("sockadree " + sockAddress);
        if (sockAddress == null || sockAddress2 == null) {
            //Try to get it from global variable, probably when it is the second device connected, it was not taken from the Intent...
            System.out.println("sockadree2 " + sockAddress);
            sockAddress = MainActivity.getSock1_mac_global();
            sockAddress2 = MainActivity.getSock2_mac_global();
            System.out.println("sockadree3 " + sockAddress);
        }
        updateTopLines();

        device = MainActivity.getSock1_Device_globalD();
        device2 = MainActivity.getSock2_Device_globalD();
        System.out.println("Entro a onCreate NOMBRE " + device.deviceName);
        core = MainActivity.getSock1_Core_global();
        core2 = MainActivity.getSock2_Core_global();
        System.out.println("Entro a onCreate CORE " + core.isConnected());

        File cachefolder = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "tmpSock1" + File.separator + "cache");
        if (!cachefolder.exists())
            cachefolder.mkdirs();
        File backfolder = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "tmpSock1" + File.separator + "backup");
        if (!backfolder.exists())
            backfolder.mkdirs();

    }

    private static void updateTopLines() {
        //TODO Modify USER, PASSWORD and DBNAME from your MongoDB
        toplines = "{'us':'USER','pass':'PASSWORD','db':'DBNAME','collection':'scks_P1','mac':', 'tmp'}\n" +
                "{'mac':'" + sockAddress + "','appversion':'" + MainActivity.getStringAppVersion() + "'" + "}\n";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        System.out.println("Entro a onCreateView");
        return inflater.inflate(R.layout.fragment_tab_sock1, container, false);
    }

    public static final String SENSORIASTREAMINGSERVICE = "SensoriaStreamingService";


    private StreamingService streamingService;
    TextView sss, acc, mag, gyr;
    static TextView tv_mac = null;
    private Button btnStop;
    private Button btnStartSession;
    private String mac = null;
    private StreamingService stream = null;

    private StreamingService streamingService2;
    TextView sss2, acc2, mag2, gyr2;
    static TextView tv_mac2 = null;
    private Button btnStop2;
    private Button btnStartSession2;
    private String mac2 = null;
    private StreamingService stream2 = null;

    private String incoming_device_type = "Sock1";

    TimerTask timerTask; //Timer task for uploading data
    private static Boolean timerstatus = false;
    Timer timer;
    Lock lock = new ReentrantLock(true); //Define a lock to avoid the concurrency problem when writing data to txt file

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss"); //Set the format of the .txt file name.
    private static final DateFormat DATE_FORMAT_GPS = new SimpleDateFormat("yyyyMMddHHmmssSSS"); //Set the format of the .txt file name.

    private long time_nowSock = 0;
    private long time_stamp = 0;
    private String folderPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "tmpSock1" + File.separator + "cache" + File.separator;
    private static String json_str = ""; //"{'t':'0','hr':'0','bph':'0','bpl':'0'}";
    private static int Nrows = 0;
    //TODO Modify USER, PASSWORD and DBNAME from your MongoDB
    private static String toplines = "{'us':'USER','pass':'PASSWORD','db':'DBNAME','collection':'scks_P1','mac':', 'tmp'}";

    public void startTimer() {
        System.out.println("Entro a startTimer");

        timerstatus = true;
        timer = new Timer(); //set a new Timer
        initializeTimerTask(); //initialize the TimerTask's job
        //schedule the timer, after the first 5000ms the TimerTask will run every 60000ms
        timer.schedule(timerTask, 5000, 60000);
    }

    public File[] findergz(String dirName) {
        File dir = new File(dirName);
        return dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".gz");
            }
        });
    }

    public static File[] finder(String dirName) {
        File dir = new File(dirName);
        return dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".txt");
            }
        });
    }

    private boolean isCompletelyWritten(File file) {
        long currenttime = System.currentTimeMillis();
        long lastmodify = file.lastModified();
        if (currenttime - lastmodify > (10000)) {
            return true;
        } else {
            return false;
        }
    }

    public String countgz(String filepath) {
        File txtfile = new File(filepath);
        String fullname = txtfile.getName();
        String folderpath = Environment.getExternalStorageDirectory().getPath() + File.separator + "tmpSock1" + File.separator + "cache" + File.separator;
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

    public String gzipFile(String source_filepath, String destinaton_zip_filepath) {
        System.out.println("Compressing " + source_filepath + "...........");
        byte[] buffer = new byte[1024];
        File textfile = new File(source_filepath);
        if (textfile.exists() && textfile.length() > 0) {
            String gzfile = countgz(source_filepath);
            if (gzfile == "0") {
                destinaton_zip_filepath = source_filepath.substring(0,
                        source_filepath.indexOf(".")) + "_" + gzfile + ".gz";
            } else {
                destinaton_zip_filepath = source_filepath.substring(0,
                        source_filepath.indexOf(".")) + ".gz";
            }
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
                //   if (gzf.length() > 1000) {
                if (gzf.length() > 0) {
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

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                //***Get file list in the folder // stackoverflow.com/questions/8646984/how-to-list-files-in-an-android-directory
                String folderpathSock1 = Environment.getExternalStorageDirectory().getPath() + File.separator + "tmpSock1" + File.separator + "cache";
                String bkpfolderSock1 = Environment.getExternalStorageDirectory().getPath() + File.separator + "tmpSock1" + File.separator + "backup";
                System.out.println("Entro a initializeTimerTask");
                try {
                    //File file[] = f.listFiles();
                    File filegz[] = findergz(folderpathSock1);   //get all the .gz file
                    //if (filegz.length>0) {			// If there are .gz files, upload them
                    if (filegz != null && filegz.length > 0) {
                        for (int j = 0; j < filegz.length; j++) {
                            String datapathgz = bkpfolderSock1 + File.separator + filegz[j].getName();
                            File bkpfile = new File(datapathgz);
                            //new RetrieveFeedTask_mmr().execute(datapathgz);
                            filegz[j].renameTo(bkpfile);
                        }
                    } else {
                        try {
                            File file[] = finder(folderpathSock1);  //get all the .txt file
                            //if (file.length > 0) {
                            if (file != null && file.length > 0) {
                                for (int i = 0; i < file.length; i++) //Send all the files to the server one by one.
                                {
//                                    Log.d("Files", "FileName:" + file[i].getName());
                                    boolean complete = isCompletelyWritten(file[i]); //Check if the file has completely written
                                    String srcpath = folderpathSock1 + File.separator + file[i].getName();
                                    String bkppath = bkpfolderSock1 + File.separator + file[i].getName();
                                    if (complete) {
                                        String despath0 = srcpath.substring(0, srcpath.indexOf(".")) + ".gz";
                                        System.out.println("despath0 " + despath0);
                                        String gzfile = gzipFile(srcpath, despath0);
                                        File zip = new File(gzfile);
                                        String despath = bkpfolderSock1 + File.separator + zip.getName();
                                        File newzip = new File(despath);
                                        zip.renameTo(newzip);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
//                            Log.d("Files", e.getLocalizedMessage());
                        }
                    }

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
//                    Log.d("Files", e.getLocalizedMessage());
                }
            }
        };
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        System.out.println("Entro a onViewCreated en 1");
        //Textview to show data
        btnStop = view.findViewById(R.id.sock1_acc_stop);
        tv_mac = view.findViewById(R.id.sock1_value_mac);
        btnStartSession = view.findViewById(R.id.sock1_acc_start);
        sss = view.findViewById(R.id.sock1_value_sss);
        acc = view.findViewById(R.id.sock1_value_acc);
        mag = view.findViewById(R.id.sock1_value_mag);
        gyr = view.findViewById(R.id.sock1_value_gyr);

        btnStop2 = view.findViewById(R.id.sock2_acc_stop);
        tv_mac2 = view.findViewById(R.id.sock2_value_mac);
        btnStartSession2 = view.findViewById(R.id.sock2_acc_start);
        sss2 = view.findViewById(R.id.sock2_value_sss);
        acc2 = view.findViewById(R.id.sock2_value_acc);
        mag2 = view.findViewById(R.id.sock2_value_mag);
        gyr2 = view.findViewById(R.id.sock2_value_gyr);

        // startSession();

        mac = MainActivity.getSock1_mac_global();
        MainActivity.setSock1_mac_global(mac);
        System.out.println("sockadree5 " + mac);
        tv_mac.setText(mac);
        stream = MainActivity.getSock1_stream_global();
        MainActivity.setSock1_stream_global(stream);

        mac2 = MainActivity.getSock2_mac_global();
        MainActivity.setSock2_mac_global(mac2);
        System.out.println("sockadree5 " + mac2);
        tv_mac2.setText(mac2);
        stream2 = MainActivity.getSock2_stream_global();
        MainActivity.setSock2_stream_global(stream2);

        view.findViewById(R.id.sock1_acc_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimer(); //start timer for zipping data
                //change color to green if start on click
                if (core.isConnected()) {
                    tv_mac.setTextColor(Color.parseColor("#FF99CC00"));
                    tv_mac.setText(mac);
                }
                startSession();
            }
        });
        view.findViewById(R.id.sock1_acc_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timer != null)
                    timer.cancel();
                //change color to red if stop on click
                tv_mac.setTextColor(Color.parseColor("#FFCC0000"));

                //change color to green if start on click

                tv_mac.setText("Stopped");
                stopSession();
            }
        });
        view.findViewById(R.id.sock2_acc_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimer(); //start timer for zipping data
                //change color to green if start on click
                if (core2.isConnected()) {
                    tv_mac2.setTextColor(Color.parseColor("#FF99CC00"));
                    tv_mac2.setText(mac2);
                }
                startSession2();
            }
        });
        view.findViewById(R.id.sock1_acc_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timer != null)
                    timer.cancel();
                //change color to red if stop on click
                tv_mac2.setTextColor(Color.parseColor("#FFCC0000"));

                //change color to green if start on click

                tv_mac2.setText("Stopped");
                stopSession2();
            }
        });
    }

    public void startSession() {
        try {
            System.out.println("Entro a startSession");
            if (!SensoriaSdk.isInitialized()) {
                SensoriaSdk.initialize(false, true, VERBOSE, true);
                System.out.println("Esta inicializado? " + SensoriaSdk.isInitialized());
            }
            SensoriaSdk.startSession(mContext, "test");
            if (MainActivity.isSock1Connected1()) {
                System.out.println("DENTRO PERO ANTES " + streamingService);
                streamingService = new StreamingService(sockAddress);
                streamingService.start(this, mContext);
                System.out.println("DENTRO PERO DESPUES " + streamingService);
            } else {
                System.out.println("DENTRO PERO ANTES no con " + streamingService);
                streamingService.resumeStreaming();
                System.out.println("DENTRO PERO DESPUES " + streamingService);
            }
            btnStartSession.setEnabled(false);
            btnStop.setEnabled(true);
        } catch (Exception exception) {

        }
    }
    public void startSession2() {
        try {
            System.out.println("Entro a startSession");
            if (!SensoriaSdk.isInitialized()) {
                SensoriaSdk.initialize(false, true, VERBOSE, true);
                System.out.println("Esta inicializado? " + SensoriaSdk.isInitialized());
            }
            SensoriaSdk.startSession(mContext, "test");
            if (MainActivity.isSock2Connected2()) {
                System.out.println("DENTRO PERO ANTES " + streamingService2);
                streamingService2 = new StreamingService(sockAddress);
                streamingService2.start(this, mContext);
                System.out.println("DENTRO PERO DESPUES " + streamingService2);
            } else {
                System.out.println("DENTRO PERO ANTES no con " + streamingService2);
                streamingService2.resumeStreaming();
                System.out.println("DENTRO PERO DESPUES " + streamingService2);
            }
            btnStartSession2.setEnabled(false);
            btnStop2.setEnabled(true);
        } catch (Exception exception) {

        }
    }

    public void stopSession() {
        try {
            System.out.println("Entro a stopSession");
            //SensoriaSdk.startSession(mContext.getApplicationContext(), "test");
            SensoriaSdk.endSession();
            if (MainActivity.isSock1Connected1()) {
                System.out.println("DENTRO PERO ANTES pauseStreaming " + streamingService + " status " + streamingService.getStatus());
                streamingService.pauseStreaming();
                System.out.println("DENTRO PERO DESPUES pauseStreaming" + streamingService + " status " + streamingService.getStatus());
            } else {
            }
            btnStartSession.setEnabled(true);
            btnStop.setEnabled(false);
        } catch (Exception exception) {

        }
    }
    public void stopSession2() {
        try {
            System.out.println("Entro a stopSession");
            //SensoriaSdk.startSession(mContext.getApplicationContext(), "test");
            SensoriaSdk.endSession();
            if (MainActivity.isSock2Connected2()) {
                System.out.println("DENTRO PERO ANTES pauseStreaming " + streamingService2 + " status " + streamingService2.getStatus());
                streamingService2.pauseStreaming();
                System.out.println("DENTRO PERO DESPUES pauseStreaming" + streamingService2 + " status " + streamingService2.getStatus());
            } else {
            }
            btnStartSession2.setEnabled(true);
            btnStop2.setEnabled(false);
        } catch (Exception exception) {

        }
    }

    private String getUintAsTimestamp(Long uint) {
        return DATE_FORMAT.format(new Date(uint)).toString();
    }

    private String getUintAsTimestampGPS(Long uint) {
        return DATE_FORMAT_GPS.format(new Date(uint)).toString();
    }

    @Override
    public void didUpdateData(DeviceDescriptor device, ServiceType service, DataPoint dataPoint) {

        System.out.println("Entro a didUpdateData");
        mac = device.deviceMac;
        MainActivity.setSock1_device_globalD(device);
        System.out.println("sockadree4 " + mac);
        MainActivity.setSock1_mac_global(mac);
        System.out.println("sockadree5 " + mac);
        tv_mac.setText(mac);

        System.out.println("Entro a didUpdateData");
        mac2 = device.deviceMac;
        MainActivity.setSock2_device_globalD(device);
        System.out.println("sockadree4 " + mac2);
        MainActivity.setSock2_mac_global(mac2);
        System.out.println("sockadree5 " + mac2);
        tv_mac2.setText(mac2);

        String ax = "{x:" + String.format(Locale.US, "%f", dataPoint.magnetometer[0]);
        String ay = "y:" + String.format(Locale.US, "%f", dataPoint.magnetometer[1]);
        String az = "z:" + String.format(Locale.US, "%f", dataPoint.magnetometer[2]) + "}";
        String accel = ax + ", " + ay + ", " + az;
        mag.setText(accel);

        String mx = "{x:" + String.format(Locale.US, "%f", dataPoint.accelerometer[0]);
        String my = "y:" + String.format(Locale.US, "%f", dataPoint.accelerometer[1]);
        String mz = "z:" + String.format(Locale.US, "%f", dataPoint.accelerometer[2]) + "}";
        String magn = mx + ", " + my + ", " + mz;
        acc.setText(magn);

        String gx = "{x:" + String.format(Locale.US, "%f", dataPoint.gyroscope[0]);
        String gy = "y:" + String.format(Locale.US, "%f", dataPoint.gyroscope[1]);
        String gz = "z:" + String.format(Locale.US, "%f", dataPoint.gyroscope[2]) + "}";
        String gyro = gx + ", " + gy + ", " + gz;
        gyr.setText(gyro);

        String sx = "{s0:" + String.format(Locale.US, "%d", dataPoint.channels[0]);
        String sy = "s1:" + String.format(Locale.US, "%d", dataPoint.channels[1]);
        String sz = "s2:" + String.format(Locale.US, "%d", dataPoint.channels[2]) + "}";
        String ssss = sx + ", " + sy + ", " + sz;
        sss.setText(ssss);


        // THIS IS NULL, needs to be initialized
        // processedDataLogger.logDataPoint(dataPoint.toRawDataString() + ",1,2,3");
        time_nowSock = System.currentTimeMillis();
        time_stamp = System.currentTimeMillis();
        String shortnameSock = getUintAsTimestamp(time_nowSock);
        String timestamp = getUintAsTimestampGPS(time_stamp);

        toplines = "{'us': 'USER', 'pass': 'PASSWORD', 'db': 'DBNAME', 'collection': 'scks_P1', 'mac':' " + device.deviceMac + "', 't_end': '" + shortnameSock + "', 'appVersion': '" + MainActivity.getStringAppVersion() + "' }\n";
        json_str = json_str + "{'Device':'" + device.deviceName + "','S0':'" + dataPoint.channels[0] + "','S1':'" + dataPoint.channels[1] + "','S2':'" + dataPoint.channels[2] + "','Ax':'" + dataPoint.accelerometer[0] + "','Ay':'" + dataPoint.accelerometer[1] + "','Az':'" + dataPoint.accelerometer[2] + "','Gx':'" + dataPoint.gyroscope[0] + "','Gy':'" + dataPoint.gyroscope[1] + "','Gz':'" + dataPoint.gyroscope[2] + "','Mx':'" + dataPoint.magnetometer[0] + "','My':'" + dataPoint.magnetometer[1] + "','Mz':'" + dataPoint.magnetometer[2] + "','Timestamp':'" + timestamp + "'}\n";
        Nrows++;

//        Log.i("*******Nrows*******:", Integer.toString(Nrows));
        //define how many rows in one txt file
        if (Nrows % 750 == 0) {
            String input = toplines + json_str;
            //***Create text file and write data into it***
            System.out.println("writting to text file");
            lock.lock(); //Lock begin
            FileWriter fw = null;
            String shortname = device.deviceName + "_" + shortnameSock;
            shortname = shortname.replaceAll(":", "");
            shortname = shortname.replaceAll("-", "");

            String txtname = folderPath + shortname;
            //create folder if not exist
            File folderfile = new File(folderPath);
            if (!folderfile.exists()) {
                folderfile.mkdirs();
            }
            File txtfile = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "tmpSock1" + File.separator + "cache" + File.separator + shortname + ".txt");
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
            } catch (IOException e) {
                System.out.println("Error writing to and closing file:" + e.getMessage());
                lock.unlock(); //Release lock
                return;
            }

            lock.unlock();  //Release lock

        }
    }

    @Override
    public void didChangeStreaming(DeviceDescriptor deviceDescriptor, ServiceType serviceType, boolean b) {


    }

    @Override
    public void didServiceError(DeviceDescriptor device, ServiceType service, String s, String s1, SdkError saErrors, String innerErrorCode) {
        AlertDialog.Builder builder;
        System.out.println("Entro a didServiceError");
        switch (saErrors) {
            case ERROR_INVALID_CHANNEL_PROTOCOL:
                System.out.println("Invalid channel protocol, Inner Exception");
                /*
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_INVALID_CHANNEL_PROTOCOL)
                                + "\nInner Exception: " + innerErrorCode)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                */
                break;
            default:
                System.out.println("Error!, Unknown error");
                /*
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_FAILURE)
                                + "\nInner Exception: " + innerErrorCode)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                */
                break;
        }
    }

    @Override
    public void didServiceEvent(DeviceDescriptor deviceDescriptor, ServiceType serviceType, ServiceEvent serviceEvent) {

        switch (serviceEvent) {
            case STARTED:
                try {
                    streamingService.connect();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case STOPPED:
                streamingService.stop();
                break;
            case CONNECTED:
                break;
            case DISCONNECTED:
                break;
            case SIGNAL_LOST:
                break;
            case READY:
                break;
        }
    }
}
