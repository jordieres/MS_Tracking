package com.upm.jgp.healthywear.ui.main.fragments.socks;

import static android.util.Log.DEBUG;
import static android.util.Log.WARN;

import static io.sensoria.sdk.SensoriaSdk.checkRequiredPermissions;
import static io.sensoria.sdk.SensoriaSdk.sdkLog;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
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
import androidx.core.app.ActivityCompat;

import com.upm.jgp.healthywear.R;
import com.upm.jgp.healthywear.ui.main.activity.MainActivity;

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

public class ScanSock2Activity extends AppCompatActivity implements IPermissionsCallback, ICoreCallback, IScannerCallback, ActivityCompat.OnRequestPermissionsResultCallback {


    private String incoming_device_type = "Sock2";


    Core coreScanner;
    Scanner scanner;
    Core core1;
    //    SACore core2;
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

    private boolean mSignalLost = false;
    private DeviceDescriptor deviceDiscovered;
//    private SADevice deviceDiscovered2;

    private Spinner deviceListSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_sock);
        //SensoriaSdk.initialize(this, this,true);
        checkRequiredPermissions(this, this);
        coreScanner = new Core(this);
        scanner = new Scanner(this);
        core1 = new Core(this);
//        core2 = new SACore(this);
        //btnGenericAccessService = findViewById(R.id.buttonGenericAccessServiceStart);
        //btnGenericAccessService.setEnabled(false);
        btnGenericAttributeService = findViewById(R.id.buttonGenericAttributeServiceStart);
        btnGenericAttributeService.setEnabled(false);
            /*btnDeviceInformationService = findViewById(R.id.buttonDeviceInformationServiceStart);
            btnDeviceInformationService.setEnabled(false);
            btnBatteryService = findViewById(R.id.buttonBatteryServiceStart);
            btnBatteryService.setEnabled(false);
            btnHealthThermometerService = findViewById(R.id.buttonTemperatureServiceStart);
            btnHealthThermometerService.setEnabled(false);
            btnSensoriaCoreStreamingService = findViewById(R.id.buttonSensoriaCoreStreamingServiceStart);
            btnSensoriaCoreStreamingService.setEnabled(false);
            btnSensoriaCoreControlPointService = findViewById(R.id.buttonSensoriaCoreControlPointServiceStart);
            btnSensoriaCoreControlPointService.setEnabled(false);
            btnSensoriaCoreCustomConfiguration = findViewById(R.id.buttonSensoriaCoreCustomConfigStart);
            btnSensoriaCoreCustomConfiguration.setEnabled(false);
            btnSensoriaCoreSmokeTest = findViewById(R.id.buttonSensoriaCoreSmokeTest);
            btnSensoriaCoreSmokeTest.setEnabled(false);
            btnDFU = findViewById(R.id.buttonDFU);

             */
        btnConnect = findViewById(R.id.buttonConnect);
        btnConnect.setEnabled(false);
        //btnStopScan = findViewById(R.id.buttonStopScan);
        //btnStopScan.setEnabled(false);
        btnStartScan = findViewById(R.id.buttonStartScan);
        btnStartScan.setEnabled(true);
        //btnDisconnect = findViewById(R.id.buttonDisconnect);
        //btnDisconnect.setEnabled(false);
        deviceListSpinner = findViewById(R.id.spinnerDeviceCode);
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
        coreScanner.dispose();
        core1.dispose();
