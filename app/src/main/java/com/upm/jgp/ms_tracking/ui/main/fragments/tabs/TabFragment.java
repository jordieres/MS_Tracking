package com.upm.jgp.healthywear.ui.main.fragments.tabs;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.upm.jgp.healthywear.R;
import com.upm.jgp.healthywear.ui.main.activity.MainActivity;
import com.upm.jgp.healthywear.ui.main.fragments.mmr.ScanMMR2Activity;
import com.upm.jgp.healthywear.ui.main.fragments.mmr.ScanMMRActivity;
import com.upm.jgp.healthywear.ui.main.fragments.smartband.ScanSmartBandActivity;
import com.upm.jgp.healthywear.ui.main.activity.TabWearablesActivity;
import com.upm.jgp.healthywear.ui.main.fragments.socks.CoreTestActivity2;
import com.upm.jgp.healthywear.ui.main.fragments.socks.NEEWW;
import com.upm.jgp.healthywear.ui.main.fragments.socks.ScanSockActivity;


/**
 * A placeholder TabFragment containing a simple view.
 * <p>
 * Using 4 Tabs necessary to update second device interface, part of refreshing tabs is made in the onDestroyView method
 *
 * @author Jorge Garcia Paredes (yoryidan)
 * Modified by Raquel Prous 2022
 * @version 222
 * @since 2020
 */
public class TabFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private TabViewModel tabViewModel;

    public static TabFragment newInstance(int index) {
        TabFragment fragment = new TabFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tabViewModel = ViewModelProviders.of(this).get(TabViewModel.class);

        int index = 0;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        tabViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root;
        FloatingActionButton mFab;

        System.out.println("onCreateView. tab:" + getArguments().getInt(ARG_SECTION_NUMBER));

        switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
            case 0: {
                if (MainActivity.isSmartbandConnected()) {
                    root = inflater.inflate(R.layout.content_tab_smartband, container, false);
                } else {
                    root = inflater.inflate(R.layout.fragment_textview_tabs, container, false);
                    simpleTab(root);
                    //Add new SmartBand
                    mFab = root.findViewById(R.id.fab_tabs_section_label);
                    mFab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(view.getContext(), ScanSmartBandActivity.class);
                            startActivity(intent);
                        }
                    });
                }
                break;
            }
            case 1: {
                if (MainActivity.isMmrConnected()) {
                    root = inflater.inflate(R.layout.content_tab_mmr, container, false);
                } else {
                    root = inflater.inflate(R.layout.fragment_textview_tabs, container, false);
                    simpleTab(root);
                    //Add new MMR
                    mFab = root.findViewById(R.id.fab_tabs_section_label);
                    mFab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(view.getContext(), ScanMMRActivity.class);
                            startActivity(intent);
                        }
                    });
                }
                break;
            }
            case 2: {
                if (MainActivity.isMmr2Connected()) {
                    root = inflater.inflate(R.layout.content_tab_mmr2, container, false);
                } else {
                    root = inflater.inflate(R.layout.fragment_textview_tabs, container, false);
                    simpleTab(root);
                    //Add new MMR
                    mFab = root.findViewById(R.id.fab_tabs_section_label);
                    mFab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(view.getContext(), ScanMMR2Activity.class);
                            startActivity(intent);
                        }
                    });
                }
                break;
            }

            case 3: {
                if(MainActivity.isSockConnected()||MainActivity.isSock2Connected()) {
                    root = inflater.inflate(R.layout.content_tab_sock, container, false);
                }else{
                    root = inflater.inflate(R.layout.fragment_textview_tabs, container, false);
                    simpleTab(root);
                    //Add new SmartBand
                    mFab = root.findViewById(R.id.fab_tabs_section_label);
                    mFab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(view.getContext(), ScanSockActivity.class);
                            startActivity(intent);
                        }
                    });
                }
                break;
            }

            case 4: {
                root = inflater.inflate(R.layout.fragment_textview_tabs, container, false);
                simpleTab(root);
                //TODO add other devices
                mFab = root.findViewById(R.id.fab_tabs_section_label);
                mFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Snackbar.make(view, "Add other devices... (TBD)", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
                break;
            }

            default:
                root = inflater.inflate(R.layout.fragment_textview_tabs, container, false);
                simpleTab(root);
                break;
        }

        return root;
    }

    private void simpleTab(View root) {
        final TextView textView = root.findViewById(R.id.textview_tabs_section_label);
        tabViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                textView.setText(s);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        int currentDestroyedTab = getArguments().getInt(ARG_SECTION_NUMBER);
        System.out.println("onDestroyView. Tab:" + currentDestroyedTab);

        //Come back to refreshed view if it was refreshing tabs
        if (TabWearablesActivity.isRefreshingTabs()) {
            int currentRefreshingTab = TabWearablesActivity.getCurrentRefreshingTab();
            if (currentRefreshingTab == currentDestroyedTab) {
                TabWearablesActivity.setRefreshingTabs(false);
                Handler uiHandler = new Handler();
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("onDestroyView_refreshTo:" + currentRefreshingTab);
                        TabWearablesActivity.viewPager.setCurrentItem(currentRefreshingTab, true);
                    }
                });
            }
        }
    }
}