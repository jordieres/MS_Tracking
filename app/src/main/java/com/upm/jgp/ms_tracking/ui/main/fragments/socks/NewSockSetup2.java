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

import io.sensoria.sdk.Core;
import io.sensoria.sdk.DeviceDescriptor;
import io.sensoria.sdk.SensoriaSdk;
import io.sensoria.sdk.data.DataPoint;
import io.sensoria.sdk.enums.CoreStatus;
import io.sensoria.sdk.enums.SdkError;
import io.sensoria.sdk.enums.ServiceEvent;
import io.sensoria.sdk.enums.ServiceType;
import io.sensoria.sdk.interfaces.IServiceCallback;
import io.sensoria.sdk.interfaces.IStreamingServiceCallback;
import io.sensoria.sdk.services.StreamingService;

public class NewSockSetup2 extends Fragment implements IStreamingServiceCallback, ServiceConnection,  IServiceCallback {
    public interface FragmentSettings {
        DeviceDescriptor getBtDevice_sock2();
    }
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        if (settings.getBtDevice_sock2() != null) {
            device2 = settings.getBtDevice_sock2();
            System.out.println("Metawearrrrrr before: " + device2);
            mac2 = device2.deviceMac;
        } else {
            //Try to get it from global variable, probably when it is the second device connected, it was not taken from the Intent...
            device2 = MainActivity.getSock1_Device_globalD();
            if (device2 != null) {
                mac2 = device2.deviceMac;
            }
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }
    private FragmentSettings settings;
    private static Activity owner_sock2;
    static Context mContext2 = null;
    private static String sockAddress2 = null;
    Core core2;
    private static DeviceDescriptor device2 = null;
    private StreamingService streamingService2;
    TextView sss2, acc2, mag2, gyr2;
    static TextView tv_mac2 = null;
    private Button btnStop2;
    private Button btnStartSession2;
    private String mac2 = null;
    private StreamingService stream2 = null;
    private long time_nowSock2 = 0;
    private long time_stamp2 = 0;
    private String folderPath2 = Environment.getExternalStorageDirectory().getPath() + File.separator + "tmpSock2" + File.separator + "cache" + File.separator;
    private static String json_str2 = ""; //"{'t':'0','hr':'0','bph':'0','bpl':'0'}";
    private static int Nrows2 = 0;
    //TODO Modify USER, PASSWORD and DBNAME from your MongoDB
    private static String toplines2 = "{'us':'USER','pass':'PASSWORD','db':'DBNAME','collection':'scks_P1','mac':', 'tmp'}";
    private static final DateFormat DATE_FORMAT2 = new SimpleDateFormat("yyyyMMdd_HHmmss"); //Set the format of the .txt file name.
    private static final DateFormat DATE_FORMAT_GPS2 = new SimpleDateFormat("yyyyMMddHHmmssSSS"); //Set the format of the .txt file name.
    TimerTask timerTask2; //Timer task for uploading data
    private static Boolean timerstatus2 = false;
    Timer timer2;
    Lock lock2 = new ReentrantLock(true); //Define a lock to avoid the concurrency problem when writing data to txt file

    CoreStatus status;

    public NewSockSetup2() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        owner_sock2 = getActivity();
        if (!( owner_sock2 instanceof FragmentSettings)) {
            throw new ClassCastException("Owning activity must implement the FragmentSettings interface");
        }
        settings = (FragmentSettings)  owner_sock2;
        owner_sock2.getApplicationContext().bindService(new Intent(owner_sock2, BluetoothDevice.class), this, Context.BIND_AUTO_CREATE);

        System.out.println("Entro a onCreate");

        stream2 = MainActivity.getSock2_stream_global();
        mContext2 = owner_sock2.getApplicationContext();

        sockAddress2 = getActivity().getIntent().getStringExtra("deviceaddress");
        System.out.println("sockadree " + sockAddress2);
        if (sockAddress2 == null) {
            //Try to get it from global variable, probably when it is the second device connected, it was not taken from the Intent...
            System.out.println("sockadree2 " + sockAddress2);
            sockAddress2 = MainActivity.getSock2_mac_global();
            System.out.println("sockadree3 " + sockAddress2);
        }
        updateTopLines2();

