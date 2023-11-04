package com.upm.jgp.healthywear.ui.main.fragments.socks;

import static android.util.Log.VERBOSE;
import static io.sensoria.sdk.SensoriaSdk.checkRequiredPermissions;
import static io.sensoria.sdk.enums.ServiceType.SENSORIA_STREAMING_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.upm.jgp.healthywear.R;
import com.upm.jgp.healthywear.ui.main.activity.MainActivity;
import com.upm.jgp.healthywear.ui.main.activity.TabWearablesActivity;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import io.sensoria.sdk.Core;
import io.sensoria.sdk.DeviceDescriptor;
import io.sensoria.sdk.Scanner;
import io.sensoria.sdk.SensoriaSdk;
import io.sensoria.sdk.enums.CoreEvent;
import io.sensoria.sdk.enums.ScannerEvent;
import io.sensoria.sdk.enums.SdkError;
import io.sensoria.sdk.enums.ServiceType;
import io.sensoria.sdk.interfaces.ICoreCallback;
import io.sensoria.sdk.interfaces.IPermissionsCallback;
import io.sensoria.sdk.interfaces.IScannerCallback;
import io.sensoria.sdk.services.BaseService;
import io.sensoria.sdk.services.StreamingService;


public class CoreTestActivity2 extends AppCompatActivity implements IPermissionsCallback, ICoreCallback, IScannerCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    Scanner scanner2;
    Core core2;
    String mac2;
    final String local_device_type2 = "Sock2";    //MMR
    private static final int REQUEST_START_APP = 1;

    private final int MY_PERMISSIONS = 1;
    private String selectedCode;
    private String selectedMac;
    private int selectedPosition = 0;
    private Button btnGenericAccessService;
    private Button btnGenericAttributeService;
    private Button btnDeviceInformationService;
    private Button btnBatteryService;
    private Button btnHealthThermometerService;
    private Button btnSensoriaCoreStreamingService;
    private Button btnSensoriaCoreControlPointService;
    private Button btnSensoriaCoreCustomConfiguration;
    private Button btnSensoriaCoreSmokeTest;
    private Button btnConnect;
    private Button btnStopScan;
    private Button btnStartScan;
    private Button btnDisconnect;
    private Button btnDFU;

    StreamingService stream2;

    private String incoming_device_type = "Sock2";

    private boolean mSignalLost = false;
    private DeviceDescriptor deviceDiscovered2;

    private Spinner deviceListSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_core_test);

        setContentView(R.layout.activity_scan_sock2);

//////Toolbar Settings//////
        Toolbar toolbar = (Toolbar) findViewById(R.id.myToolbar_scan);
        toolbar.setTitle(getString(R.string.app_name)); //setting the title

        setSupportActionBar(toolbar);   //placing toolbar in place of actionbar

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //////Toolbar Settings//////
        if (!SensoriaSdk.isInitialized()) {
            SensoriaSdk.initialize(false, true, VERBOSE, true);
        }

        checkRequiredPermissions(this, this);
        scanner2 = new Scanner(this);
        core2 = new Core(this);


        btnGenericAccessService = findViewById(R.id.buttonGenericAccessServiceStart2);
        btnGenericAccessService.setEnabled(false);
        btnGenericAttributeService = findViewById(R.id.buttonGenericAttributeServiceStart2);
        btnGenericAttributeService.setEnabled(false);
        btnDeviceInformationService = findViewById(R.id.buttonDeviceInformationServiceStart2);
        btnDeviceInformationService.setEnabled(false);
        btnBatteryService = findViewById(R.id.buttonBatteryServiceStart2);
        btnBatteryService.setEnabled(false);
        btnHealthThermometerService = findViewById(R.id.buttonTemperatureServiceStart2);
        btnHealthThermometerService.setEnabled(false);
        btnSensoriaCoreStreamingService = findViewById(R.id.buttonSensoriaCoreStreamingServiceStartP2);
        btnSensoriaCoreStreamingService.setEnabled(false);
        btnSensoriaCoreControlPointService = findViewById(R.id.buttonSensoriaCoreControlPointServiceStart2);
        btnSensoriaCoreControlPointService.setEnabled(false);
        btnSensoriaCoreCustomConfiguration = findViewById(R.id.buttonSensoriaCoreCustomConfigStart2);
        btnSensoriaCoreCustomConfiguration.setEnabled(false);
        btnSensoriaCoreSmokeTest = findViewById(R.id.buttonSensoriaCoreSmokeTest2);
        btnSensoriaCoreSmokeTest.setEnabled(false);
        btnDFU = findViewById(R.id.buttonDFU2);
        btnConnect = findViewById(R.id.buttonConnect2);
        btnConnect.setEnabled(false);
        btnStopScan = findViewById(R.id.buttonStopScan2);
        btnStopScan.setEnabled(false);
        btnStartScan = findViewById(R.id.buttonStartScan2);
        btnStartScan.setEnabled(true);
        btnDisconnect = findViewById(R.id.buttonDisconnect2);
        btnDisconnect.setEnabled(false);
        deviceListSpinner = findViewById(R.id.spinnerDeviceCode2);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        core2.dispose();
