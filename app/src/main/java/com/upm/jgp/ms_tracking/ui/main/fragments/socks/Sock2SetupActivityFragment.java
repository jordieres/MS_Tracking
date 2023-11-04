package com.upm.jgp.healthywear.ui.main.fragments.socks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.upm.jgp.healthywear.R;


public class Sock2SetupActivityFragment extends Fragment {


    static Context mContext = null;

    static Activity owner;

    private Button btnSock2;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        owner= getActivity();
        mContext = owner.getApplicationContext();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        return inflater.inflate(R.layout.fragment_tab_sock2, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnSock2 = view.findViewById(R.id.btnSock2);
        btnSock2.setVisibility(View.VISIBLE);

        /*if (MainActivity.isSock2Connected()) {
            btnSock2.setVisibility(View.VISIBLE);
        } else {
            btnSock2.setVisibility(View.GONE);
        }

         */

        view.findViewById(R.id.btnSock2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SensoriaCoreStreamingServiceActivity2.class);
                Bundle bundle = new Bundle();

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                intent.putExtras(bundle);

                startActivity(intent);
            }
        });

    }


}