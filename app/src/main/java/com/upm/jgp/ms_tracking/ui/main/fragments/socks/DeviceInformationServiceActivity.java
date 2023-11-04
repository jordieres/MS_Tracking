package com.upm.jgp.healthywear.ui.main.fragments.socks;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import com.upm.jgp.healthywear.R;

public class DeviceInformationServiceActivity extends AppCompatActivity {

    private DeviceInformationServiceActivity deviceInformationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_information_service);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            deviceInformationService = (DeviceInformationServiceActivity) getIntent().getSerializableExtra("SAService");
        }

    }
}