//        core2.dispose();
        super.onDestroy();
    }

    public void startScan2(View view) {
        scanner2.startScan(5000);
        btnStopScan.setEnabled(true);
        btnStartScan.setEnabled(false);
    }

    public void stopScan2(View view) {
        scanner2.stopScan();
        btnStopScan.setEnabled(false);
        btnStartScan.setEnabled(true);
    }

    public void connect2(View view) {
        core2.connect(deviceDiscovered2);
        MainActivity.setSock2Connected2(core2.isConnected());
        btnSensoriaCoreStreamingService.setEnabled(true);
    }

    public void disconnect2(View view) {
        core2.disconnect();
//        core2.disconnect();
    }

    public void startGenericAttributeService2(View view) {
        Intent intent = new Intent(this, GenericAttributeServiceActivity2.class);
        intent.putExtra("SAService", deviceDiscovered2);
        startActivity(intent);

    }

    public void startDeviceInformationService2(View view) {
        Intent intent = new Intent(this, DeviceInformationServiceActivity2.class);
        intent.putExtra("SAService", deviceDiscovered2);
        startActivity(intent);

    }

    public void startSensoriaStreamingService2(View view) {
        if (stream2 != null) {
            MainActivity.setSock2_mac_global(mac2);
            MainActivity.setSock2_stream_global(stream2);


            System.out.println("startSensoriaStreamingService2 1");
            Intent navActivityIntent2 = new Intent(CoreTestActivity2.this, TabWearablesActivity.class);
            System.out.println("startSensoriaStreamingService2 2");
            navActivityIntent2.putExtra(TabWearablesActivity.DEVICE_TYPE, local_device_type2);
            System.out.println("startSensoriaStreamingService2 3");
            navActivityIntent2.putExtra(TabWearablesActivity.EXTRA_BT_DEVICE5, stream2);
            System.out.println("startSensoriaStreamingService2 4");
            navActivityIntent2.putExtra(TabWearablesActivity.EXTRA_BT_DEVICE6, deviceDiscovered2);
            System.out.println("startSensoriaStreamingService2 5");
            navActivityIntent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            System.out.println("startSensoriaStreamingService2 6");
            startActivityForResult(navActivityIntent2, REQUEST_START_APP);

        }
    }

    public void didDeviceScanning(Scanner scanner) {
        btnStartScan.setEnabled(false);
        btnStopScan.setEnabled(true);
        btnConnect.setEnabled(false);
        MainActivity.setSock2Connected2(core2.isConnected());
    }


    public void didDeviceDiscoveredUpdated(Core core2, DeviceDescriptor device, boolean hasDisappeared) {
//        sdkLog(WARN, "SensoriaLibrary", "Device Updated: " + device.deviceName + ", disappeared: " + hasDisappeared);
        mac2 = device.deviceMac;
        MainActivity.setSock2_mac_global(mac2);
        //    stream = new StreamingService(mac);
        MainActivity.setSock2_stream_global(stream2);
        deviceDiscovered2 = device;
        MainActivity.setSock2_device_globalD(deviceDiscovered2);
        MainActivity.setSock2_Core_global(core2);
    }

    public void didServicesDiscovered(Core coreM2, DeviceDescriptor device) {
//        sdkLog(WARN, "SensoriaLibrary", "Service Discovered");

        mac2 = device.deviceMac;
        MainActivity.setSock2_mac_global(mac2);
        stream2 = (StreamingService) core2.getServiceByType(SENSORIA_STREAMING_SERVICE);
        //   stream = new StreamingService(mac);
        MainActivity.setSock2_stream_global(stream2);
        deviceDiscovered2 = device;
        MainActivity.setSock2_device_globalD(deviceDiscovered2);
        MainActivity.setSock2_Core_global(coreM2);
        if (stream2 != null) {
            btnSensoriaCoreStreamingService.setEnabled(true);
        }
    }

    private boolean enableButton(BaseService service, Button button, String serviceName) {
        return (((service.getServiceName().compareTo(serviceName) == 0) &&
                button.getVisibility() == View.VISIBLE) || button.isEnabled());
    }

    public void didInitialized(Core core2) {
    }

    public void didConnecting(Core core2, DeviceDescriptor device) {
        btnConnect.setEnabled(false);
        MainActivity.setSock2Connected2(core2.isConnected());
        mac2 = device.deviceMac;
        MainActivity.setSock2_mac_global(mac2);
        //   stream = new StreamingService(mac);
        MainActivity.setSock2_stream_global(stream2);
        btnStartScan.setEnabled(false);
        deviceDiscovered2 = device;
        MainActivity.setSock2_device_globalD(deviceDiscovered2);
        MainActivity.setSock2_Core_global(core2);

    }

    public void didConnect(Core core2, DeviceDescriptor device) {
        btnDisconnect.setEnabled(true);
        btnConnect.setEnabled(false);
        mac2 = device.deviceMac;
        MainActivity.setSock2_mac_global(mac2);
        //    stream = new StreamingService(mac);
        MainActivity.setSock2_stream_global(stream2);
        MainActivity.setSock2Connected2(core2.isConnected());
        deviceDiscovered2 = device;
        MainActivity.setSock2_device_globalD(deviceDiscovered2);
        MainActivity.setSock2_Core_global(core2);
        if (mSignalLost) {
            mSignalLost = false;
            didServicesDiscovered(core2, device);
        } else {
            btnGenericAccessService.setEnabled(true);
            btnGenericAttributeService.setEnabled(true);
            btnDeviceInformationService.setEnabled(true);
            btnBatteryService.setEnabled(true);
            btnHealthThermometerService.setEnabled(true);
            btnSensoriaCoreStreamingService.setEnabled(true);
            btnSensoriaCoreControlPointService.setEnabled(true);
            btnSensoriaCoreCustomConfiguration.setEnabled(true);
            btnSensoriaCoreSmokeTest.setEnabled(true);
          /*  if (coreScanner.getSDVersion() >= 5)
                btnDFU.setEnabled(true);
            else
                btnDFU.setEnabled(false);

           */
        }
    }

    public void didDisconnect(Core core2, DeviceDescriptor device) {
        btnConnect.setEnabled(true);
        MainActivity.setSock2Connected2(core2.isConnected());
        mac2 = device.deviceMac;
        MainActivity.setSock2_mac_global(mac2);
        //   stream = new StreamingService(mac);
        MainActivity.setSock2_stream_global(stream2);
        deviceDiscovered2 = device;
        MainActivity.setSock2_device_globalD(deviceDiscovered2);
        MainActivity.setSock2_Core_global(core2);
        btnStartScan.setEnabled(true);
        btnDisconnect.setEnabled(false);
        btnGenericAccessService.setEnabled(false);
        btnGenericAttributeService.setEnabled(false);
        btnDeviceInformationService.setEnabled(false);
        btnBatteryService.setEnabled(false);
        btnHealthThermometerService.setEnabled(false);
        btnSensoriaCoreStreamingService.setEnabled(true);
        btnSensoriaCoreControlPointService.setEnabled(false);
        btnSensoriaCoreCustomConfiguration.setEnabled(false);
        btnSensoriaCoreSmokeTest.setEnabled(false);
      /*  if (coreScanner.getSDVersion() >= 5)
            btnDFU.setEnabled(false);

       */
    }

    public void didSignalLost(Core core2, DeviceDescriptor device) {
        mSignalLost = true;
        btnStartScan.setEnabled(true);
        btnStopScan.setEnabled(false);
        btnConnect.setEnabled(false);
        mac2 = device.deviceMac;
        MainActivity.setSock2_mac_global(mac2);
        //    stream = new StreamingService(mac);
        MainActivity.setSock2_stream_global(stream2);
        deviceDiscovered2 = device;
        MainActivity.setSock2_device_globalD(deviceDiscovered2);
        MainActivity.setSock2_Core_global(core2);
        MainActivity.setSock2Connected2(core2.isConnected());
        btnDisconnect.setEnabled(false);
        btnGenericAccessService.setEnabled(false);
        btnGenericAttributeService.setEnabled(false);
        btnDeviceInformationService.setEnabled(false);
        btnBatteryService.setEnabled(false);
        btnHealthThermometerService.setEnabled(false);
        btnSensoriaCoreStreamingService.setEnabled(true);
        btnSensoriaCoreControlPointService.setEnabled(false);
        btnSensoriaCoreCustomConfiguration.setEnabled(false);
        btnSensoriaCoreSmokeTest.setEnabled(false);
    }


    public void didDeviceError(Core core2, DeviceDescriptor device, SdkError errorCode) {
        AlertDialog.Builder builder;

        mac2 = device.deviceMac;
        MainActivity.setSock2_mac_global(mac2);
        stream2 = new StreamingService(mac2);
        MainActivity.setSock2_stream_global(stream2);
        deviceDiscovered2 = device;
        MainActivity.setSock2_device_globalD(deviceDiscovered2);
        MainActivity.setSock2_Core_global(core2);

        switch (errorCode) {
            case ERROR_INVALID_STATE:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_NOT_IN_STATE_DISCONNECTED))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                break;

            case ERROR_BLUETOOTH_NOT_SUPPORTED:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_BLUETOOTH_NOT_SUPPORTED))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                break;

            case ERROR_BLUETOOTH_DISABLED:
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent intentBtEnabled = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    // The REQUEST_ENABLE_BT constant passed to startActivityForResult() is a
                    // locally defined integer (which must be greater than 0), that the system
                    // passes back to you in your onActivityResult()
                    // implementation as the requestCode parameter.
                    int REQUEST_ENABLE_BT = 1;
                    startActivityForResult(intentBtEnabled, REQUEST_ENABLE_BT);
                }
                break;

            case ERROR_BLUETOOTH_LE_NOT_SUPPORTED:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_BLUETOOTH_LE_NOT_SUPPORTED))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                break;

            case ERROR_SCAN_NOT_STARTED:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_SCAN_NOT_STARTED))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                break;

            case ERROR_NO_DEVICE_FOUND:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_NO_DEVICE_FOUND))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                break;

            case ERROR_NO_CONNECTION:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_NO_CONNECTION))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                break;

            case ERROR_INVALID_MAC_ADDRESS:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_INVALID_MAC_ADDRESS))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                break;

            case ERROR_INVALID_OR_NULL_CALLBACK:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_INVALID_OR_NULL_CALLBACK))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                break;

            case ERROR_DEVICE_NOT_STARTED:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_DEVICE_NOT_STARTED))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                break;

            case ERROR_SCAN_ALREADY_STARTED:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_SCAN_ALREADY_STARTED))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                break;

            case ERROR_SCAN_APPLICATION_REGISTRATION_FAILED:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string
                                .ERROR_SCAN_APPLICATION_REGISTRATION_FAILED))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                break;

            case ERROR_FEATURE_NOT_IMPLEMENTED:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_FEATURE_NOT_IMPLEMENTED))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                break;

            case ERROR_SCAN_INTERNAL_ERROR:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_SCAN_INTERNAL_ERROR))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                break;

            case ERROR_ILLEGAL_ARGUMENT_EXCEPTION:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_ILLEGAL_ARGUMENT_EXCEPTION))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                break;

            case ERROR_NULL_OR_NOT_IMPLEMENTED_SERVICE_INTERFACE:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_NULL_OR_NOT_IMPLEMENTED_SERVICE_INTERFACE))
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
                        .setMessage(this.getString(R.string.ERROR_FAILURE))
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
    public void didPermissionsGranted(String[] permissions) {
        Toast.makeText(CoreTestActivity2.this, this.getString(R.string.PERMISSION_GRANTED), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void didPermissionsToBeRequested(String[] permissions) {
        ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS);
    }

    @Override
    public void didPermissionsOptionalRationaleRequested(final String[] permissions) {
        // WRITE_EXTERNAL_STORAGE is needed for debugging - if we don't receive permission we ignore
        // Only LOCATION is mandatory
        boolean found = false;

        for (String permission : permissions) {
            if (permission == Manifest.permission.ACCESS_FINE_LOCATION) {
                found = true;
                break;
            }
        }

        if (found) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(this.getString((R.string.PERMISSION_TITLE)))
                    .setMessage(this.getString(R.string.RATIONALE_LOCATION))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            didPermissionsToBeRequested(permissions);
                        }
                    });
            builder.create().show();
        }
    }

    @Override
    public void didPermissionsDisabled(String[] permissions) {
        // WRITE_EXTERNAL_STORAGE is needed for debugging - if we don't receive permission we ignore
        // Only LOCATION is mandatory
        boolean found = false;

        for (String permission : permissions) {
            if (permission == Manifest.permission.ACCESS_FINE_LOCATION) {
                found = true;
                break;
            }
        }

        if (found) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(this.getString((R.string.PERMISSION_TITLE)))
                    .setMessage(this.getString(R.string.PERMISSION_DISABLED_LOCATION))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });

            builder.create().show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_CANCELED) {
                finish();
            }
        } else if (requestCode == 2) {
            // requestCode = 2 for SensoriaCoreCustomConfigurationActivity call.
            // resultCode = 2 if Core was intentionally rebooted from SensoriaCoreCustomConfigurationActivity.
            if (resultCode == 2) {
                // the value for key "rebootStatus" is only true if Core was intentionally rebooted.
                String rebootResult = data.getStringExtra("rebootStatus");
                if (rebootResult.equals("true")) {
                    // calling dispose method will prevent the Core from automatically reconnecting.
                    core2.dispose();
                    // clearing discovered devices list since its data is no longer valid.
                    deviceListSpinner.setAdapter(null);
                    // Re-initializing objects, since onCreate is not executed when the activity resumes.
                    core2 = new Core(this);
                    scanner2 = new Scanner(this);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length <= 0) {
            return;
        }

        // We can continue only if the LOCATION is granted
        for (int i = 0; i < grantResults.length; i++) {
            if (permissions[i] == Manifest.permission.ACCESS_FINE_LOCATION) {
                boolean granted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                Toast.makeText(CoreTestActivity2.this, this.getString(granted ? R.string.PERMISSION_GRANTED_LOCATION : R.string.PERMISSION_DENIED_LOCATION), Toast.LENGTH_SHORT).show();
                if (!granted) {
                    finish();
                    break;
                }
            }
        }
    }

    @Override
    public void didCoreError(Core core2, DeviceDescriptor device, SdkError errorCode) {
        AlertDialog.Builder builder;

         mac2 = device.deviceMac;
        MainActivity.setSock2_mac_global(mac2);

        switch (errorCode) {
            case ERROR_INVALID_STATE:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_NOT_IN_STATE_DISCONNECTED))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                break;

            case ERROR_BLUETOOTH_NOT_SUPPORTED:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_BLUETOOTH_NOT_SUPPORTED))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                break;

            case ERROR_BLUETOOTH_DISABLED:
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent intentBtEnabled = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    // The REQUEST_ENABLE_BT constant passed to startActivityForResult() is a
                    // locally defined integer (which must be greater than 0), that the system
                    // passes back to you in your onActivityResult()
                    // implementation as the requestCode parameter.
                    int REQUEST_ENABLE_BT = 1;
                    startActivityForResult(intentBtEnabled, REQUEST_ENABLE_BT);
                }
                break;

            case ERROR_BLUETOOTH_LE_NOT_SUPPORTED:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_BLUETOOTH_LE_NOT_SUPPORTED))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                break;

            case ERROR_SCAN_NOT_STARTED:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_SCAN_NOT_STARTED))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                break;

            case ERROR_NO_DEVICE_FOUND:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_NO_DEVICE_FOUND))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                break;

            case ERROR_NO_CONNECTION:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_NO_CONNECTION))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                break;

            case ERROR_INVALID_MAC_ADDRESS:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_INVALID_MAC_ADDRESS))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                break;

            case ERROR_INVALID_OR_NULL_CALLBACK:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_INVALID_OR_NULL_CALLBACK))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                break;

            case ERROR_DEVICE_NOT_STARTED:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_DEVICE_NOT_STARTED))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                break;

            case ERROR_SCAN_ALREADY_STARTED:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_SCAN_ALREADY_STARTED))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                break;

            case ERROR_SCAN_APPLICATION_REGISTRATION_FAILED:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string
                                .ERROR_SCAN_APPLICATION_REGISTRATION_FAILED))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                break;

            case ERROR_FEATURE_NOT_IMPLEMENTED:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_FEATURE_NOT_IMPLEMENTED))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                break;

            case ERROR_SCAN_INTERNAL_ERROR:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_SCAN_INTERNAL_ERROR))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                break;

            case ERROR_ILLEGAL_ARGUMENT_EXCEPTION:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_ILLEGAL_ARGUMENT_EXCEPTION))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                builder.create().show();
                break;

            case ERROR_NULL_OR_NOT_IMPLEMENTED_SERVICE_INTERFACE:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString((R.string.ERROR_TITLE)))
                        .setMessage(this.getString(R.string.ERROR_NULL_OR_NOT_IMPLEMENTED_SERVICE_INTERFACE))
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
                        .setMessage(this.getString(R.string.ERROR_FAILURE))
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
    public void didServiceStatusChange(Core core2, DeviceDescriptor device, Map<ServiceType, Boolean> serviceStatus) {
        boolean isConnected = core2.isConnected();
        btnDisconnect.setEnabled(isConnected);
        btnConnect.setEnabled(!isConnected);
        mac2 = device.deviceMac;
        MainActivity.setSock2_mac_global(mac2);
        if (serviceStatus.values().contains(true)) {
            btnStartScan.setEnabled(false);
        } else {
            btnStartScan.setEnabled(true);
        }
        btnGenericAccessService.setEnabled(!serviceStatus.get(ServiceType.GENERIC_ACCESS_SERVICE));
        btnGenericAttributeService.setEnabled(!serviceStatus.get(ServiceType.GENERIC_ATTRIBUTE_SERVICE));
        btnDeviceInformationService.setEnabled(!serviceStatus.get(ServiceType.DEVICE_INFORMATION_SERVICE));
        btnBatteryService.setEnabled(!serviceStatus.get(ServiceType.BATTERY_SERVICE));
        btnHealthThermometerService.setEnabled(!serviceStatus.get(ServiceType.HEALTH_THERMOMETER_SERVICE));
        //       btnSensoriaCoreStreamingService.setEnabled(!serviceStatus.get(ServiceType.SENSORIA_STREAMING_SERVICE));
        btnSensoriaCoreControlPointService.setEnabled(!serviceStatus.get(ServiceType.SENSORIA_CONTROL_POINT_SERVICE));
        btnSensoriaCoreCustomConfiguration.setEnabled(!serviceStatus.get(ServiceType.SENSORIA_CUSTOM_CONFIGURATION_SERVICE));
        btnSensoriaCoreSmokeTest.setEnabled(!serviceStatus.get(ServiceType.SENSORIA_SMOKE_TEST_SERVICE));
        //  btnDFU.setEnabled(coreScanner.getSDVersion() >= 5);
    }

    @Override
    public void didRemoteRssiRead(Core core2, DeviceDescriptor deviceDescriptor, int i) {
    }

    @Override
    public void didScannerEvent(Scanner scanner, ScannerEvent scannerEvent) {
        switch (scannerEvent) {
            case SCANNING:
                didDeviceScanning(scanner);

            case SCANNING_COMPLETED:
                btnStartScan.setEnabled(false);
                btnStopScan.setEnabled(false);
                if (deviceDiscovered2 != null) {
                    if (!deviceDiscovered2.deviceName.isEmpty()) {
                        btnConnect.setEnabled(true);
                        MainActivity.setSock2Connected2(core2.isConnected());
                    } else {
                        btnConnect.setEnabled(false);
                        MainActivity.setSock2Connected2(core2.isConnected());
                    }
                } else {
                    btnConnect.setEnabled(false);
                    MainActivity.setSock2Connected2(core2.isConnected());
                }
        }
    }

    @Override
    public void didCoreEvent(Core core2, DeviceDescriptor deviceDescriptor, CoreEvent coreEvent) {
        switch (coreEvent) {
            case CONNECTING:
                didConnecting(core2, deviceDescriptor);

            case CONNECTED:
                didConnect(core2, deviceDescriptor);
                didServicesDiscovered(core2, deviceDescriptor);
            case DISCONNECTED:
                didDisconnect(core2, deviceDescriptor);

            case SIGNAL_LOST:
                didSignalLost(core2, deviceDescriptor);

            case DE_INITIALIZED:
                didInitialized(core2);
        }
    }

    @Override
    public void didDeviceDiscovered(Scanner scannerM, DeviceDescriptor deviceDescriptor, boolean b, boolean b1) {
//        sdkLog(WARN, "SensoriaLibrary", "Device Discovered: " + deviceDescriptor.deviceName);
        mac2 = deviceDescriptor.deviceMac;
        MainActivity.setSock2_mac_global(mac2);

        // Will sort available devices by closest core device using RSSI.
        CopyOnWriteArrayList<DeviceDescriptor> coreList = scanner2.getDeviceDiscoveredList();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            coreList.sort(new Comparator<DeviceDescriptor>() {
                @Override
                public int compare(DeviceDescriptor o1, DeviceDescriptor o2) {
                    return -1 * Integer.compare(o1.returnRSSI(), o2.returnRSSI());
                }
            });
        }

        ArrayAdapter<DeviceDescriptor> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                coreList);
        deviceListSpinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        deviceListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                deviceDiscovered2 = scanner2.getDeviceDiscoveredList().get(position);
                selectedCode = deviceDiscovered2.deviceCode;
                selectedMac = deviceDiscovered2.deviceMac;

//                deviceDiscovered2 = coreScanner.getDeviceDiscoveredList().get(position + 1);

//                sdkLog(DEBUG, "SensoriaLibrary", selectedCode + " " + selectedMac);
//                SdkLog(DEBUG, "SensoriaLibrary", deviceDiscovered2.deviceCode + " " + deviceDiscovered2.deviceName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCode = null;
            }
        });
    }

    @Override
    public void didScannerError(Scanner scanner, SdkError sdkError) {
    }
}
