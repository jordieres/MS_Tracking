package com.upm.jgp.healthywear.ui.main.fragments.mmr;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.Subscriber;
import com.mbientlab.metawear.android.BtleService;
import com.mbientlab.metawear.builder.RouteBuilder;
import com.mbientlab.metawear.builder.RouteComponent;
import com.mbientlab.metawear.data.Acceleration;
import com.mbientlab.metawear.data.AngularVelocity;
import com.mbientlab.metawear.data.MagneticField;
import com.mbientlab.metawear.module.Accelerometer;
import com.mbientlab.metawear.module.AccelerometerBmi270;
import com.mbientlab.metawear.module.AmbientLightLtr329;
import com.mbientlab.metawear.module.AmbientLightLtr329.Gain;
import com.mbientlab.metawear.module.AmbientLightLtr329.IntegrationTime;
import com.mbientlab.metawear.module.AmbientLightLtr329.MeasurementRate;
import com.mbientlab.metawear.module.BarometerBosch;
import com.mbientlab.metawear.module.Gyro;
import com.mbientlab.metawear.module.GyroBmi160;
import com.mbientlab.metawear.module.Gyro.OutputDataRate;
import com.mbientlab.metawear.module.Gyro.Range;
import com.mbientlab.metawear.module.GyroBmi270;
import com.mbientlab.metawear.module.Logging;
import com.mbientlab.metawear.module.MagnetometerBmm150;
import com.mbientlab.metawear.module.SensorFusionBosch;
import com.mbientlab.metawear.module.SensorFusionBosch.AccRange;
import com.mbientlab.metawear.module.SensorFusionBosch.GyroRange;
import com.mbientlab.metawear.module.SensorFusionBosch.Mode;
import com.mbientlab.metawear.module.Temperature;
import com.upm.jgp.healthywear.R;
import com.upm.jgp.healthywear.ui.main.activity.MainActivity;
import com.upm.jgp.healthywear.ui.main.activity.TabWearablesActivity;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPOutputStream;

import bolts.Continuation;
import bolts.Task;

/*
 * From Android API level 23, APP has to ask for permission to write or read from local folders.
 * This required update for the previous versions of the app which was developped for old phones lower than level 23.
 * This was done in the MainActivity class
 * https://stackoverflow.com/questions/44455887/permission-denied-on-writing-to-external-storage-despite-permission
 * */

/**
 * Fragment with the communication with MMR device (mbientlab).
 * <p>
 * It periodically gets the MMR information on the background. When a few values are collected, they are compressed on a file and saved in to the phone's storage.
 * Then, they can be posted into the MongoDB by the Service RetrievedFeedTask.
 * It also handles the reconnection of the MMR for better stability (without DialogFragment).
 * The reconnection is called when the RetrievedFeedTask didn't found any new data to post into the database for around 5min
 * <p>
 * Based on MainActivity class of MetaWear-SDK-Android by mbientlab
 *
 * @author Modified by Jorge Garcia Paredes (yoryidan) + Raquel Prous
 * @version 222
 * @since 2020
 */
public class MMRSetupActivityFragment extends Fragment implements ServiceConnection {
    public interface FragmentSettings {
        BluetoothDevice getBtDevice_mmr();
    }

    private static MetaWearBoard metawear = null;
    private FragmentSettings settings;
    private String mmrMAC = null;
    private final int TYPEMMR = 2;
    private static Activity  owner_mmr;
    static Context mContext = null;

    public MMRSetupActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        owner_mmr = getActivity();
        if (!( owner_mmr instanceof FragmentSettings)) {
            throw new ClassCastException("Owning activity must implement the FragmentSettings interface");
        }

