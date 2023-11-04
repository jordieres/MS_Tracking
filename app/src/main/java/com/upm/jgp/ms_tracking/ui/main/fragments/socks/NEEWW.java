package com.upm.jgp.healthywear.ui.main.fragments.socks;

import static android.util.Log.DEBUG;
import static android.util.Log.VERBOSE;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.upm.jgp.healthywear.R;
import com.upm.jgp.healthywear.ui.main.activity.ChooseDeviceToScanActivity;
import com.upm.jgp.healthywear.ui.main.activity.MainActivity;
import com.upm.jgp.healthywear.ui.main.activity.TabWearablesActivity;
import com.upm.jgp.healthywear.ui.main.fragments.socks.SensoriaCoreStreamingServiceActivity;

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

public class NEEWW extends AppCompatActivity implements IPermissionsCallback, ICoreCallback, IScannerCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    private String incoming_device_type = "Sock";
    public static final String BLUETOOTH_SCAN = null;


    Core coreScanner;
    Scanner scanner;
    Core core1;
    Core core2;
    private final int MY_PERMISSIONS = 1;
    private String selectedCode;
    private String selectedMac;
    private int selectedPosition = 0;
    private Button btnGenericAttributeService;
    private Button btnConnect;
    private Button btnStartScan;

    private boolean mSignalLost = false;
    private DeviceDescriptor deviceDiscovered;
    private DeviceDescriptor deviceDiscovered2;

    private Spinner deviceListSpinner;

    RadioButton uno, dos;
    RadioGroup rgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_sock);
        if (!SensoriaSdk.isInitialized()) {
            SensoriaSdk.initialize(false, true, VERBOSE, true);
        }
        System.out.println("Entro a onCreate");
        //SensoriaSdk.initialize(this, this,true);
        checkRequiredPermissions(this, this);
        coreScanner = new Core(this);
        scanner = new Scanner(this);
        core1 = new Core(this);
        core2 = new Core(this);

        btnGenericAttributeService = findViewById(R.id.buttonGenericAttributeServiceStart);
        btnGenericAttributeService.setEnabled(true);

        uno = findViewById(R.id.uno);
        dos = findViewById(R.id.dos);
        rgr = findViewById(R.id.rg);

        btnConnect = findViewById(R.id.buttonConnect);
        btnConnect.setEnabled(false);
        btnStartScan = findViewById(R.id.buttonStartScan);
        deviceListSpinner = findViewById(R.id.spinnerDeviceCode);

       /* if (ContextCompat.checkSelfPermission(ScanSockActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(ScanSockActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                return;
            }
        }*/
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
        core2.dispose();
        super.onDestroy();
    }

    public void startScan(View view) {
        if (!uno.isChecked()&&!dos.isChecked()) {
            Toast.makeText(this, "Cuántos dispositivos quiere conectar?", Toast.LENGTH_SHORT).show();
        } else if (uno.isChecked()) {
            MainActivity.setSockConnected(true);
            scanner.startScan(5000);
        } else if (dos.isChecked()) {
            MainActivity.setSock2Connected(true);
            scanner.startScan(5000);
        }
    }

    public void stopScan(View view) {
        scanner.stopScan();
    }

    public void connect(View view) {
        if (MainActivity.isSockConnected()) {
            core1.connect(deviceDiscovered);
            System.out.println("AQUIIII ENTROOO cc3 " + core1.isConnected());
            btnGenericAttributeService.setEnabled(true);
        } else if (MainActivity.isSock2Connected()) {
            core1.connect(deviceDiscovered);
            core2.connect(deviceDiscovered2);
        }
    }

    public void disconnect(View view) {
        core1.disconnect();
        core2.disconnect();
    }

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
 //       btnStartScan.setEnabled(false);
        //btnStopScan.setEnabled(false);
    }

    public void didDeviceScanning(Scanner core) {
 //       btnStartScan.setEnabled(false);
        //btnStopScan.setEnabled(true);
        btnConnect.setEnabled(false);
    }

    public void didDeviceDiscoveredUpdated(Core core, DeviceDescriptor device, boolean hasDisappeared) {
        sdkLog(WARN, "SensoriaLibrary", "Device Updated: " + device.deviceName + ", disappeared: " + hasDisappeared);
    }

    public void didServicesDiscovered(Core core, DeviceDescriptor device) {
        sdkLog(WARN, "SensoriaLibrary", "Service Discovered");

        for (BaseService service : coreScanner.getServiceDiscoveredList()) {

        }

    }

    private boolean enableButton(BaseService service, Button button, String serviceName) {
        return (((service.getServiceName().compareTo(serviceName) == 0) &&
                button.getVisibility() == View.VISIBLE) || button.isEnabled());
    }


    public void didConnecting(Core core, DeviceDescriptor device) {
        btnConnect.setEnabled(false);
//        btnStartScan.setEnabled(false);

    }

    public void didConnect(Core core, DeviceDescriptor device) {
        //btnDisconnect.setEnabled(true);
        btnConnect.setEnabled(false);
        if (mSignalLost) {
            mSignalLost = false;
            didServicesDiscovered(core, device);
            MainActivity.setSockConnected(false);
            MainActivity.setSock2Connected(false);
        } else {
            btnGenericAttributeService.setEnabled(true);
        }
    }

    public void didDisconnect(Core core, DeviceDescriptor device) {
        btnConnect.setEnabled(true);
        btnStartScan.setEnabled(true);
 //       btnGenericAttributeService.setEnabled(false);

    }

    @Override
    public void didRemoteRssiRead(Core core, DeviceDescriptor device, int rssi) {
        // NOOP
    }

    public void didSignalLost(Core core, DeviceDescriptor device) {
        mSignalLost = true;
 //       btnStartScan.setEnabled(false);
        //btnStopScan.setEnabled(false);
        btnConnect.setEnabled(false);
    }

    @Override
    public void didServiceStatusChange(Core core, DeviceDescriptor device, Map<ServiceType, Boolean> serviceStatus) {
//        btnDisconnect.setEnabled(false);
        boolean isConnected = coreScanner.isConnected();
        //btnDisconnect.setEnabled(isConnected);
        btnConnect.setEnabled(!isConnected);
        if (serviceStatus.values().contains(true)) {
//            btnStartScan.setEnabled(false);
        } else {
            btnStartScan.setEnabled(true);
        }
 //       btnGenericAttributeService.setEnabled(!serviceStatus.get(ServiceType.GENERIC_ATTRIBUTE_SERVICE));
    }


    @Override
    public void didCoreError(Core core, DeviceDescriptor device, SdkError errorCode) {
        AlertDialog.Builder builder;

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
        Toast.makeText(NEEWW.this, this.getString(R.string.PERMISSION_GRANTED), Toast.LENGTH_SHORT).show();
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
                    scanner = new Scanner(this);
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
                Toast.makeText(NEEWW.this, this.getString(granted ? R.string.PERMISSION_GRANTED_LOCATION :
                        R.string.PERMISSION_DENIED_LOCATION), Toast.LENGTH_SHORT).show();
                if (!granted) {
                    finish();
                    break;
                }
            }
        }
    }

    public void Actividad (View view) {
        //Intent intent = new Intent(this, TabWearablesActivity.class);
        Intent intent = new Intent(this, SensoriaCoreStreamingServiceActivity.class);
        Bundle bundle = new Bundle();

        if (MainActivity.isSockConnected()) {
            // Sensoria or Balance device
            BaseService service1 = core1.getServiceByType(ServiceType.SENSORIA_STREAMING_SERVICE);
            if (service1 != null) {
                bundle.putSerializable(SensoriaCoreStreamingServiceActivity.SENSORIASTREAMINGSERVICE, service1);
            } else {
               bundle.putSerializable(SensoriaCoreStreamingServiceActivity.BALANCESTREAMINGSERVICE, service1);
            }
        }

        if (MainActivity.isSock2Connected()) {
            BaseService service1 = core1.getServiceByType(ServiceType.SENSORIA_STREAMING_SERVICE);
            if (service1 != null) {
                bundle.putSerializable(SensoriaCoreStreamingServiceActivity.SENSORIASTREAMINGSERVICE, service1);
            } else {

                bundle.putSerializable(SensoriaCoreStreamingServiceActivity.BALANCESTREAMINGSERVICE, service1);
            }

            BaseService service2 = core2.getServiceByType(ServiceType.SENSORIA_STREAMING_SERVICE);
            if (service2 != null) {
                bundle.putSerializable(SensoriaCoreStreamingServiceActivity.SENSORIASTREAMINGSERVICE + "_2", service2);
            } else {

                bundle.putSerializable(SensoriaCoreStreamingServiceActivity.BALANCESTREAMINGSERVICE + "_2", service2);
            }
        }

        //intent.putExtra(TabWearablesActivity.DEVICE_TYPE, incoming_device_type);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        intent.putExtras(bundle);

        startActivity(intent);
    }

    @Override
    public void didCoreEvent(Core core, DeviceDescriptor deviceDescriptor, CoreEvent coreEvent) {
        switch (coreEvent) {
            case INITIALIZED:
                didInitialized(core);
            case CONNECTING:
                didConnecting(core, deviceDescriptor);

            case CONNECTED:
                didConnect(core, deviceDescriptor);

            case DISCONNECTING:

            case DISCONNECTED:
                didDisconnect(core, deviceDescriptor);

            case SIGNAL_LOST:

        }
    }

    @Override
    public void didScannerEvent(Scanner scanner, ScannerEvent scannerEvent) {
            switch (scannerEvent) {
                case READY:

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
                if (MainActivity.isSockConnected()) {
                    deviceDiscovered = scanner.getDeviceDiscoveredList().get(position);
                    selectedCode = deviceDiscovered.deviceCode;
                    selectedMac = deviceDiscovered.deviceMac;

                    sdkLog(DEBUG, "SensoriaLibrary", selectedCode + " " + selectedMac);

                } else if (MainActivity.isSock2Connected()){
                    deviceDiscovered = scanner.getDeviceDiscoveredList().get(position);
                    selectedCode = deviceDiscovered.deviceCode;
                    selectedMac = deviceDiscovered.deviceMac;

                    deviceDiscovered2 = scanner.getDeviceDiscoveredList().get(position + 1);

                    sdkLog(DEBUG, "SensoriaLibrary", selectedCode + " " + selectedMac);
                    sdkLog(DEBUG, "SensoriaLibrary", deviceDiscovered2.deviceCode + " " + deviceDiscovered2.deviceName);
                }

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
