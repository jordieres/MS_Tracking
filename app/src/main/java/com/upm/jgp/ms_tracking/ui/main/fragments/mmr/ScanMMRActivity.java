package com.upm.jgp.healthywear.ui.main.fragments.mmr;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.mbientlab.bletoolbox.scanner.BleScannerFragment;
import com.mbientlab.metawear.MetaWearBoard;
import com.upm.jgp.healthywear.R;
import com.upm.jgp.healthywear.ui.main.activity.MainActivity;
import com.upm.jgp.healthywear.ui.main.activity.TabWearablesActivity;
import com.upm.jgp.healthywear.ui.main.fragments.common.MyService;

import java.util.UUID;

/**
 * Activity to scan MMR devices (mbientlab) and connect to a selected one.
 *
 * It also handles the reconnection of the MMR for better stability
 *
 * Based on MainActivity class of MetaWear-SDK-Android by mbientlab
 * @author Modified by Jorge Garcia Paredes (yoryidan)
 * Modified by Raquel Prous 2022
 * @version 222
 * @since 2020
 */
public class ScanMMRActivity extends AppCompatActivity implements BleScannerFragment.ScannerCommunicationBus {
    private static final int REQUEST_START_APP= 1;

    private MetaWearBoard metawear = null;
    final String local_device_type = "MMR";    //MMR

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_mmr);

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
    }

    //****** code from uploadMMRData app ***
    @Override
    protected void onResume() {
        super.onResume();
        //System.out.println("onResume...");

        //***Receive textview information from MyService***
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String UIString = intent.getStringExtra(MyService.DATA_STRING);
                        System.out.println(UIString);
                    }
                }, new IntentFilter(MyService.ACTION_UI_BROADCAST)
        );

        //***Restart the application when no data coming for a long period
        //http://blog.scriptico.com/01/how-to-restart-android-application/
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        long timegap=intent.getLongExtra("TIMEGAP", 0);
                        if (timegap>1800000){
                            //finish();
                            restartApp(300);
                        }
                    }
                }, new IntentFilter("APP_RESTART_BROADCAST")
        );

    }

    public void restartApp(int delay) {
        PendingIntent intent = PendingIntent.getActivity(this.getBaseContext(), 0, new Intent(getIntent()), getIntent().getFlags());
        AlarmManager manager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        manager.set(AlarmManager.RTC, System.currentTimeMillis() + delay, intent);
        //System.exit(0);
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_START_APP:
                ((BleScannerFragment) getFragmentManager().findFragmentById(R.id.scanner_fragment)).startBleScan();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public UUID[] getFilterServiceUuids() {
        return new UUID[] {MetaWearBoard.METAWEAR_GATT_SERVICE};
    }

    @Override
    public long getScanDuration() {
        return 10000L;
    }

    @Override
    public void onDeviceSelected(final BluetoothDevice device) {
        metawear = MainActivity.serviceBinder.getMetaWearBoard(device);

        final ProgressDialog connectDialog = new ProgressDialog(this);
        connectDialog.setTitle(getString(R.string.title_connecting));
        connectDialog.setMessage(getString(R.string.message_wait));
        connectDialog.setCancelable(false);
        connectDialog.setCanceledOnTouchOutside(false);
        connectDialog.setIndeterminate(true);
        connectDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel), (dialogInterface, i) -> metawear.disconnectAsync());
        connectDialog.show();

        metawear.connectAsync().continueWithTask(task -> task.isCancelled() || !task.isFaulted() ? task : MainActivity.reconnect(metawear))
                .continueWith(task -> {
                    if (!task.isCancelled()) {
                        runOnUiThread(connectDialog::dismiss);
                        MainActivity.setMmrConnected(true); //Sets MMR device as connected
                        MainActivity.setMmr_device_global(device);   //Set device's MAC

                        Intent navActivityIntent = new Intent(ScanMMRActivity.this, TabWearablesActivity.class);
                        navActivityIntent.putExtra(TabWearablesActivity.DEVICE_TYPE, local_device_type);
                        navActivityIntent.putExtra(TabWearablesActivity.EXTRA_BT_DEVICE, device);
                        navActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivityForResult(navActivityIntent, REQUEST_START_APP);
                        //If it's the second device to connect, then it is necessary to refresh the view
                        if(MainActivity.isSmartbandConnected()){
                            TabWearablesActivity.refreshTabs(1);
                        }
                        if(MainActivity.isMmr2Connected()){
                            TabWearablesActivity.refreshTabs(1);
                        }
                        if(MainActivity.isSock1Connected1()){
                            TabWearablesActivity.refreshTabs(1);
                        }
                        if(MainActivity.isSock2Connected2()){
                            TabWearablesActivity.refreshTabs(1);
                        }
                    }
                    return null;
                });
    }
}