        settings = (FragmentSettings)  owner_mmr;
        owner_mmr.getApplicationContext().bindService(new Intent( owner_mmr, BtleService.class), this, Context.BIND_AUTO_CREATE);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ///< Unbind the service when the activity is destroyed
        //getActivity().getApplicationContext().unbindService(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        return inflater.inflate(R.layout.fragment_tab_mmr, container, false);
    }

    private SensorFusionBosch sensorFusion;
    private BarometerBosch baroBosch;
    //private HumidityBme280 humidity;
    private Temperature temperature;
    private Temperature.Sensor tempSensor;
    private AmbientLightLtr329 alsLtr329;
    //   private GyroBmi160 gyroBmi160;
    private GyroBmi270 gyroBmi270;
    private Gyro gyroBmi160;

    private MagnetometerBmm150 magnetometer;
    private Accelerometer accelerometer;
    //private Logging logging= metawear.getModule(Logging.class);
    private Logging logging;
    private static final String LOG_TAG = "Logging";

    private long time_now = 0;
    private int n = 1;
    private long time_now2 = 0;
    private int n2 = 1;
    private long time_now3 = 0;
    private int n3 = 1;
    //public static  String mac_address_mmr = "D2:01:2C:D9:BC:76"; //Defined in DeviceSetupActivity.java
    Lock lock = new ReentrantLock(true); //Define a lock to avoid the concurrency problem when writing data to txt file
    Lock lock2 = new ReentrantLock(true); //Define a lock to avoid the concurrency problem when writing data to txt file
    Lock lock3 = new ReentrantLock(true); //Define a lock to avoid the concurrency problem when writing data to txt file
    private String folderPathtmp1 = Environment.getExternalStorageDirectory().getPath() + File.separator + "tmp1" + File.separator + "cache" + File.separator;
    //private String folderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+ File.separator + "tmp" +  File.separator + "cache" + File.separator;
    //private String folderPath = getPublicDownloadStorageDir("cache").getPath();
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss"); //Set the format of the .txt file name.
    //DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT+0"));
    Timer timer;  //Timer for uploading data
    TimerTask timerTask; //Timer task for uploading data

    private static Boolean timerstatus = false;


    private String TEMP = "0"; //TEMPERATURE
    private String ILLUM = "0"; //Illumnition
    private String ALT = "0"; //ALTITUDE
    private String armangle = "0";
    private String location = "'lat':'-000.000000','lng':'-000.000000'";
    private Double arm;

    static TextView tv_mac = null;
    TextView tv_temp = null;
    TextView tv_illm = null;
    TextView tv_alt = null;
    TextView tv_gyr = null;
    TextView tv_arm = null;
    TextView tv_acc = null;
    TextView tv_mag = null;
    TextView tv_gps = null;
    FloatingActionButton fab = null;

    StringBuffer sb1 = new StringBuffer();
    StringBuffer sb2 = new StringBuffer();
    StringBuffer sb3 = new StringBuffer();

    public void countn() {

        if (n % 750 == 0 || n2 % 750 == 0 || n3 % 750 == 0) {
            /*
            if (n > n2) {
                if (n > n3) {
                    System.out.println("El mayor es n: " + n);
                } else {
                    System.out.println("el mayor es n3: " + n3);
                }
            } else if (n2 > n3) {
                System.out.println("el mayor es n2: " + n2);
            } else {
                System.out.println("el mayor es n3: " + n3);
            }
             */
            time_now = System.currentTimeMillis();
            String shortname = getUintAsTimestamp(time_now);  //timestam


            // PARA GYR
            System.out.println("crear fichero gyr en tmp1");
            //TODO Modify USER, PASSWORD and DBNAME from your MongoDB
            String toplinesGyr = "{'us':'USER','pass':'PASSWORD','db':'DBNAME','collection':'mmrp1_gyr'}\n" +
                    "{'mac':'" + metawear.getMacAddress() + "','appversion':'" + MainActivity.getStringAppVersion() + "'," + location + ",'alt':'" + ALT + "','t_end':'" + shortname + "','temp':'" + TEMP + "','illum':'" + ILLUM + "'}\n";
            Log.i("Toplines:", toplinesGyr);

            String inputGyr = toplinesGyr + sb3.toString();

            //***Create text file and write data into it***
            System.out.println("writting to text file Gyr p1");
            //System.out.println("count:"+count+"buffer:"+sb.toString());
            lock3.lock(); //Lock begin
            FileWriter fwGyr = null;
            //String shortname=getUintAsTimestamp(time_now);

            // 20190409
            String txtnameGyr = folderPathtmp1 + shortname + "_gr";
            //create folder if not exist
            File folderfileGyr = new File(folderPathtmp1);
            if (!folderfileGyr.exists()) {
                folderfileGyr.mkdirs();
            }

            File txtfileGyr = new File(txtnameGyr + ".txt");
            if (txtfileGyr.exists()) {                 //check if txt file already existed
                File ftxtGyr = new File(folderPathtmp1);    //if exist count the number of files with this name
                int ntxt = 0;
                for (File file : ftxtGyr.listFiles()) {
                    if (file.isFile() && (file.getName().startsWith(shortname)) && (file.getName().endsWith(".txt"))) {
                        ntxt++;
                    }
                }
                txtnameGyr = txtnameGyr + Integer.toString(ntxt);
            }
            try {
                fwGyr = new FileWriter(txtnameGyr + ".txt", true);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            BufferedWriter bufferWritterGyr = new BufferedWriter(fwGyr);
            try {
                //bufferWritter.write(sb.toString());
                bufferWritterGyr.write(inputGyr);
                bufferWritterGyr.close();
            } catch (IOException e) {
                System.out.println("Error writing to and closing file:" + e.getMessage());
                lock3.unlock(); //Release lock
                return;
            }
            lock3.unlock();  //Release lock
            sb3.delete(0, sb3.length());

            //PARA ACC
            System.out.println("crear fichero acc en tmp1");
            //TODO Modify USER, PASSWORD and DBNAME from your MongoDB
            String toplinesAcc = "{'us':'USER','pass':'PASSWORD','db':'DBNAME','collection':'mmrp1_acc'}\n" +
                    "{'mac':'" + metawear.getMacAddress() + "','appversion':'" + MainActivity.getStringAppVersion() + "'," + location + ",'alt':'" + ALT + "','t_end':'" + shortname + "','temp':'" + TEMP + "','illum':'" + ILLUM + "'}\n";
            Log.i("Toplines:", toplinesAcc);

            String inputAcc = toplinesAcc + sb1.toString();

            //***Create text file and write data into it***
            System.out.println("writting to text file Acc p1");
            //System.out.println("count:"+count+"buffer:"+sb.toString());
            lock.lock(); //Lock begin
            FileWriter fwAcc = null;
            //String shortname=getUintAsTimestamp(time_now);

            // 20190409
            String txtnameAcc = folderPathtmp1 + shortname + "_ac";
            //create folder if not exist
            File folderfileAcc = new File(folderPathtmp1);
            if (!folderfileAcc.exists()) {
                folderfileAcc.mkdirs();
            }

            File txtfileAcc = new File(txtnameAcc + ".txt");
            if (txtfileAcc.exists()) {                 //check if txt file already existed
                File ftxtAcc = new File(folderPathtmp1);    //if exist count the number of files with this name
                int ntxt = 0;
                for (File file : ftxtAcc.listFiles()) {
                    if (file.isFile() && (file.getName().startsWith(shortname)) && (file.getName().endsWith(".txt"))) {
                        ntxt++;
                    }
                }
                txtnameAcc = txtnameAcc + Integer.toString(ntxt);
            }
            try {
                fwAcc = new FileWriter(txtnameAcc + ".txt", true);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            BufferedWriter bufferWritterAcc = new BufferedWriter(fwAcc);
            try {
                //bufferWritter.write(sb.toString());
                bufferWritterAcc.write(inputAcc);
                bufferWritterAcc.close();
            } catch (IOException e) {
                System.out.println("Error writing to and closing file:" + e.getMessage());
                lock.unlock(); //Release lock
                return;
            }
            lock.unlock();  //Release lock
            sb1.delete(0, sb1.length());

            //PARA MAG
            System.out.println("crear fichero magn en tmp1");
            //TODO Modify USER, PASSWORD and DBNAME from your MongoDB
            String toplinesMag = "{'us':'USER','pass':'PASSWORD','db':'DBNAME','collection':'mmrp1_mag'}\n" +
                    "{'mac':'" + metawear.getMacAddress() + "','appversion':'" + MainActivity.getStringAppVersion() + "'," + location + ",'alt':'" + ALT + "','t_end':'" + shortname + "','temp':'" + TEMP + "','illum':'" + ILLUM + "'}\n";
            Log.i("Toplines:", toplinesMag);

            String inputMag = toplinesMag + sb2.toString();

            //***Create text file and write data into it***
            System.out.println("writting to text file Magn p1");
            //System.out.println("count:"+count+"buffer:"+sb.toString());
            lock2.lock(); //Lock begin
            FileWriter fwMag = null;
            //String shortname=getUintAsTimestamp(time_now);

            // 20190409
            String txtnameMag = folderPathtmp1 + shortname + "_mg";
            //create folder if not exist
            File folderfileMag = new File(folderPathtmp1);
            if (!folderfileMag.exists()) {
                folderfileMag.mkdirs();
            }

            File txtfileMag = new File(txtnameMag + ".txt");
            if (txtfileMag.exists()) {                 //check if txt file already existed
                File ftxtMag = new File(folderPathtmp1);    //if exist count the number of files with this name
                int ntxt = 0;
                for (File file : ftxtMag.listFiles()) {
                    if (file.isFile() && (file.getName().startsWith(shortname)) && (file.getName().endsWith(".txt"))) {
                        ntxt++;
                    }
                }
                txtnameMag = txtnameMag + Integer.toString(ntxt);
            }
            try {
                fwMag = new FileWriter(txtnameMag + ".txt", true);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            BufferedWriter bufferWritterMag = new BufferedWriter(fwMag);
            try {
                //bufferWritter.write(sb.toString());
                bufferWritterMag.write(inputMag);
                bufferWritterMag.close();
            } catch (IOException e) {
                System.out.println("Error writing to and closing file:" + e.getMessage());
                lock2.unlock(); //Release lock
                return;
            }
            lock2.unlock();  //Release lock
            sb2.delete(0, sb2.length());

            n = 1;
            n2 = 1;
            n3 = 1;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String compressTarGzip(String outputFile, List<String> inputFiles) throws IOException {

        try (OutputStream outputStream = Files.newOutputStream(Paths.get(outputFile));
             GzipCompressorOutputStream gzipOut = new GzipCompressorOutputStream(outputStream);
             TarArchiveOutputStream tarOut = new TarArchiveOutputStream(gzipOut)) {

            for (String inputFile : inputFiles) {
                File textfile = new File(inputFile.toString());
                TarArchiveEntry entry = new TarArchiveEntry(textfile);
                tarOut.putArchiveEntry(entry);
                Files.copy(Paths.get(String.valueOf(textfile)), tarOut);
                tarOut.closeArchiveEntry();
            }

            tarOut.finish();
            tarOut.close();

            File gzf = new File(outputFile);//check if the generated gzfile is larger than 1kb.

            if (gzf.length() > 1000) {
                for (String inputFile : inputFiles) {
                    File textfile = new File(inputFile.toString());
                    textfile.delete();
                }
                return outputFile;

            } else {
                // System.out.println("Borrar gzf " + gzf);
                gzf.delete();
                return null;
            }
        }
    }

    // private Timer timer = metawear.getModule(Timer.class); //timer to download data from logging
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fab = view.findViewById(R.id.fabMMRfav);

        if (mmrMAC == null) {
            fab.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_dialog_alert));
        } else {
            if (MainActivity.checkFavouriteDevice(mmrMAC)) {
                fab.setImageDrawable(getResources().getDrawable(android.R.drawable.star_big_on));
            } else {
                fab.setImageDrawable(getResources().getDrawable(android.R.drawable.btn_star_big_off));
            }
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mmrMAC != null) {
                    if (MainActivity.checkFavouriteDevice(mmrMAC)) {
                        //Take device out from the list
                        if (MainActivity.deleteFavouriteDevice(mmrMAC)) {
                            fab.setImageDrawable(getResources().getDrawable(android.R.drawable.star_big_off));
                            Snackbar.make(view, "Device removed from favourites", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    } else {
                        //Add device to the list
                        if (MainActivity.putFavouriteDevice(mmrMAC, TYPEMMR)) {
                            fab.setImageDrawable(getResources().getDrawable(android.R.drawable.btn_star_big_on));
                            Snackbar.make(view, "Device added to favourites", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    }
                }
            }
        });

        //Textview to show data
        tv_mac = view.findViewById(R.id.mmr_value_mac);     //device MAC
        tv_temp = view.findViewById(R.id.mmr_value_temp);    //Temp data
        tv_illm = view.findViewById(R.id.mmr_value_illum);    //Illum data
        tv_alt = view.findViewById(R.id.mmr_value_alt);    //Altitude data
        tv_gyr = view.findViewById(R.id.mmr_value_gyr);    //Gyroscope data
        tv_arm = view.findViewById(R.id.mmr_value_arm);    //arm angle
        tv_acc = view.findViewById(R.id.mmr_value_acc);    //acceleration
        tv_mag = view.findViewById(R.id.mmr_value_mag);    //Magnetometer data
        tv_gps = view.findViewById(R.id.mmr_value_gps);    //location gps


        view.findViewById(R.id.mmr_acc_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimer(); //start timer for zipping data
                //READ TEMP DATA
                readTEMP();
                //Log.i("*****MAC****", DeviceSetupActivity.MAC);

                //change color to green if start on click
                if (metawear.isConnected()) {
                    tv_mac.setTextColor(Color.parseColor("#FF99CC00"));
                    tv_mac.setText(mmrMAC);
                }


                //accelerometer
                if ((accelerometer = metawear.getModule(Accelerometer.class)) != null || (accelerometer = metawear.getModule(AccelerometerBmi270.class)) != null) {
                    accelerometer.acceleration().addRouteAsync(new RouteBuilder() {
                        @Override
                        public void configure(RouteComponent source) {
                            //saveLog11();
                            //saveLog();//timer task to save the log data
                            //Method to calculate timestamp https://mbientlab.com/community/discussion/1934/metahub-timestamps#latest
                            source.stream(new Subscriber() {
                                //source.log(new Subscriber() {
                                @Override
                                public void apply(Data data, Object... env) {
                                    time_now = System.currentTimeMillis();
                                    n++;
                                    System.out.println("VALOR DE N  en p1 es " + n);

                                    //Log.i("counter", String.valueOf(n));
                                    //Log.i("acc_x", String.valueOf(data.value(Acceleration.class).x()) ); //print acc.x
                                    String dataString1 = "{x:" + String.format("%.3f", (data.value(Acceleration.class).x()));
                                    String float_con_punto1 = dataString1.replace(',', '.');

                                    String dataString2 = "y:" + String.format("%.3f", (data.value(Acceleration.class).y()));
                                    String float_con_punto2 = dataString2.replace(',', '.');

                                    String dataString3 = "z:" + String.format("%.3f", (data.value(Acceleration.class).z()));
                                    String float_con_punto3 = dataString3.replace(',', '.');

                                    String dataString4 = "t:" + getUintAsTimestampGPS(time_now);
                                    String float_con_punto4 = dataString4.replace(',', '.');

                                    String dataString = float_con_punto1 + ", " + float_con_punto2 + ", " + float_con_punto3 + "}";
                                    String dataStringT = float_con_punto1 + ", " + float_con_punto2 + ", " + float_con_punto3 + ", " + float_con_punto4 + "}";

                                    arm = 180 - Math.acos(data.value(Acceleration.class).y()) * 180f / Math.PI;
                                    if (Double.isNaN(arm)) {
                                        arm = 0.0;
                                    }
                                    armangle = String.format("%.2f", arm);
                                    if (n % 25 == 0) {
                                        //Log.i("Arm Angle:", armangle);
                                        //https://stackoverflow.com/questions/47041396/only-the-original-thread-that-created-a-view-hierarchy-can-touch-its-views
                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                //if (Float.valueOf(armangle)>90){
                                                if (arm > 90.0) {
                                                    //change color to red if angle >90
                                                    tv_arm.setTextColor(Color.parseColor("#FFFF0000"));
                                                } else {
                                                    tv_arm.setTextColor(Color.parseColor("#67a098")); //ANGLE <90 change back to blue
                                                }
                                                tv_arm.setText(armangle);
                                                tv_acc.setText(dataString);
                                                //tv_temp.setText(TEMP);
                                            }
                                        });

                                    }

                                    sb1.append(dataStringT);
                                    sb1.append("\n");
                                    countn();
                                }
                            });
                        }

                    }).continueWith(new Continuation<Route, Void>() {
                        @Override
                        public Void then(Task<Route> task) throws Exception {
                            accelerometer.acceleration().start();
                            accelerometer.start();
                            //logging.start(true);  //start logging data to the flash memory
                            //saveLog();//timer task to save the log data
                            return null;
                        }
                    });
                }
                if ((magnetometer = metawear.getModule(MagnetometerBmm150.class)) != null) {
                    magnetometer.magneticField().addRouteAsync(new RouteBuilder() {
                        @Override
                        public void configure(RouteComponent source) {
                            source.stream(new Subscriber() {
                                @Override
                                public void apply(Data data, Object... env) {
                                    time_now2 = System.currentTimeMillis();
                                    n2++;
                                    System.out.println("VALOR DE N2 en p1 es " + n2);


                                    //Log.i("counter", String.valueOf(n));
                                    //Log.i("acc_x", String.valueOf(data.value(Acceleration.class).x()) ); //print acc.x
                                    String dataString1m = "{mx:" + String.format("%.5f", (data.value(MagneticField.class).x()));
                                    String float_con_punto1m = dataString1m.replace(',', '.');

                                    String dataString2m = "my:" + String.format("%.5f", (data.value(MagneticField.class).y()));
                                    String float_con_punto2m = dataString2m.replace(',', '.');

                                    String dataString3m = "mz:" + String.format("%.5f", (data.value(MagneticField.class).z()));
                                    String float_con_punto3m = dataString3m.replace(',', '.');

                                    String dataString4m = "t:" + getUintAsTimestampGPS(time_now2);
                                    String float_con_punto4m = dataString4m.replace(',', '.');

                                    String dataString = float_con_punto1m + ", " + float_con_punto2m + ", " + float_con_punto3m + "}";
                                    String dataStringT = float_con_punto1m + ", " + float_con_punto2m + ", " + float_con_punto3m + ", " + float_con_punto4m + "}";

                                    //Log.i("JSON:", dataString); //print complete acc vector

                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            //if (Float.valueOf(armangle)>90){
                                            tv_mag.setText(dataString);
                                            //tv_temp.setText(TEMP);
                                        }
                                    });

                                    sb2.append(dataStringT);
                                    sb2.append("\n");
                                    countn();
                                }
                            });
                        }
                    }).continueWith(new Continuation<Route, Void>() {
                        @Override
                        public Void then(Task<Route> task) throws Exception {
                            magnetometer.magneticField().start();
                            magnetometer.start();
                            return null;
                        }
                    });
                }
                if ((gyroBmi160 = metawear.getModule(GyroBmi160.class)) != null || (gyroBmi160 = metawear.getModule(GyroBmi270.class)) != null || (gyroBmi160 = metawear.getModule(Gyro.class)) != null) {
                    gyroBmi160.angularVelocity().addRouteAsync(new RouteBuilder() {
                        @Override
                        public void configure(RouteComponent source) {
                            source.stream(new Subscriber() {
                                @Override
                                public void apply(Data data, Object... env) {
                                    time_now3 = System.currentTimeMillis();
                                    n3++;
                                    System.out.println("VALOR DE N3 en p1 es " + n3);


                                    //Log.i("counter", String.valueOf(n));
                                    //Log.i("acc_x", String.valueOf(data.value(Acceleration.class).x()) ); //print acc.x
                                    String dataString1 = "{gx:" + String.format("%.2f", (data.value(AngularVelocity.class).x()));
                                    String float_con_punto1 = dataString1.replace(',', '.');

                                    String dataString2 = "gy:" + String.format("%.2f", (data.value(AngularVelocity.class).y()));
                                    String float_con_punto2 = dataString2.replace(',', '.');

                                    String dataString3 = "gz:" + String.format("%.2f", (data.value(AngularVelocity.class).z()));
                                    String float_con_punto3 = dataString3.replace(',', '.');

                                    String dataString4 = "t:" + getUintAsTimestampGPS(time_now3);
                                    String float_con_punto4 = dataString4.replace(',', '.');

                                    String dataString = float_con_punto1 + ", " + float_con_punto2 + ", " + float_con_punto3 + "}";
                                    String dataStringT = float_con_punto1 + ", " + float_con_punto2 + ", " + float_con_punto3 + ", " + float_con_punto4 + "}";

                                    //Log.i("JSON:", dataString); //print complete acc vector

                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            //if (Float.valueOf(armangle)>90){
                                            tv_gyr.setText(dataString);
                                            //tv_temp.setText(TEMP);
                                        }
                                    });

                                    sb3.append(dataStringT);
                                    sb3.append("\n");
                                    countn();
                                }
                            });
                        }
                    }).continueWith(new Continuation<Route, Void>() {
                        @Override
                        public Void then(Task<Route> task) throws Exception {
                            gyroBmi160.angularVelocity().start();
                            gyroBmi160.start();
                            return null;
                        }
                    });
                }

                //barometer altitude value
                if ((baroBosch = metawear.getModule(BarometerBosch.class)) != null) {
                    baroBosch.altitude().addRouteAsync(new RouteBuilder() {
                        @Override
                        public void configure(RouteComponent source) {
                            source.stream(new Subscriber() {
                                @Override
                                public void apply(Data data, Object... env) {
                                    ALT = String.format("%.2f", (data.value(Float.class)));
                                    //Log.i("MMRSetupActivityFragmen", "Altitude (m) = " + data.value(Float.class));
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            tv_alt.setText(ALT);
                                        }
                                    });
                                }
                            });
                        }
                    }).continueWith(new Continuation<Route, Void>() {
                        @Override
                        public Void then(Task<Route> task) throws Exception {
                            baroBosch.altitude().start();
                            baroBosch.start();
                            return null;
                        }
                    });
                }
