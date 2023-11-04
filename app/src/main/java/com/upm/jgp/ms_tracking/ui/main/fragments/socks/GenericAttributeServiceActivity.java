package com.upm.jgp.healthywear.ui.main.fragments.socks;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.upm.jgp.healthywear.R;

public class GenericAttributeServiceActivity extends AppCompatActivity {

    private GenericAttributeServiceActivity genericAttributeService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generic_attribute_service);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            genericAttributeService = (GenericAttributeServiceActivity) getIntent().getSerializableExtra("SAService");
        }
    }
}