//        core2.dispose();
        super.onDestroy();
    }

    public void startScan(View view) {
        scanner.startScan(5000);
    }

    public void stopScan(View view) {
        scanner.stopScan();
    }

    public void connect(View view) {
        core1.connect(deviceDiscovered);
//        core2.connect(deviceDiscovered2);
    }

    public void disconnect(View view) {
        core1.disconnect();
//        core2.disconnect();
    }

        /*
        public void startGenericAccessService(View view) {
            Intent intent = new Intent(this, GenericAccessServiceActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("SAService", coreScanner.getServiceByType(SAService.Service
                    .GENERIC_ACCESS_SERVICE));
            intent.putExtras(bundle);
            startActivity(intent);
        }

        public void startBatteryService(View view) {
            Intent intent = new Intent(this, BatteryServiceActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("BatteryService", core1.getServiceByType(SAService.Service
                    .BATTERY_SERVICE));
            bundle.putSerializable("SAControlPointService", core1.getServiceByType(SAService.Service
                    .SENSORIA_CONTROL_POINT_SERVICE));

//        bundle.putSerializable("BatteryService_2", core2.getServiceByType(SAService.Service
//                .BATTERY_SERVICE));
//        bundle.putSerializable("SAControlPointService_2", core2.getServiceByType(SAService.Service
//                .SENSORIA_CONTROL_POINT_SERVICE));

            intent.putExtras(bundle);
            startActivity(intent);
        }

        public void startHealthThermometerService(View view) {
            Intent intent = new Intent(this, HealthThermometerServiceActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("SAService", core1.getServiceByType(SAService.Service
                    .HEALTH_THERMOMETER_SERVICE));
            intent.putExtras(bundle);
            startActivity(intent);
        }

        public void startGenericAttributeService(View view) {
            Intent intent = new Intent(this, GenericAttributeServiceActivity.class);
            intent.putExtra("SAService", deviceDiscovered);
            startActivity(intent);

        }

        public void startDeviceInformationService(View view) {
            Intent intent = new Intent(this, DeviceInformationServiceActivity.class);
            intent.putExtra("SAService", deviceDiscovered);
            startActivity(intent);

        }

        public void startDFU(View view) {
            if (deviceDiscovered != null) {
                Intent intent = new Intent(this, DfuActivity.class);
                intent.putExtra("DeviceDFU", deviceDiscovered);
                startActivity(intent);
            }
        }

        public void startSensoriaStreamingService(View view) {
            // Let's use this button for all streaming services

            Intent intent = new Intent(this, SensoriaCoreStreamingServiceActivity.class);
            Bundle bundle = new Bundle();

            // Sensoria or Balance device
            SAService service1 = core1.getServiceByType(SAService.Service.SENSORIA_STREAMING_SERVICE);
            if (service1 != null) {
                bundle.putSerializable(SensoriaCoreStreamingServiceActivity.SENSORIASTREAMINGSERVICE, service1);
            } else {
                service1 = core1.getServiceByType(SAService.Service.BALANCE_STREAMING_SERVICE);
                bundle.putSerializable(SensoriaCoreStreamingServiceActivity.BALANCESTREAMINGSERVICE, service1);
            }

//        SAService service2 = core2.getServiceByType(SAService.Service.SENSORIA_STREAMING_SERVICE);
//        if (service2 != null) {
//            bundle.putSerializable(SensoriaCoreStreamingServiceActivity.SENSORIASTREAMINGSERVICE + "_2", service2);
//        } else {
//            service2 = core1.getServiceByType(SAService.Service.BALANCE_STREAMING_SERVICE);
//            bundle.putSerializable(SensoriaCoreStreamingServiceActivity.BALANCESTREAMINGSERVICE + "_2", service2);
//        }

            intent.putExtras(bundle);
            startActivity(intent);
        }

        public void startSensoriaControlPointService(View view) {
            Intent intent = new Intent(this, SensoriaCoreControlPointServiceActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("SAService", core1.getServiceByType(SAService.Service
                    .SENSORIA_CONTROL_POINT_SERVICE));
            intent.putExtras(bundle);
            startActivity(intent);
        }

        public void startSensoriaCustomConfigurationService(View view) {
            Intent intent = new Intent(this, SensoriaCoreCustomConfigurationActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("SAService", core1.getServiceByType(SAService.Service
                    .SENSORIA_CONTROL_POINT_SERVICE));
            intent.putExtras(bundle);
            startActivityForResult(intent, 2);
        }

        public void startSensoriaSmokeTestService(View view) {
            Intent intent = new Intent(this, SensoriaCoreSmokeTestActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("SAServiceControlPointService", core1.getServiceByType(SAService.Service
                    .SENSORIA_CONTROL_POINT_SERVICE));
            SAService service1 = core1.getServiceByType(SAService.Service.SENSORIA_STREAMING_SERVICE);
            bundle.putSerializable("SAServiceStreamingService", service1);
            intent.putExtras(bundle);
            startActivity(intent);
        }
        */

    public void didInitialized(Core core) {
    }

    public void didDeviceScanCompleted(Scanner core) {
        btnStartScan.setEnabled(true);
        //btnStopScan.setEnabled(false);
        if (deviceDiscovered != null) {
            if (!deviceDiscovered.deviceName.isEmpty()) {
                btnConnect.setEnabled(true);
            } else {
                btnConnect.setEnabled(false);
            }
        } else {
            btnConnect.setEnabled(false);
        }
    }

    public void didUninitialized(Scanner core) {
        btnStartScan.setEnabled(false);
        //btnStopScan.setEnabled(false);
    }

    public void didDeviceScanning(Scanner core) {
        btnStartScan.setEnabled(false);
        //btnStopScan.setEnabled(true);
        btnConnect.setEnabled(false);
    }


    public void didServicesDiscovered(Core core, DeviceDescriptor device) {
        sdkLog(WARN, "SensoriaLibrary", "Service Discovered");

        for (BaseService service : coreScanner.getServiceDiscoveredList()) {
            //btnGenericAccessService.setEnabled(enableButton(service, btnGenericAccessService,
            //      this.getString(R.string.GENERIC_ACCESS_SERVICE)));
            btnGenericAttributeService.setEnabled(enableButton(service, btnGenericAttributeService,
                    this.getString(R.string.GENERIC_ATTRIBUTE_SERVICE)));
                /*btnDeviceInformationService.setEnabled(enableButton(service, btnDeviceInformationService,
                        this.getString(R.string.DEVICE_INFORMATION_SERVICE)));
                btnBatteryService.setEnabled(enableButton(service, btnBatteryService, this
                        .getString(R.string.BATTERY_SERVICE)));
                btnHealthThermometerService.setEnabled(enableButton(service, btnHealthThermometerService,
                        "Health Thermometer Service"));
                btnSensoriaCoreStreamingService.setEnabled(enableButton(service,
                        btnSensoriaCoreStreamingService, this.getString(R.string
                                .SENSORIA_CORE_STREAMING_SERVICE)));
                btnSensoriaCoreControlPointService.setEnabled(enableButton(service,
                        btnSensoriaCoreControlPointService, this.getString(R.string
                                .SENSORIA_CORE_CONTROL_POINT_SERVICE)));
                btnSensoriaCoreCustomConfiguration.setEnabled(enableButton(service,
                        btnSensoriaCoreCustomConfiguration, this.getString(R.string.SENSORIA_CORE_CUSTOM_CONFIGURATION_SERVICE)));
                btnSensoriaCoreSmokeTest.setEnabled(enableButton(service,
                        btnSensoriaCoreSmokeTest, this.getString(R.string.SENSORIA_CORE_SMOKE_TEST_SERVICE)));
            */
        }

    }

    private boolean enableButton(BaseService service, Button button, String serviceName) {
        return (((service.getServiceName().compareTo(serviceName) == 0) &&
                button.getVisibility() == View.VISIBLE) || button.isEnabled());
    }

    public void didConnecting(Core core, DeviceDescriptor device) {
        btnConnect.setEnabled(false);
        btnStartScan.setEnabled(false);

    }

    public void didConnect(Core core, DeviceDescriptor device) {
        //btnDisconnect.setEnabled(true);
        btnConnect.setEnabled(false);
        if (mSignalLost) {
            mSignalLost = false;
            didServicesDiscovered(core, device);
        } else {
            btnGenericAttributeService.setEnabled(true);
            MainActivity.setSock2Connected(true);
                /*btnGenericAccessService.setEnabled(true);
                btnGenericAttributeService.setEnabled(true);
                btnDeviceInformationService.setEnabled(true);
                btnBatteryService.setEnabled(true);
                btnHealthThermometerService.setEnabled(true);
                btnSensoriaCoreStreamingService.setEnabled(true);
                btnSensoriaCoreControlPointService.setEnabled(true);
                btnSensoriaCoreCustomConfiguration.setEnabled(true);
                btnSensoriaCoreSmokeTest.setEnabled(true);
                if (coreScanner.getSDVersion() >= 5)
                    btnDFU.setEnabled(true);
                else
                    btnDFU.setEnabled(false);

                 */
        }
    }

    public void didDisconnect(Core core, DeviceDescriptor device) {
        btnConnect.setEnabled(true);
        btnStartScan.setEnabled(true);
        //btnDisconnect.setEnabled(false);
        //btnGenericAccessService.setEnabled(false);
        btnGenericAttributeService.setEnabled(false);
            /*btnDeviceInformationService.setEnabled(false);
            btnBatteryService.setEnabled(false);
            btnHealthThermometerService.setEnabled(false);
            btnSensoriaCoreStreamingService.setEnabled(false);
            btnSensoriaCoreControlPointService.setEnabled(false);
            btnSensoriaCoreCustomConfiguration.setEnabled(false);
            btnSensoriaCoreSmokeTest.setEnabled(false);
            if (coreScanner.getSDVersion() >= 5)
                btnDFU.setEnabled(false);

             */
    }

    public void didSignalLost(Core core, DeviceDescriptor device) {
        mSignalLost = true;
        btnStartScan.setEnabled(false);
        //btnStopScan.setEnabled(false);
        btnConnect.setEnabled(false);
            /*btnDisconnect.setEnabled(false);
            btnGenericAccessService.setEnabled(false);
            btnGenericAttributeService.setEnabled(false);
            btnDeviceInformationService.setEnabled(false);
            btnBatteryService.setEnabled(false);
            btnHealthThermometerService.setEnabled(false);
            btnSensoriaCoreStreamingService.setEnabled(false);
            btnSensoriaCoreControlPointService.setEnabled(false);
            btnSensoriaCoreCustomConfiguration.setEnabled(false);
            btnSensoriaCoreSmokeTest.setEnabled(false);
            if (coreScanner.getSDVersion() >= 5)
                btnDFU.setEnabled(false);

             */
    }

    @Override
    public void didPermissionsGranted(String[] permissions) {
        Toast.makeText(ScanSock2Activity.this, this.getString(R.string.PERMISSION_GRANTED), Toast.LENGTH_SHORT).show();
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
                    core1.dispose();
                    coreScanner.dispose();
                    // clearing discovered devices list since its data is no longer valid.
                    deviceListSpinner.setAdapter(null);
                    // Re-initializing objects, since onCreate is not executed when the activity resumes.
                    core1 = new Core(this);
                    coreScanner = new Core(this);
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
                Toast.makeText(ScanSock2Activity.this, this.getString(granted ? R.string.PERMISSION_GRANTED_LOCATION :
                        R.string.PERMISSION_DENIED_LOCATION), Toast.LENGTH_SHORT).show();
                if (!granted) {
                    finish();
                    break;
                }
            }
        }
    }

    public void Actividad(View view) {
        //Intent intent = new Intent(this, TabWearablesActivity.class);
        Intent intent = new Intent(this, SensoriaCoreStreamingServiceActivity2.class);
        Bundle bundle = new Bundle();

        // Sensoria or Balance device
        BaseService service1 = core1.getServiceByType(ServiceType.SENSORIA_STREAMING_SERVICE);
        if (service1 != null) {
            bundle.putSerializable(SensoriaCoreStreamingServiceActivity2.SENSORIASTREAMINGSERVICE, service1);
        } else {
            bundle.putSerializable(SensoriaCoreStreamingServiceActivity2.BALANCESTREAMINGSERVICE, service1);
        }

        //intent.putExtra(TabWearablesActivity.DEVICE_TYPE, incoming_device_type);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        intent.putExtras(bundle);

        startActivity(intent);
    }

    @Override
    public void didCoreEvent(Core core, DeviceDescriptor deviceDescriptor, CoreEvent coreEvent) {
        switch (coreEvent) {
            case CONNECTING:
                didConnecting(core, deviceDescriptor);

            case CONNECTED:
                didConnect(core, deviceDescriptor);
                didServicesDiscovered(core, deviceDescriptor);

            case DISCONNECTED:
                didDisconnect(core, deviceDescriptor);

            case SIGNAL_LOST:
                didSignalLost(core, deviceDescriptor);

            case DE_INITIALIZED:
                didInitialized(core);
        }
    }

    @Override
    public void didCoreError(Core core, DeviceDescriptor deviceDescriptor, SdkError sdkError) {

        AlertDialog.Builder builder;

        switch (sdkError) {
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
    public void didServiceStatusChange(Core core, DeviceDescriptor deviceDescriptor, Map<ServiceType, Boolean> map) {
//        btnDisconnect.setEnabled(false);
        boolean isConnected = coreScanner.isConnected();
        //btnDisconnect.setEnabled(isConnected);
        btnConnect.setEnabled(!isConnected);
        if (map.values().contains(true)) {
            btnStartScan.setEnabled(false);
        } else {
            btnStartScan.setEnabled(true);
        }
        //btnGenericAccessService.setEnabled(!serviceStatus.get(SAService.Service.GENERIC_ACCESS_SERVICE));
        btnGenericAttributeService.setEnabled(!map.get(ServiceType.GENERIC_ATTRIBUTE_SERVICE));

    }


    @Override
    public void didRemoteRssiRead(Core core, DeviceDescriptor deviceDescriptor, int i) {

    }

    @Override
    public void didScannerEvent(Scanner scanner, ScannerEvent scannerEvent) {
        switch (scannerEvent) {
            case SCANNING:
                didDeviceScanning(scanner);

            case SCANNING_COMPLETED:
                didDeviceScanCompleted(scanner);

            case DE_INITIALIZED:
                didUninitialized(scanner);
        }
    }

    @Override
    public void didDeviceDiscovered(Scanner scanner, DeviceDescriptor deviceDescriptor, boolean b, boolean b1) {
        sdkLog(WARN, "SensoriaLibrary", "Device Discovered: " + deviceDescriptor.deviceName);

        // Will sort available devices by closest core device using RSSI.
        CopyOnWriteArrayList<DeviceDescriptor> coreList = scanner.getDeviceDiscoveredList();
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
                deviceDiscovered = scanner.getDeviceDiscoveredList().get(position);
                selectedCode = deviceDiscovered.deviceCode;
                selectedMac = deviceDiscovered.deviceMac;

//                deviceDiscovered2 = coreScanner.getDeviceDiscoveredList().get(position + 1);

                sdkLog(DEBUG, "SensoriaLibrary", selectedCode + " " + selectedMac);
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
