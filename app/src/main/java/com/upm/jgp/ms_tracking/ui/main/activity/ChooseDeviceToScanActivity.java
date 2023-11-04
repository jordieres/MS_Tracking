package com.upm.jgp.healthywear.ui.main.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.upm.jgp.healthywear.R;
import com.upm.jgp.healthywear.ui.main.fragments.mmr.ScanMMR2Activity;
import com.upm.jgp.healthywear.ui.main.fragments.mmr.ScanMMRActivity;
import com.upm.jgp.healthywear.ui.main.fragments.smartband.ScanSmartBandActivity;
import com.upm.jgp.healthywear.ui.main.fragments.socks.CoreTestActivity;
import com.upm.jgp.healthywear.ui.main.fragments.socks.CoreTestActivity2;
import com.upm.jgp.healthywear.ui.main.fragments.socks.NEEWW;
import com.upm.jgp.healthywear.ui.main.fragments.socks.ScanSock2Activity;
import com.upm.jgp.healthywear.ui.main.fragments.socks.ScanSockActivity;

/**
 * Activity to select which device type is going to be scanned
 *
 * Currently it can be of two different types: SmartBand or MMR
 * It will redirect to the scan activity of the selected type
 *
 * @author Jorge Garcia Paredes (yoryidan)
 * Modified by Raquel Prous 2022
 * @version 222
 * @since 2020
 */
public class ChooseDeviceToScanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_device_to_scan);

        CardView sock1_CV = findViewById(R.id.CV_card_selection_4);

        //Set CardView color to grey if the device is already connected, white otherwise

        if(MainActivity.isSock1Connected1()){
            sock1_CV.setCardBackgroundColor(getResources().getColor(R.color.color44));
        }else{
            sock1_CV.setCardBackgroundColor(getResources().getColor(R.color.color4));
        }if(MainActivity.isSockConnected()){
            sock1_CV.setCardBackgroundColor(getResources().getColor(R.color.color44));
        }else{
            sock1_CV.setCardBackgroundColor(getResources().getColor(R.color.color4));
        }


        //////Toolbar Settings//////
        Toolbar toolbar = (Toolbar) findViewById(R.id.myToolbar_choose_scan);
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

    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.LL_card_selection_4: //go to mmr2 scan screen
                if(MainActivity.isSockConnected()){
                    Toast.makeText(this, "Sock1 device already connected", Toast.LENGTH_SHORT).show();
                }else{
                    Intent intent3 = new Intent(ChooseDeviceToScanActivity.this, ScanSockActivity.class);
                    startActivity(intent3);
                }
                break;

        }
    }

}