/*
                //MMR doesn't have HUMIDITY sensor, Uncomment this section when necessary for other models
                // Relative humidity data is a float value from 0 to 100 percent and is represented as a forced data producer.
                humidity.value().addRouteAsync(new RouteBuilder() {
                    @Override
                    public void configure(RouteComponent source) {
                        source.stream(new Subscriber() {
                            @Override
                            public void apply(Data data, Object ... env) {
                                Log.i("MMRSetupActivityFragmen", "Humidity = " + data.value(Float.class));
                            }
                        });
                    }
                }).continueWith(new Continuation<Route, Void>() {
                    @Override
                    public Void then(Task<Route> task) throws Exception {
                        humidity.value().read();
                        return null;
                    }
                });
*/
                // Temperature data
                //****THE METHOD IN THE API DOCUMENTATION DOESNOT WORK. NOT UPDATING.  Use the readTEMP() function instead****
                // Temperature data is reported in Celsius and interepreted as a float value. It is represented as a forced data producer.
                //temperature = metawear.getModule(Temperature.class);
                //Temperature.Sensor tempSensor = temperature.findSensors(Temperature.SensorType.PRESET_THERMISTOR)[0];
/*
                tempSensor.addRouteAsync(new RouteBuilder() {
                    @Override
                    public void configure(RouteComponent source) {
                        source.stream(new Subscriber() {
                            @Override
                            public void apply(Data data, Object ... env) {
                                TEMP = String.valueOf(data.value(Float.class));
                                //Log.i("MMRSetupActivityFragmen", "Temperature (C) = " + data.value(Float.class));
                                new Handler(Looper.getMainLooper()).post(new Runnable(){
                                    @Override
                                    public void run() {
                                        tv_temp.setText(TEMP);
                                    }
                                });
                            }
                        });
                    }
                }).continueWith(new Continuation<Route, Void>() {
                    @Override
                    public Void then(Task<Route> task) throws Exception {
                        //metawear.getModule(BarometerBosch.class).start();
                        tempSensor.read();
                        return null;
                    }
                });

*/
                // Illuminance Data
                // Illuminance data is categorized as an async data producer; data is interpreted as a float value and is in units of lux (lx).
                if ((alsLtr329 = metawear.getModule(AmbientLightLtr329.class)) != null) {
                    alsLtr329.illuminance().addRouteAsync(new RouteBuilder() {
                        @Override
                        public void configure(RouteComponent source) {
                            source.stream(new Subscriber() {
                                @Override
                                public void apply(Data data, Object... env) {
                                    ILLUM = String.format(Locale.US, "%.2f", data.value(Float.class));
                                    //Log.i("MMRSetupActivityFragmen", String.format(Locale.US, "illuminance = %.3f lx", data.value(Float.class)));
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            tv_illm.setText(ILLUM);
                                        }
                                    });
                                }
                            });
                        }
                    }).continueWith(new Continuation<Route, Void>() {
                        @Override
                        public Void then(Task<Route> task) throws Exception {
                            alsLtr329.illuminance().start();
                            return null;
                        }
                    });
                }