        device2 = MainActivity.getSock2_Device_globalD();
        System.out.println("Entro a onCreate NOMBRE " + device2.deviceName);
        core2 = MainActivity.getSock2_Core_global();
        status = core2.getStatus();
        System.out.println("Entro a onCreate CORE " + core2.isConnected());

        File cachefolder = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "tmpSock2" + File.separator + "cache");
        if (!cachefolder.exists())
            cachefolder.mkdirs();
        File backfolder = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "tmpSock2" + File.separator + "backup");
        if (!backfolder.exists())
            backfolder.mkdirs();

    }

    private static void updateTopLines2() {
        //TODO Modify USER, PASSWORD and DBNAME from your MongoDB
        toplines2 = "{'us':'USER','pass':'PASSWORD','db':'DBNAME','collection':'scks_P1','mac':', 'tmp'}\n" +
                "{'mac':'" + sockAddress2 + "','appversion':'" + MainActivity.getStringAppVersion() + "'" + "}\n";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        status = core2.getStatus();
        System.out.println("Entro a onCreateView del 2 " + core2.isConnected());
        return inflater.inflate(R.layout.fragment_tab_sock1, container, false);
    }

    public void startTimer2() {
        System.out.println("Entro a startTimer del 2 " + core2.isConnected());
        status = core2.getStatus();
        timerstatus2 = true;
        timer2 = new Timer(); //set a new Timer
        initializeTimerTask2(); //initialize the TimerTask's job
        //schedule the timer, after the first 5000ms the TimerTask will run every 60000ms
        timer2.schedule(timerTask2, 5000, 60000);
    }

    public File[] findergz2(String dirName) {
        File dir = new File(dirName);
        return dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".gz");
            }
        });
    }

    public static File[] finder2(String dirName) {
        File dir = new File(dirName);
        return dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".txt");
            }
        });
    }

    private boolean isCompletelyWritten2(File file) {
        long currenttime = System.currentTimeMillis();
        long lastmodify = file.lastModified();
        if (currenttime - lastmodify > (10000)) {
            return true;
        } else {
            return false;
        }
    }

    public String countgz2(String filepath) {
        File txtfile = new File(filepath);
        String fullname = txtfile.getName();
        String folderpath = Environment.getExternalStorageDirectory().getPath() + File.separator + "tmpSock2" + File.separator + "cache" + File.separator;
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

    public String gzipFile2(String source_filepath, String destinaton_zip_filepath) {
        System.out.println("Compressing " + source_filepath + "...........");
        byte[] buffer = new byte[1024];
        File textfile = new File(source_filepath);
        if (textfile.exists() && textfile.length() > 0) {
            String gzfile = countgz2(source_filepath);
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

    public void initializeTimerTask2() {
        timerTask2 = new TimerTask() {
            public void run() {
                //***Get file list in the folder // stackoverflow.com/questions/8646984/how-to-list-files-in-an-android-directory
                String folderpathSock2 = Environment.getExternalStorageDirectory().getPath() + File.separator + "tmpSock2" + File.separator + "cache";
                String bkpfolderSock2 = Environment.getExternalStorageDirectory().getPath() + File.separator + "tmpSock2" + File.separator + "backup";
                System.out.println("Entro a initializeTimerTask2 del 2 " + core2.isConnected());
                try {
                    //File file[] = f.listFiles();
                    File filegz[] = findergz2(folderpathSock2);   //get all the .gz file
                    //if (filegz.length>0) {			// If there are .gz files, upload them
                    if (filegz != null && filegz.length > 0) {
                        for (int j = 0; j < filegz.length; j++) {
                            String datapathgz = bkpfolderSock2 + File.separator + filegz[j].getName();
                            File bkpfile = new File(datapathgz);
                            //new RetrieveFeedTask_mmr().execute(datapathgz);
                            filegz[j].renameTo(bkpfile);
                        }
                    } else {
                        try {
                            File file[] = finder2(folderpathSock2);  //get all the .txt file
                            //if (file.length > 0) {
                            if (file != null && file.length > 0) {
                                for (int i = 0; i < file.length; i++) //Send all the files to the server one by one.
                                {
//                                    Log.d("Files", "FileName:" + file[i].getName());
                                    boolean complete = isCompletelyWritten2(file[i]); //Check if the file has completely written
                                    String srcpath = folderpathSock2 + File.separator + file[i].getName();
                                    String bkppath = bkpfolderSock2 + File.separator + file[i].getName();
                                    if (complete) {
                                        String despath0 = srcpath.substring(0, srcpath.indexOf(".")) + ".gz";
                                        System.out.println("despath0 " + despath0);
                                        String gzfile = gzipFile2(srcpath, despath0);
                                        File zip = new File(gzfile);
                                        String despath = bkpfolderSock2 + File.separator + zip.getName();
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
        System.out.println("Entro a onViewCreated del 2 " + core2.isConnected());
        //Textview to show data
        status = core2.getStatus();
        btnStop2 = view.findViewById(R.id.sock2_acc_stop);
        tv_mac2 = view.findViewById(R.id.sock2_value_mac);
        btnStartSession2 = view.findViewById(R.id.sock2_acc_start);
        sss2 = view.findViewById(R.id.sock2_value_sss);
        acc2 = view.findViewById(R.id.sock2_value_acc);
        mag2 = view.findViewById(R.id.sock2_value_mag);
        gyr2 = view.findViewById(R.id.sock2_value_gyr);

        // startSession();
        status = core2.getStatus();
        mac2 = MainActivity.getSock2_mac_global();
        MainActivity.setSock2_mac_global(mac2);
        System.out.println("sockadree5 " + mac2);
        status = core2.getStatus();
        tv_mac2.setText(mac2);

        stream2 = MainActivity.getSock2_stream_global();
        MainActivity.setSock2_stream_global(stream2);

        System.out.println("Entro a onViewCreated del 22 " + core2.isConnected());
        status = core2.getStatus();
        view.findViewById(R.id.sock2_acc_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimer2(); //start timer for zipping data
                //change color to green if start on click
                if (core2.isConnected()) {
                    status = core2.getStatus();
                    tv_mac2.setTextColor(Color.parseColor("#FF99CC00"));
                    tv_mac2.setText(mac2);
                }
                startSession2();
            }
        });
        view.findViewById(R.id.sock2_acc_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timer2 != null)
                    timer2.cancel();
                //change color to red if stop on click
                tv_mac2.setTextColor(Color.parseColor("#FFCC0000"));

                //change color to green if start on click

                tv_mac2.setText("Stopped");
                stopSession2();
            }
        });
    }

    public void startSession2() {
        try {
            System.out.println("Entro a startSession del 2 " + core2.isConnected());
            if (!SensoriaSdk.isInitialized()) {
                SensoriaSdk.initialize(false, true, VERBOSE, true);
                System.out.println("Esta inicializado? " + SensoriaSdk.isInitialized());
            }
            SensoriaSdk.startSession(mContext2, "test");
            if (MainActivity.isSock2Connected2()) {
                System.out.println("DENTRO PERO ANTES " + streamingService2);
                streamingService2 = new StreamingService(sockAddress2);
                streamingService2.start(this, mContext2);
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
        return DATE_FORMAT2.format(new Date(uint)).toString();
    }

    private String getUintAsTimestampGPS(Long uint) {
        return DATE_FORMAT_GPS2.format(new Date(uint)).toString();
    }


    @Override
    public void didUpdateData(DeviceDescriptor deviceDescriptor, ServiceType serviceType, DataPoint dataPoint) {
        System.out.println("Entro a didUpdateData");
        status = core2.getStatus();
        mac2 = device2.deviceMac;
        MainActivity.setSock2_device_globalD(deviceDescriptor);
        System.out.println("sockadree4 " + mac2);
        MainActivity.setSock2_mac_global(mac2);
        System.out.println("sockadree5 " + mac2);
        tv_mac2.setText(mac2);

        String ax = "{x:" + String.format(Locale.US, "%f", dataPoint.magnetometer[0]);
        String ay = "y:" + String.format(Locale.US, "%f", dataPoint.magnetometer[1]);
        String az = "z:" + String.format(Locale.US, "%f", dataPoint.magnetometer[2]) + "}";
        String accel = ax + ", " + ay + ", " + az;
        mag2.setText(accel);

        String mx = "{x:" + String.format(Locale.US, "%f", dataPoint.accelerometer[0]);
        String my = "y:" + String.format(Locale.US, "%f", dataPoint.accelerometer[1]);
        String mz = "z:" + String.format(Locale.US, "%f", dataPoint.accelerometer[2]) + "}";
        String magn = mx + ", " + my + ", " + mz;
        acc2.setText(magn);

        String gx = "{x:" + String.format(Locale.US, "%f", dataPoint.gyroscope[0]);
        String gy = "y:" + String.format(Locale.US, "%f", dataPoint.gyroscope[1]);
        String gz = "z:" + String.format(Locale.US, "%f", dataPoint.gyroscope[2]) + "}";
        String gyro = gx + ", " + gy + ", " + gz;
        gyr2.setText(gyro);

        String sx = "{s0:" + String.format(Locale.US, "%d", dataPoint.channels[0]);
        String sy = "s1:" + String.format(Locale.US, "%d", dataPoint.channels[1]);
        String sz = "s2:" + String.format(Locale.US, "%d", dataPoint.channels[2]) + "}";
        String ssss = sx + ", " + sy + ", " + sz;
        sss2.setText(ssss);


        // THIS IS NULL, needs to be initialized
        // processedDataLogger.logDataPoint(dataPoint.toRawDataString() + ",1,2,3");
        time_nowSock2 = System.currentTimeMillis();
        time_stamp2 = System.currentTimeMillis();
        String shortnameSock = getUintAsTimestamp(time_nowSock2);
        String timestamp = getUintAsTimestampGPS(time_stamp2);

        toplines2 = "{'us': 'USER', 'pass': 'PASSWORD', 'db': 'DBNAME', 'collection': 'scks_P1', 'mac':' " + device2.deviceMac + "', 't_end': '" + shortnameSock + "', 'appVersion': '" + MainActivity.getStringAppVersion() + "' }\n";
        json_str2 = json_str2 + "{'Device':'" + device2.deviceName + "','S0':'" + dataPoint.channels[0] + "','S1':'" + dataPoint.channels[1] + "','S2':'" + dataPoint.channels[2] + "','Ax':'" + dataPoint.accelerometer[0] + "','Ay':'" + dataPoint.accelerometer[1] + "','Az':'" + dataPoint.accelerometer[2] + "','Gx':'" + dataPoint.gyroscope[0] + "','Gy':'" + dataPoint.gyroscope[1] + "','Gz':'" + dataPoint.gyroscope[2] + "','Mx':'" + dataPoint.magnetometer[0] + "','My':'" + dataPoint.magnetometer[1] + "','Mz':'" + dataPoint.magnetometer[2] + "','Timestamp':'" + timestamp + "'}\n";
        Nrows2++;

//        Log.i("*******Nrows*******:", Integer.toString(Nrows));
        //define how many rows in one txt file
        if (Nrows2 % 750 == 0) {
            String input = toplines2 + json_str2;
            //***Create text file and write data into it***
            System.out.println("writting to text file");
            lock2.lock(); //Lock begin
            FileWriter fw = null;
            String shortname = device2.deviceName + "_" + shortnameSock;
            shortname = shortname.replaceAll(":", "");
            shortname = shortname.replaceAll("-", "");

            String txtname = folderPath2 + shortname;
            //create folder if not exist
            File folderfile = new File(folderPath2);
            if (!folderfile.exists()) {
                folderfile.mkdirs();
            }
            File txtfile = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "tmpSock2" + File.separator + "cache" + File.separator + shortname + ".txt");
            if (txtfile.exists()) {                 //check if txt file already existed
                File ftxt = new File(folderPath2);    //if exist count the number of files with this name
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
                lock2.unlock(); //Release lock
                return;
            }

            lock2.unlock();  //Release lock

        }
    }

    @Override
    public void didChangeStreaming(DeviceDescriptor deviceDescriptor, ServiceType serviceType, boolean b) {

    }

    @Override
    public void didServiceError(DeviceDescriptor deviceDescriptor, ServiceType serviceType, String s, String s1, SdkError sdkError, String s2) {
        AlertDialog.Builder builder;
        System.out.println("Entro a didServiceError");
        switch (sdkError) {
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
                    streamingService2.connect();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case STOPPED:
                streamingService2.stop();
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