/*
                //SENSOR FUSION
                //THIS CAN BE USED FOR ACCURATE ARM ANGLE CALCULATION
                final SensorFusionBosch sensorFusion = metawear.getModule(SensorFusionBosch.class);
                sensorFusion.quaternion().addRouteAsync(new RouteBuilder() {
                //sensorFusion.eulerAngles().addRouteAsync(new RouteBuilder() {
                    @Override
                    public void configure(RouteComponent source) {
                        source.stream(new Subscriber() {
                            @Override
                            public void apply(Data data, Object... env) {
                                //Log.i("DataFusion", "Quaternion = " + data.value(Quaternion.class));
                                float halfAngle = (float) Math.acos(data.value(Quaternion.class).w());
                                float angle =(float) (halfAngle * 360f / Math.PI);

                                //Log.i("Rotation Angle", "Angle = " + String.valueOf(angle));
                                //Log.i("DataFusion", "eulerAngles = " + data.value(EulerAngles.class));
                            }
                        });
                    }
                }).continueWith(new Continuation<Route, Void>() {
                    @Override
                    public Void then(Task<Route> task) throws Exception {
                        //sensorFusion.eulerAngles().start();
                        sensorFusion.quaternion().start();
                        sensorFusion.start();
                        return null;
                    }
                });
                */

            }
        });

        view.findViewById(R.id.mmr_acc_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (timer != null)
                    timer.cancel();
                //change color to red if stop on click
                tv_mac.setTextColor(Color.parseColor("#FFCC0000"));

                if (metawear.isConnected()) {
                    tv_mac.setText("Stopped");
                    if ((accelerometer = metawear.getModule(Accelerometer.class)) != null || (accelerometer = metawear.getModule(AccelerometerBmi270.class)) != null) {
                        accelerometer.stop();
                        accelerometer.acceleration().stop();
                    }
                    //sensorFusion.quaternion().stop();
                    //sensorFusion.stop();
                    //logging.stop(); //stop logging
                    metawear.tearDown();

                    if ((magnetometer = metawear.getModule(MagnetometerBmm150.class)) != null) {
                        magnetometer.magneticField().stop();
                        magnetometer.stop();
                    }
                    if ((baroBosch = metawear.getModule(BarometerBosch.class)) != null) {
                        baroBosch.altitude().stop();
                        baroBosch.stop();
                    }
                    if ((alsLtr329 = metawear.getModule(AmbientLightLtr329.class)) != null) {
                        alsLtr329.illuminance().stop();
                    }
                    if ((gyroBmi160 = metawear.getModule(GyroBmi160.class)) != null || (gyroBmi160 = metawear.getModule(GyroBmi270.class)) != null) {
                        gyroBmi160.angularVelocity().stop();
                        gyroBmi160.stop();
                    }
                } else {
                    tv_mac.setTextColor(Color.parseColor("#FF7E00"));
                    reconnection();
                    //tv_mac.setText("Disconnected");
                    //onServiceDisconnected();
                }
            }
        });
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        if (settings.getBtDevice_mmr() != null) {
            metawear = ((BtleService.LocalBinder) service).getMetaWearBoard(settings.getBtDevice_mmr());
            System.out.println("Metawearrrrrr before: " + metawear);
            mmrMAC = metawear.getMacAddress();
            unexpectedDisconnection();
        } else {
            //Try to get it from global variable, probably when it is the second device connected, it was not taken from the Intent...
            metawear = ((BtleService.LocalBinder) service).getMetaWearBoard(MainActivity.getMmr_device_global());
            if (metawear != null) {
                mmrMAC = metawear.getMacAddress();
            }
        }
        System.out.println("Metawearrrrrr after : " + metawear);

        //check if the device is on favourites devices when pressing start button
        if (MainActivity.checkFavouriteDevice(mmrMAC)) {
            fab.setImageDrawable(getResources().getDrawable(android.R.drawable.star_big_on));
        } else {
            fab.setImageDrawable(getResources().getDrawable(android.R.drawable.btn_star_big_off));
        }
        if ((accelerometer = metawear.getModule(AccelerometerBmi270.class)) != null) {
            accelerometer = metawear.getModule(AccelerometerBmi270.class);
            accelerometer.configure()
                    .odr(70f)       // Set sampling frequency to 25Hz, or closest valid ODR
                    .range(4f)      // Set data range to +/-4g, or closet valid range
                    .commit();
            Log.i("MMRSetupActivityFragmen", "Actual Odr = " + accelerometer.getOdr());
        }
        if ((accelerometer = metawear.getModule(Accelerometer.class)) != null) {
            accelerometer = metawear.getModule(Accelerometer.class);
            accelerometer.configure()
                    .odr(70f)       // Set sampling frequency to 25Hz, or closest valid ODR
                    .range(4f)      // Set data range to +/-4g, or closet valid range
                    .commit();
            Log.i("MMRSetupActivityFragmen", "Actual Odr = " + accelerometer.getOdr());
        }

        logging = metawear.getModule(Logging.class);
        if ((magnetometer = metawear.getModule(MagnetometerBmm150.class)) != null) {
            magnetometer = metawear.getModule(MagnetometerBmm150.class);
            magnetometer.configure().commit();
        }
        if ((gyroBmi160 = metawear.getModule(GyroBmi160.class)) != null) {
            gyroBmi160 = metawear.getModule(GyroBmi160.class);
            gyroBmi160.configure()
                    .odr(OutputDataRate.ODR_25_HZ)
                    .range(Range.FSR_2000)
                    .commit();
        }
        if ((gyroBmi270 = metawear.getModule(GyroBmi270.class)) != null) {
            gyroBmi270 = metawear.getModule(GyroBmi270.class);
            gyroBmi270.configure()
                    .odr(OutputDataRate.ODR_25_HZ)
                    .range(Range.FSR_2000)
                    .commit();
        }
        // use ndof mode with +/-16g acc range and 2000dps gyro range
        sensorFusion = metawear.getModule(SensorFusionBosch.class);
        sensorFusion.configure()
                .mode(Mode.IMU_PLUS)
                .accRange(AccRange.AR_16G)
                .gyroRange(GyroRange.GR_2000DPS);
        // .commit();

        if ((baroBosch = metawear.getModule(BarometerBosch.class)) != null) {
            baroBosch = metawear.getModule(BarometerBosch.class);
            // configure the barometer with suggested values for indoor navigation
            baroBosch.configure()
                    .filterCoeff(BarometerBosch.FilterCoeff.AVG_16)
                    .pressureOversampling(BarometerBosch.OversamplingMode.ULTRA_HIGH)
                    .standbyTime(0.5f)
                    .commit();
        }

        //humidity = metawear.getModule(HumidityBme280.class);
        // set oversampling to 16x
        //humidity.setOversampling(OversamplingMode.SETTING_16X);

        //https://mbientlab.com/androiddocs/3/temperature.html
        //temerature not updating
        temperature = metawear.getModule(Temperature.class);
        //tempSensor = temperature.findSensors(Temperature.SensorType.NRF_SOC)[0];
        //tempSensor = temperature.findSensors(Temperature.SensorType.BOSCH_ENV)[1];
        //Temperature.Sensor tempSensor = temperature.findSensors(Temperature.SensorType.PRESET_THERMISTOR)[0];
        //tempSensor = temperature.findSensors(Temperature.SensorType.EXT_THERMISTOR)[0];
        timerModule = metawear.getModule(com.mbientlab.metawear.module.Timer.class);


        //Light
        if ((alsLtr329 = metawear.getModule(AmbientLightLtr329.class)) != null) {
            alsLtr329 = metawear.getModule(AmbientLightLtr329.class);

            // Set the gain to 8x
            // Set integration time to 250ms
            // Set measurement rate to 50ms
            alsLtr329.configure()
                    .gain(Gain.LTR329_8X)
                    .integrationTime(IntegrationTime.LTR329_TIME_250MS)
                    .measurementRate(MeasurementRate.LTR329_RATE_500MS)
                    .commit();
        }
        //check if folder exist, create them if not
        File cachefoldertmp1 = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "tmp1" + File.separator + "cache");
        if (!cachefoldertmp1.exists())
            cachefoldertmp1.mkdirs();
        File backfoldertmp1 = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "tmp1" + File.separator + "backup");
        if (!backfoldertmp1.exists())
            backfoldertmp1.mkdirs();


        /*boolean writable = isExternalStorageWritable();
        if (writable){
            Log.i("ExternalStorageWritable", "********YES!!!!!");
        }else {
            Log.i("ExternalStorageWritable", "********NO!!!!!");
        }
*/
    }

    /**
     * This function jumps whenever there is an unexpectedisconnection of the MMR (it was only working when the MMR is connected in first place)
     */
    private void unexpectedDisconnection() {
        metawear.onUnexpectedDisconnect(status -> {
            //System.out.println("!!!!!!!!!UNEXPECTEDDISCONNECTION!!!!!!!");
            tv_mac.setTextColor(Color.parseColor("#FF7E00"));
            //ReconnectDialogFragment dialogFragment = ReconnectDialogFragment.newInstance(settings.getBtDevice_mmr());
            //dialogFragment.show(getActivity().getSupportFragmentManager(), RECONNECT_DIALOG_TAG);
            metawear.connectAsync().continueWithTask(task -> task.isCancelled() || !task.isFaulted() ? task : MainActivity.reconnect(metawear))
                    .continueWith((Continuation<Void, Void>) task -> {
                        if (!task.isCancelled()) {
                            getActivity().runOnUiThread(() -> {
                                tv_mac.setTextColor(Color.parseColor("#FF99CC00"));
                                //((DialogFragment) getActivity().getSupportFragmentManager().findFragmentByTag(RECONNECT_DIALOG_TAG)).dismiss();
                                //This was giving NullPointerException, but the call to this function is not necessary right now because it is currently empty
                                //((MMRSetupActivityFragment) getSupportFragmentManager().findFragmentById(R.id.mmr_setup_fragment)).reconnected();
                            });
                        } else {
                            tv_mac.setTextColor(Color.parseColor("#FFCC0000"));
                            MainActivity.setMmrConnected(false);
                            MainActivity.setMmr_device_global(null);   //Set device's MAC
                            //finish();
                        }

                        return null;
                    });
        });
    }

    /**
     * This function is called when the MMR is not finding new data for 5min
     */
    public static void reconnection() {
        //System.out.println("!!!!!!!!!RECONNECTION!!!!!!!");
        tv_mac.setTextColor(Color.parseColor("#FF7E00"));
        //ReconnectDialogFragment dialogFragment = ReconnectDialogFragment.newInstance(settings.getBtDevice_mmr());
        //dialogFragment.show(getActivity().getSupportFragmentManager(), RECONNECT_DIALOG_TAG);
        metawear.connectAsync().continueWithTask(task -> task.isCancelled() || !task.isFaulted() ? task : MainActivity.reconnect(metawear))
                .continueWith((Continuation<Void, Void>) task -> {
                    if (!task.isCancelled()) {
                        owner_mmr.runOnUiThread(() -> {
                            tv_mac.setTextColor(Color.parseColor("#FF99CC00"));
                        });
                    } else {
                        tv_mac.setTextColor(Color.parseColor("#FFCC0000"));
                        MainActivity.setMmrConnected(false);
                        MainActivity.setMmr_device_global(null);   //Set device's MAC
                        //finish();
                    }

                    return null;
                });
    }


    /* Checks if external storage is available for read and write */
  /*  public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public File getPublicDownloadStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), albumName);
        if (!file.mkdirs()) {
            Log.e(LOG_TAG, "Directory not created");
        }
        return file;
    }*/

    //setup temp sensor  // https://github.com/mbientlab/MetaWear-SampleApp-Android/blob/master/app/src/main/java/com/mbientlab/metawear/app/TemperatureFragment.java#L228
    private com.mbientlab.metawear.module.Timer timerModule;
    private com.mbientlab.metawear.module.Timer.ScheduledTask scheduledTask;

    protected void readTEMP() {
        byte gpioDataPin = 0, gpioPulldownPin = 1;
        boolean activeHigh = false;
        int TEMP_SAMPLE_PERIOD = 1000;  //UPDATE FREQUENCY
        Temperature.Sensor tempSensor = temperature.sensors()[0];
        if (tempSensor.type() == Temperature.SensorType.EXT_THERMISTOR) {
            ((Temperature.ExternalThermistor) temperature.sensors()[0]).configure(gpioDataPin, gpioPulldownPin, activeHigh);
        }
        tempSensor.addRouteAsync(source -> source.stream((data, env) -> {
            TEMP = String.valueOf(data.value(Float.class));

            //Update UI
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    //When changing the Tab the Data on the fragment is deleted, so we write it again when temp value is updated...
                    tv_mac.setTextColor(Color.parseColor("#FF99CC00"));
                    tv_mac.setText(mmrMAC);
                    tv_temp.setText(TEMP);
                }
            });

        })).continueWithTask(task -> {
            task.getResult();
            return timerModule.scheduleAsync(TEMP_SAMPLE_PERIOD, false, tempSensor::read);
        }).continueWithTask(task -> {
            scheduledTask = task.getResult();
            scheduledTask.start();
            return null;
        });
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.i("OnServiceDisconnected:", name.toString());

        MainActivity.setMmrConnected(false);
        MainActivity.setMmr_device_global(null);   //Set device's MAC

        tv_mac.setText("Disconnected");
        timer.cancel();
    }

    public static void disconnection() {
        metawear.disconnectAsync();
    }

    /**
     * Called when the app has reconnected to the board
     * Right now not called, but not necessary
     */
    public void reconnected() {
        startTimer();
    }

    /**
     * This function starts the timer to get data from the MMR and compressing it into a .gz file
     */
    //Timer for zipping data
    public void startTimer() {
        timerstatus = true;
        timer = new Timer(); //set a new Timer
        initializeTimerTask(); //initialize the TimerTask's job
        //schedule the timer, after the first 5000ms the TimerTask will run every 60000ms
        timer.schedule(timerTask, 5000, 60000);
    }

    /**
     * This function gets the location information from the mobile phone
     */
    //Function to Get the GPS information from the phone. //Reference: http://blog.csdn.net/cjjky/article/details/6557561
    private String refresh_phone_Location() {
        double latitude = 0.0;
        double longitude = 0.0;
        double altitude = 0.0;
        float accuracy = 0;
        long t_gps = 0;
        long utcTime = System.currentTimeMillis();
        //t_gps=utcTime+tmadrid.getOffset(utcTime);

        t_gps = utcTime;
        //TODO attach this fragment to Activity, when disconnecting smartband and connecting again, the getActivity method is retuning null object
        //solved by getting the LocationManager from TabWearablesActivity
        LocationManager locationManager = TabWearablesActivity.getLocationManager();
        LocationListener locationListener = new LocationListener() {
            //Provider's state triggers this function when the three states of available, temporarily unavailable and no service are directly switched
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            // This function is triggered when the Provider is enabled, such as GPS is turned on
            @Override
            public void onProviderEnabled(String provider) {
            }

            //This function is triggered when the Provider is disabled, such as GPS is turned off
            @Override
            public void onProviderDisabled(String provider) {
            }

            //This function is triggered when the coordinates change, if the Provider passes the same coordinates, it will not be triggered
            @Override
            public void onLocationChanged(Location location) {
                //TODO uncomment?
    			/*if (location != null) {
    				Log.e("Map", "Location changed : Lat: "
    				+ location.getLatitude() + " Lng: "  + location.getLongitude()+ " Alt: "
    				+ location.getAltitude()+" Acc: " + location.getAccuracy()+" t_gps:"+location.getTime());
    			}	   */
                //t_gps=location.getTime(); //timestamp
            }
        };

        //after Reconnection it was jumping an error here: java.lang.NullPointerException
        //SOLVED Permission checked when app is opened
        if (MainActivity.isLocationPermissionsGranted()) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300, 0, locationListener);
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    altitude = location.getAltitude();
                    accuracy = location.getAccuracy();
                }
            } else {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 300, 0, locationListener);
                Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null) {
                    latitude = location.getLatitude(); //经度
                    longitude = location.getLongitude(); //纬度
                    altitude = location.getAltitude(); //海拔
                    accuracy = location.getAccuracy(); //精度, in meters
                }
            }
        }

        //String location="'lat':'"+String.format("%.6f",(latitude))+"','lng':'"+String.format("%.6f",(longitude)) +"','t_gps':'"+getUintAsTimestampGPS(t_gps)+"'";
        location = "'lat':'" + String.format("%.6f", (latitude)) + "','lng':'" + String.format("%.6f", (longitude)) + "'";
        //Toast.makeText(this, location, Toast.LENGTH_SHORT).show();
        return location;
    }

    /**
     * This task periodically stores the incoming data into files
     * The information is changed on the UI often
     */
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void run() {
                //***Get file list in the folder // stackoverflow.com/questions/8646984/how-to-list-files-in-an-android-directory
                String folderpathtmp1 = Environment.getExternalStorageDirectory().getPath() + File.separator + "tmp1" + File.separator + "cache";
                String bkpfoldertmp1 = Environment.getExternalStorageDirectory().getPath() + File.separator + "tmp1" + File.separator + "backup";

                try {
                    File filegz[] = findergz(folderpathtmp1);   //get all the .gz file
                    System.out.println("antes");
                    if (filegz != null && filegz.length > 0) {
                        System.out.println("despues");
                        for (int j = 0; j < filegz.length; j++) {
                            String datapathgz = bkpfoldertmp1 + File.separator + filegz[j].getName();
                            File bkpfile = new File(datapathgz);
                            //new RetrieveFeedTask_mmr().execute(datapathgz);
                            filegz[j].renameTo(bkpfile);
                        }
                    } else {
                        try {
                            File file[] = finder(folderpathtmp1);  //get all the .txt file
                            List<String> tres = new ArrayList<String>();
                            List<String> lista = new ArrayList<String>();
                            if (file != null && file.length > 0) {
                                for (int i = 0; i < file.length; i++) //Send all the files to the server one by one.
                                {
                                    String com_fich = file[i].getName().substring(0, 1);
                                    if (com_fich.charAt(0) != '.') {
                                        tres.add(file[i].getName().substring(0, 15));
                                        lista.add(folderpathtmp1 + File.separator + file[i].getName());
                                        if (file[i + 1].exists() && file[i].getName().substring(0, 15).matches(file[i + 1].getName().substring(0, 15))) {
                                            tres.add(file[i + 1].getName().substring(0, 15));
                                            if (!lista.contains(file[i + 1])) {
                                                lista.add(folderpathtmp1 + File.separator + file[i + 1].getName());
                                            }
                                            for (int x = i + 2; x < file.length; x++) {

                                                if (file[i].getName().substring(0, 15).matches(file[x].getName().substring(0, 15))) {
                                                    tres.add(file[x].getName().substring(0, 15));
                                                    lista.add(folderpathtmp1 + File.separator + file[x].getName());
                                                    if (tres.size() == 3 || lista.size() == 3) {

                                                        String despath0 = folderpathtmp1 + File.separator + file[i].getName().substring(0, 15) + "-P1.gz";

                                                        try {
                                                            System.out.println("ANTES DEL COMPRESS");
                                                            File zip = new File(compressTarGzip(despath0, lista));
                                                            System.out.println("despues DEL COMPRESS");

                                                            String despath = bkpfoldertmp1 + File.separator + zip.getName();
                                                            File newzip = new File(despath);
                                                            zip.renameTo(newzip);


                                                        } catch (Exception e) {

                                                        }
                                                        tres.clear();
                                                        lista.clear();
                                                        //cuando este aqui que vuelva a i porq si no recorre todo x y todo j
                                                    }
                                                    //if lista tiene3 elementos j==file.lenght
                                                } else if (x == file.length - 1 && tres.size() != 3) {
                                                    //BORRAR LISTA?
                                                    System.out.println("entro al else if");
                                                    tres.clear();
                                                    lista.clear();
                                                    //cuando este aqui que vuelva a i porq si no recorre todo  j
                                                }
                                            }
                                        } else {
                                            for (int j = i + 2; j < file.length; j++) {
                                                if (file[i].getName().substring(0, 15).matches(file[j].getName().substring(0, 15))) {
                                                    tres.add(file[j].getName().substring(0, 15));
                                                    lista.add(folderpathtmp1 + File.separator + file[j].getName());

                                                    for (int x = j + 1; x < file.length; x++) {
                                                        if (file[i].getName().substring(0, 15).matches(file[x].getName().substring(0, 15))) {
                                                            tres.add(file[x].getName().substring(0, 15));
                                                            lista.add(folderpathtmp1 + File.separator + file[x].getName());
                                                            if (tres.size() == 3 || lista.size() == 3) {
                                                                String despath0 = folderpathtmp1 + File.separator + file[i].getName().substring(0, 15) + "-P1.gz";

                                                                try {
                                                                    System.out.println("ANTES DEL COMPRESS");
                                                                    File zip = new File(compressTarGzip(despath0, lista));
                                                                    System.out.println("despues DEL COMPRESS");

                                                                    String despath = bkpfoldertmp1 + File.separator + zip.getName();
                                                                    File newzip = new File(despath);
                                                                    zip.renameTo(newzip);
                                                                } catch (Exception e) {

                                                                }
                                                                tres.clear();
                                                                lista.clear();
                                                                //cuando este aqui que vuelva a i porq si no recorre todo x y todo j
                                                            }
                                                            //if lista tiene3 elementos j==file.lenght
                                                        } else if (x == file.length - 1 && tres.size() != 3) {
                                                            //BORRAR LISTA?
                                                            tres.clear();
                                                            lista.clear();
                                                            //cuando este aqui que vuelva a i porq si no recorre todo  j
                                                        }
                                                    }
                                                } else {
                                                    if (j == file.length - 1 && tres.size() != 3) {
                                                        //BORRAR LISTA?
                                                        tres.clear();
                                                        lista.clear();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            Log.d("Files", e.getLocalizedMessage());
                        }
                    }

                    /*
                    String[] strAr1=new String[] {"hola", "hola", "holi","hole","hola","holo","hole", "holo", "holi", "holi", "hole"};
                    List<String> tres = new ArrayList<String>();

                    if (strAr1 != null && strAr1.length > 0) {
                        System.out.println("tamaño de str: " + strAr1.length);
                        for (int i = 0; i < strAr1.length; i++) //Send all the files to the server one by one.
                        {
                            System.out.println("valor de i es: " + i);
                            System.out.println("valor de string i es: " + strAr1[i]);
                            tres.add(strAr1[i].substring(0, 4));
                            System.out.println("anadido + " + strAr1[i]);
                            if (strAr1[i + 1]!=null && strAr1[i].substring(0, 4).matches(strAr1[i + 1].substring(0, 4))) {
                                tres.add(strAr1[i+1].substring(0, 4));
                                System.out.println("anadido + " + strAr1[i+1]);
                                for (int x = i+2; x < strAr1.length; x++) {
                                    System.out.println("valor de x es: " + x);
                                    System.out.println("valor de string x es: " + strAr1[x]);
                                    if (strAr1[i].substring(0, 4).matches(strAr1[x].substring(0, 4))) {
                                        tres.add(strAr1[x].substring(0, 4));
                                        System.out.println("anadido x + " + strAr1[x]);
                                        System.out.println("tres vale despues x + " + tres);
                                        if (tres.size() == 3) {
                                            System.out.println("tres vale + " + tres);
                                            tres.clear();
                                            strAr1[i] = "null";
                                            strAr1[i+1] = "null";
                                            strAr1[x] = "null";
                                            //cuando este aqui que vuelva a i porq si no recorre todo x y todo j
                                        }
                                        //if lista tiene3 elementos j==file.lenght
                                    } else if (x == strAr1.length-1 && tres.size() != 3){
                                        //BORRAR LISTA?
                                        System.out.println("entro al else if");
                                        tres.clear();
                                        //cuando este aqui que vuelva a i porq si no recorre todo  j
                                    }
                                }
                            } else {
                                for (int j = i+2; j < strAr1.length; j++) {
                                    System.out.println("valor de j es: " + j);
                                    System.out.println("valor de string j es: " + strAr1[j]);
                                    if (strAr1[i].substring(0, 4).matches(strAr1[j].substring(0, 4))) {
                                        tres.add(strAr1[j].substring(0, 4));
                                        System.out.println("anadido + " + strAr1[j]);
                                        for (int x = j + 1; x < strAr1.length; x++) {
                                            System.out.println("valor de x es: " + x);
                                            System.out.println("valor de string x es: " + strAr1[x]);
                                            System.out.println("valor tres + " + tres);
                                            System.out.println("tamaño tres + " + strAr1.length);
                                            if (strAr1[i].substring(0, 4).matches(strAr1[x].substring(0, 4))) {
                                                tres.add(strAr1[x].substring(0, 4));
                                                System.out.println("anadido x + " + strAr1[x]);
                                                System.out.println("tres vale despues x2 + " + tres);
                                                if (tres.size() == 3 ) {
                                                    System.out.println("tres vale + " + tres);

                                                    tres.clear();
                                                    strAr1[i] = "null";
                                                    strAr1[j] = "null";
                                                    strAr1[x] = "null";
                                                    //cuando este aqui que vuelva a i porq si no recorre todo x y todo j
                                                }
                                                //if lista tiene3 elementos j==file.lenght
                                            } else if (x == strAr1.length-1 && tres.size() != 3){
                                                //BORRAR LISTA?
                                                System.out.println("entro al else if");
                                                tres.clear();
                                                //cuando este aqui que vuelva a i porq si no recorre todo  j
                                            }
                                        }
                                        //if lista tiene3 elementos j==file.lenght
                                    }else {

                                    }

                                }
                            }
                        }
                    }
                    */
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.d("Files", e.getLocalizedMessage());
                }
            }
        };
    }

    //Function to check a string is Null or Empty
    public static boolean isNullOrEmpty(String str) {
        if (str != null && !str.trim().isEmpty())
            return false;
        return true;
    }


    //Gzip a text file http://examples.javacodegeeks.com/core-java/io/fileinputstream/compress-a-file-in-gzip-format-in-java/
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

    //find all the .txt files in a folder. http://stackoverflow.com/questions/1384947/java-find-txt-files-in-specified-folder
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

    //Change the date format
    private String getUintAsTimestamp(Long uint) {
        //return DATE_FORMAT.format(new Date(uint.longValue() * 1000L)).toString();
        //uint=uint+tmadrid.getOffset(uint);
        //DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT+1"));
        return DATE_FORMAT.format(new Date(uint)).toString();
    }

    private static final DateFormat DATE_FORMAT_GPS = new SimpleDateFormat("yyyyMMddHHmmssSSS"); //Set the format of the .txt file name.

    private String getUintAsTimestampGPS(Long uint) {
        //return DATE_FORMAT.format(new Date(uint.longValue() * 1000L)).toString();
        //DATE_FORMAT_GPS.setTimeZone(TimeZone.getTimeZone("GMT+0")); //set timezone*******?
        //uint=uint+tmadrid.getOffset(uint); //added in the function
        return DATE_FORMAT_GPS.format(new Date(uint)).toString();
    }


}