package com.offsec.nethunter;

import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.offsec.nethunter.bridge.Bridge;
import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;


public class BTFragment extends Fragment {
    private ViewPager mViewPager;
    private SharedPreferences sharedpreferences;
    private Context context;
    private Activity activity;
    private static final String ARG_SECTION_NUMBER = "section_number";

    public static BTFragment newInstance(int sectionNumber) {
        BTFragment fragment = new BTFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.bt, container, false);
        BTFragment.TabsPagerAdapter tabsPagerAdapter = new TabsPagerAdapter(getChildFragmentManager());

        mViewPager = rootView.findViewById(R.id.pagerBt);
        mViewPager.setAdapter(tabsPagerAdapter);
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                activity.invalidateOptionsMenu();
            }
        });
        sharedpreferences = activity.getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater menuinflater) {
        menuinflater.inflate(R.menu.bt, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.setup:
                SharedPreferences sharedpreferences = context.getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
                Boolean iswatch = sharedpreferences.getBoolean("running_on_wearos", false);
                if (iswatch) RunSetupWatch();
                else RunSetup();
                return true;
            case R.id.update:
                sharedpreferences = context.getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
                iswatch = sharedpreferences.getBoolean("running_on_wearos", false);
                if (iswatch) Toast.makeText(requireActivity().getApplicationContext(), "Updates have to be done manually through adb shell. If anything gone wrong at first run, please run Setup again.", Toast.LENGTH_LONG).show();
                else RunUpdate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void SetupDialog() {
        sharedpreferences = activity.getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity(), R.style.DialogStyleCompat);
        builder.setTitle("Welcome to Bluetooth Arsenal!");
        builder.setMessage("This seems to be the first run. Install the Bluetooth tools?");
        builder.setPositiveButton("Install", (dialog, which) -> {
            RunSetup();
            sharedpreferences.edit().putBoolean("setup_done", true).apply();
        });
        builder.setNegativeButton("Disable message", (dialog, which) -> {
            dialog.dismiss();
            sharedpreferences.edit().putBoolean("setup_done", true).apply();
        });
        builder.show();
    }

    public void SetupDialogWatch() {
        sharedpreferences = activity.getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity(), R.style.DialogStyleCompat);
        builder.setMessage("This seems to be the first run. Install the Bluetooth tools?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
                RunSetupWatch();
                sharedpreferences.edit().putBoolean("setup_done", true).apply();
        });
        builder.setNegativeButton("No", (dialog, which) -> {
                dialog.dismiss();
                sharedpreferences.edit().putBoolean("setup_done", true).apply();
        });
        builder.show();
    }

    public void RunSetupWatch() {
        sharedpreferences = activity.getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        run_cmd("echo -ne \"\\033]0;BT Arsenal Setup\\007\" && clear;" +
                "if [[ -f /usr/sbin/bluebinder ]]; then echo 'Bluebinder is installed!'; else wget https://raw.githubusercontent.com/yesimxev/bluebinder/master/prebuilt/armhf/bluebinder -P /usr/sbin/ && chmod +x /usr/sbin/bluebinder;fi;" +
                "if [[ -f /usr/lib/libgbinder.so.1.1.25 ]]; then echo 'libgbinder.so.1.1.25 is installed!'; else wget https://raw.githubusercontent.com/yesimxev/libgbinder/master/prebuilt/armhf/libgbinder.so.1.1.25 -P /usr/lib/ &&" +
                " ln -s libgbinder.so.1.1.25 /usr/lib/libgbinder.so.1.1 && ln -s libgbinder.so.1.1 /usr/lib/libgbinder.so.1 && ln -s libgbinder.so.1 /usr/lib/libgbinder.so;fi;" +
                "if [[ -f /usr/lib/libglibutil.so.1.0.67 ]]; then echo 'libglibutil.so.1.0.67 is installed!'; else wget https://raw.githubusercontent.com/yesimxev/libglibutil/master/prebuilt/armhf/libglibutil.so.1.0.67 -P /usr/lib/ &&" +
                " ln -s libglibutil.so.1.0.67 /usr/lib/libglibutil.so.1.0 && ln -s libglibutil.so.1.0 /usr/lib/libglibutil.so.1 && ln -s libglibutil.so.1 /usr/lib/libglibutil.so;fi;" +
                "if [[ -f /usr/bin/carwhisperer ]]; then echo 'carwhisperer is installed!'; else wget https://raw.githubusercontent.com/yesimxev/carwhisperer-0.2/master/prebuilt/armhf/carwhisperer -P /usr/bin/ && chmod +x /usr/bin/carwhisperer;fi;" +
                "if [[ -f /usr/bin/rfcomm_scan ]]; then echo 'rfcomm_scan is installed!'; else wget https://raw.githubusercontent.com/yesimxev/bt_audit/master/prebuilt/armhf/rfcomm_scan -P /usr/bin/ && chmod +x /usr/bin/rfcomm_scan;fi;" +
                "if [[ -d /root/carwhisperer ]]; then echo '/root/carwhisperer is installed!'; else git clone https://github.com/yesimxev/carwhisperer-0.2 /root/carwhisperer;fi;" +
                "if [[ -f /root/badbt/btk_server.py ]]; then echo 'BadBT is installed!'; else git clone https://github.com/yesimxev/badbt /root/badbt && cp /root/badbt/org.thanhle.btkbservice.conf /etc/dbus-1/system.d/;fi;" +
                "if [[ ! \"`grep 'noplugin=input' /etc/init.d/bluetooth`\" == \"\" ]]; then echo 'Bluetooth service is patched!'; else echo 'Patching Bluetooth service..' && " +
                "sed -i -e 's/# NOPLUGIN_OPTION=.*/NOPLUGIN_OPTION=\"--noplugin=input\"/g' /etc/init.d/bluetooth;fi;" +
                "echo 'Everything is installed! Closing in 3secs..'; sleep 3 && exit ");
        sharedpreferences.edit().putBoolean("setup_done", true).apply();
    }

    public void RunSetup() {
        sharedpreferences = activity.getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        run_cmd("echo -ne \"\\033]0;BT Arsenal Setup\\007\" && clear;apt update && apt install screen bluetooth bluez bluez-tools bluez-obexd libbluetooth3 sox spooftooph libglib2.0*-dev " +
                        "libsystemd-dev python3-dbus python3-bluez python3-pyudev python3-evdev libbluetooth-dev redfang bluelog blueranger -y;" +
                        "if [[ -f /usr/bin/carwhisperer && -f /usr/bin/rfcomm_scan ]];then echo 'All scripts are installed!'; else " +
                        "git clone https://github.com/yesimxev/carwhisperer-0.2 /root/carwhisperer;" +
                        "cd /root/carwhisperer;make && make install;git clone https://github.com/yesimxev/bt_audit /root/bt_audit;cd /root/bt_audit/src;make;" +
                        "cp rfcomm_scan /usr/bin/;fi;" +
                        "if [[ -f /usr/lib/libglibutil.so ]]; then echo 'Libglibutil is installed!'; else git clone https://github.com/yesimxev/libglibutil /root/libglibutil;" +
                        "cd /root/libglibutil;make && make install-dev;fi;" +
                        "if [[ -f /usr/lib/libgbinder.so ]]; then echo 'Libgbinder is installed!'; else git clone https://github.com/yesimxev/libgbinder /root/libgbinder;" +
                        "cd /root/libgbinder;make && make install-dev;fi;" +
                        "if [[ -f /usr/sbin/bluebinder ]]; then echo 'Bluebinder is installed!'; else git clone https://github.com/yesimxev/bluebinder /root/bluebinder;" +
                        "cd /root/bluebinder;make && make install;fi;" +
                        "if [[ -f /root/badbt/btk_server.py ]]; then echo 'BadBT is installed!'; else git clone https://github.com/yesimxev/badbt /root/badbt && cp /root/badbt/org.thanhle.btkbservice.conf /etc/dbus-1/system.d/;fi;" +
                        "if [[ ! \"`grep 'noplugin=input' /etc/init.d/bluetooth`\" == \"\" ]]; then echo 'Bluetooth service is patched!'; else echo 'Patching Bluetooth service..' && " +
                        "sed -i -e 's/.*NOPLUGIN_OPTION=\"\"/NOPLUGIN_OPTION=\"--noplugin=input\"/g' /etc/init.d/bluetooth;fi; echo 'Everything is installed!' && echo '\nPress any key to continue...' && read -s -n 1 && exit ");
                sharedpreferences.edit().putBoolean("setup_done", true).apply();
    }

    public void RunUpdate() {
        sharedpreferences = activity.getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        run_cmd("echo -ne \"\\033]0;BT Arsenal Update\\007\" && clear;apt update && apt install screen bluetooth bluez bluez-tools bluez-obexd libbluetooth3 sox spooftooph " +
                "libbluetooth-dev redfang bluelog blueranger libglib2.0*-dev libsystemd-dev python3-dbus python3-bluez python3-pyudev python3-evdev  -y;if [[ -f /usr/bin/carwhisperer && -f /usr/bin/rfcomm_scan && -f /root/bluebinder && -f /root/libgbinder && -f /root/libglibutil ]];" +
                "then cd /root/carwhisperer/;git pull && make && make install;cd /root/bluebinder/;git pull && make && make install;cd /root/libgbinder/;git pull && make && " +
                "make install-dev;cd /root/libglibutil/;git pull && make && make install-dev;cd /root/bt_audit; git pull; cd src && make;" +
                "cp rfcomm_scan /usr/bin/;cd /root/badbt/;git pull;fi; echo 'Done! Closing in 3secs..'; sleep 3 && exit ");
        sharedpreferences.edit().putBoolean("setup_done", true).apply();
    }

    public static class TabsPagerAdapter extends FragmentPagerAdapter {
        TabsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return new MainFragment();
                case 1:
                    return new ToolsFragment();
                case 2:
                    return new SpoofFragment();
                case 3:
                    return new CWFragment();
                default:
                    return new BadBtFragment();
            }
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 4:
                    return "Bad Bluetooth";
                case 3:
                    return "Carwhisperer";
                case 2:
                    return "Spoof";
                case 1:
                    return "Tools";
                case 0:
                    return "Main Page";
                default:
                    return "";
            }
        }
    }

    public static class MainFragment extends BTFragment {
        private Context context;
        final ShellExecuter exe = new ShellExecuter();
        private String selected_iface;
        private Boolean iswatch;
        String selected_addr;
        String selected_class;
        String selected_name;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            context = getContext();
        }
        @Override
        public void onResume(){
            super.onResume();
            Toast.makeText(requireActivity().getApplicationContext(), "Status updated", Toast.LENGTH_SHORT).show();
            AsyncTask.execute(() -> refresh(requireView().getRootView()));
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.bt_main, container, false);
            SharedPreferences sharedpreferences = context.getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);

            //Detecting watch
            final TextView BTMainDesc = rootView.findViewById(R.id.bt_maindesc);
            final TextView BTIface = rootView.findViewById(R.id.bt_if);
            final TextView BTService = rootView.findViewById(R.id.bt_service);

            iswatch = sharedpreferences.getBoolean("running_on_wearos", false);
            if (iswatch) {
                BTMainDesc.setVisibility(View.GONE);
                BTIface.setText("Interface");
                BTService.setText("BT Service");
            }

            //First run
            Boolean setupdone = sharedpreferences.getBoolean("setup_done", false);
            if (!setupdone.equals(true)) {
                if (iswatch) SetupDialogWatch();
                SetupDialog();
            }

            final Spinner ifaces = rootView.findViewById(R.id.hci_interface);

            //Bluebinder or bt_smd
            final TextView Binder = rootView.findViewById(R.id.bluebinder);
            File bt_smd = new File("/sys/module/hci_smd/parameters/hcismd_set");
            if (bt_smd.exists()) {
                Binder.setText("Bluetooth SMD");
            }

            //Bluetooth interfaces
            final String[] outputHCI = {""};
            AsyncTask.execute(() -> outputHCI[0] = exe.RunAsRootOutput(NhPaths.APP_SCRIPTS_PATH + "/bootkali custom_cmd hciconfig | grep hci | cut -d: -f1"));
            final ArrayList<String> hciIfaces = new ArrayList<>();
            if (outputHCI[0].isEmpty()) {
                hciIfaces.add("None");
                ifaces.setAdapter(new ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, hciIfaces));
            } else {
                final String[] ifacesArray = outputHCI[0].split("\n");
                ifaces.setAdapter(new ArrayAdapter(requireContext(),android.R.layout.simple_list_item_1, ifacesArray));
            }

            ifaces.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
                    selected_iface = parentView.getItemAtPosition(pos).toString();
                    sharedpreferences.edit().putInt("selected_iface", ifaces.getSelectedItemPosition()).apply();
                }
                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // TODO document why this method is empty
                }
            });

            //Refresh Status
            ImageButton RefreshStatus = rootView.findViewById(R.id.refreshStatus);
            RefreshStatus.setOnClickListener(v -> refresh(rootView));
            AsyncTask.execute(() -> refresh(rootView));

            //Internal bluetooth support
            final Button bluebinderButton = rootView.findViewById(R.id.bluebinder_button);
            final Button dbusButton = rootView.findViewById(R.id.dbus_button);
            final Button btButton = rootView.findViewById(R.id.bt_button);
            final Button hciButton = rootView.findViewById(R.id.hci_button);
            File hwbinder = new File("/dev/hwbinder");
            File vhci = new File("/dev/vhci");

            bluebinderButton.setOnClickListener( v -> {
                if (bluebinderButton.getText().equals("Start")) {
                    if (!bt_smd.exists() && !hwbinder.exists() && !vhci.exists()) {
                        final MaterialAlertDialogBuilder confirmbuilder = new MaterialAlertDialogBuilder(requireActivity(), R.style.DialogStyleCompat);
                        confirmbuilder.setTitle("Internal bluetooth support disabled");
                        confirmbuilder.setMessage("Your device does not support hwbinder, vhci, or bt_smd. Make sure your kernel config has the recommended drivers enabled in order to use internal bluetooth.");
                        confirmbuilder.setPositiveButton("Sure", (dialogInterface, i) -> {
                            bluebinderButton.setEnabled(false);
                            bluebinderButton.setTextColor(Color.parseColor("#40FFFFFF"));
                            dialogInterface.cancel();
                        });
                        confirmbuilder.setNegativeButton("Try anyway", (dialogInterface, i) -> dialogInterface.cancel());
                        final AlertDialog alert = confirmbuilder.create();
                        alert.show();
                    } else {
                        if (bt_smd.exists()) {
                            exe.RunAsRoot(new String[]{"svc bluetooth disable"});
                            exe.RunAsRoot(new String[]{"echo 0 > " + bt_smd});
                            exe.RunAsRoot(new String[]{"echo 1 > " + bt_smd});
                            exe.RunAsRoot(new String[]{"svc bluetooth enable"});
                        }
                        else {
                            File bluebinder = new File(NhPaths.CHROOT_PATH() + "/usr/sbin/bluebinder");
                            if (bluebinder.exists()) {
                                // Ensure all services are disabled before enabling airplane mode for bluebinder
                                exe.RunAsRoot(new String[]{
                                    "svc bluetooth disable",
                                    "svc wifi disable",
                                    "settings put global airplane_mode_on 1;am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true",
				    "pm disable com.android.bluetooth"
                                });

                                // Run the Bluebinder script
                                run_cmd("echo -ne \"\\033]0;Bluebinder\\007\" && clear;screen -A bluebinder || bluebinder;exit");
                                Toast.makeText(requireActivity().getApplicationContext(), "Starting bluebinder...", Toast.LENGTH_SHORT).show();

                                // Delay to disable airplane mode and re-enable Wi-Fi after 9 seconds
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        exe.RunAsRoot(new String[]{
                                            "settings put global airplane_mode_on 0;am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false",
                                            "svc wifi enable"
                                        });
                                    }
                                }, 9000); // 9000 milliseconds delay
                            } else {
                                Toast.makeText(requireActivity().getApplicationContext(), "Bluebinder is not installed. Launching setup..", Toast.LENGTH_SHORT).show();
                                RunSetup();
                            }
                        }
                        refresh(rootView);
                    }
                } else if (bluebinderButton.getText().equals("Stop")) {
                    if (bt_smd.exists()) {
                        exe.RunAsRoot(new String[]{"echo 0 > " + bt_smd});
                    }
                    else {
                        exe.RunAsRoot(new String[]{NhPaths.APP_SCRIPTS_PATH + "/bootkali custom_cmd pkill bluebinder;exit"});
			exe.RunAsRoot(new String[]{"pm enable com.android.bluetooth"});
			exe.RunAsRoot(new String[]{"svc bluetooth enable"});
                    }
                    refresh(rootView);
                }
            });

            //Services
            dbusButton.setOnClickListener( v -> {
                if (dbusButton.getText().equals("Start")) {
                    exe.RunAsRoot(new String[]{NhPaths.APP_SCRIPTS_PATH + "/bootkali custom_cmd service dbus start"});
                    refresh(rootView);
                } else if (dbusButton.getText().equals("Stop")) {
                    exe.RunAsRoot(new String[]{NhPaths.APP_SCRIPTS_PATH + "/bootkali custom_cmd service dbus stop"});
                    refresh(rootView);
                }
            });

            btButton.setOnClickListener( v -> {
                String dbus_statusCMD = exe.RunAsRootOutput(NhPaths.APP_SCRIPTS_PATH + "/bootkali custom_cmd service dbus status | grep dbus");
                if (dbus_statusCMD.equals("dbus is running.")) {
                    if (btButton.getText().equals("Start")) {
                        exe.RunAsRoot(new String[]{NhPaths.APP_SCRIPTS_PATH + "/bootkali custom_cmd service bluetooth start"});
                        refresh(rootView);
                    } else if (btButton.getText().equals("Stop")) {
                        exe.RunAsRoot(new String[]{NhPaths.APP_SCRIPTS_PATH + "/bootkali custom_cmd service bluetooth stop"});
                        refresh(rootView);
                    }
                } else {
                    Toast.makeText(requireActivity().getApplicationContext(), "Enable dbus service first!", Toast.LENGTH_SHORT).show();
                }
            });

            hciButton.setOnClickListener( v -> {
                if (hciButton.getText().equals("Start")) {
                    if (selected_iface.equals("None")) {
                        Toast.makeText(requireActivity().getApplicationContext(), "No interface, please refresh or check connections!", Toast.LENGTH_SHORT).show();
                    } else {
                        exe.RunAsRoot(new String[]{NhPaths.APP_SCRIPTS_PATH + "/bootkali custom_cmd hciconfig " + selected_iface + " up noscan"});
                        refresh(rootView);
                    }
                } else if (hciButton.getText().equals("Stop")) {
                    exe.RunAsRoot(new String[]{NhPaths.APP_SCRIPTS_PATH + "/bootkali custom_cmd hciconfig " + selected_iface + " down"});
                    refresh(rootView);
                }
            });

            //Scanning
            Button StartScanButton = rootView.findViewById(R.id.start_scan);
            final TextView BTtime = rootView.findViewById(R.id.bt_time);
            ListView targets = rootView.findViewById(R.id.targets);
            ShellExecuter exe = new ShellExecuter();
            File ScanLog = new File(NhPaths.CHROOT_PATH() + "/root/blue.log");
            StartScanButton.setOnClickListener( v -> {
                if (!selected_iface.equals("None")) {
                    String hci_current = exe.RunAsRootOutput(NhPaths.APP_SCRIPTS_PATH + "/bootkali custom_cmd hciconfig "+ selected_iface + " | grep 'UP RUNNING' | cut -f2 -d$'\\t'");
                    if (hci_current.equals("UP RUNNING ")) {
                        final String scantime = BTtime.getText().toString();
                        AsyncTask.execute(() -> {
                            requireActivity().runOnUiThread(() -> {
                                final ArrayList<String> scanning = new ArrayList<>();
                                scanning.add("Scanning..");
                                targets.setAdapter(new ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, scanning));
                            });
                            exe.RunAsRoot(new String[]{NhPaths.APP_SCRIPTS_PATH + "/bootkali custom_cmd rm /root/blue.log"});
                            exe.RunAsRoot(new String[]{NhPaths.APP_SCRIPTS_PATH + "/bootkali custom_cmd timeout " + scantime + " bluelog -i " + selected_iface + " -ncqo /root/blue.log;hciconfig " + selected_iface + " noscan"});
                             requireActivity().runOnUiThread(() -> {
                                 String outputScanLog = exe.RunAsRootOutput("cat " + ScanLog);
                                 final String[] targetsArray = outputScanLog.split("\n");
                                 ArrayAdapter targetsadapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, targetsArray);
                                 if (!outputScanLog.isEmpty()) {
                                     targets.setAdapter(targetsadapter);
                                 } else {
                                     final ArrayList<String> notargets = new ArrayList<>();
                                     notargets.add("No devices found");
                                     targets.setAdapter(new ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, notargets));
                                 }
                             });
                        });
                    } else
                        Toast.makeText(requireActivity().getApplicationContext(), "Interface is down!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(requireActivity().getApplicationContext(), "No interface selected!", Toast.LENGTH_SHORT).show();
                }
            });

            //Target selection
            targets.setOnItemClickListener((adapterView, view, i, l) -> {
                String selected_target = targets.getItemAtPosition(i).toString();
                if (selected_target.equals("No devices found"))
                    Toast.makeText(requireActivity().getApplicationContext(), "No target!", Toast.LENGTH_SHORT).show();
                else {
                    selected_addr = exe.RunAsRootOutput("echo " + selected_target + " | cut -d , -f 1");
                    selected_class = exe.RunAsRootOutput("echo " + selected_target + " | cut -d , -f 2");
                    selected_name = exe.RunAsRootOutput("echo " + selected_target + " | cut -d , -f 3");
                    PreferencesData.saveString(context, "selected_address", selected_addr);
                    PreferencesData.saveString(context, "selected_class", selected_class);
                    PreferencesData.saveString(context, "selected_name", selected_name);
                    Toast.makeText(requireActivity().getApplicationContext(), "Target selected!", Toast.LENGTH_SHORT).show();
                }
            });
            return rootView;
        }

        //Refresh main
        private void refresh(View BTFragment) {
            final TextView Binderstatus = BTFragment.findViewById(R.id.BinderStatus);
            final TextView DBUSstatus = BTFragment.findViewById(R.id.DBUSstatus);
            final TextView BTstatus = BTFragment.findViewById(R.id.BTstatus);
            final TextView HCIstatus = BTFragment.findViewById(R.id.HCIstatus);
            final Button bluebinderButton = BTFragment.findViewById(R.id.bluebinder_button);
            final Button dbusButton = BTFragment.findViewById(R.id.dbus_button);
            final Button btButton = BTFragment.findViewById(R.id.bt_button);
            final Button hciButton = BTFragment.findViewById(R.id.hci_button);
            final Spinner ifaces = BTFragment.findViewById(R.id.hci_interface);
            SharedPreferences sharedpreferences = context.getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);

            requireActivity().runOnUiThread(() -> {
                String outputHCI = exe.RunAsRootOutput(NhPaths.APP_SCRIPTS_PATH + "/bootkali custom_cmd hciconfig | grep hci | cut -d: -f1");
                final ArrayList<String> hciIfaces = new ArrayList<>();
                if (outputHCI.isEmpty()) {
                    hciIfaces.add("None");
                    ifaces.setAdapter(new ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, hciIfaces));
                } else {
                    final String[] ifacesArray = outputHCI.split("\n");
                    ifaces.setAdapter(new ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, ifacesArray));
                    int lastiface = sharedpreferences.getInt("selected_iface", 0);
                    ifaces.setSelection(lastiface);
                }
                String binder_statusCMD = exe.RunAsRootOutput("pidof bluebinder");
                File bt_smd = new File("/sys/module/hci_smd/parameters/hcismd_set");
                if (!bt_smd.exists()) {
                    if (binder_statusCMD.isEmpty()) {
                        Binderstatus.setText("Stopped");
                        bluebinderButton.setText("Start");
                    }
                    else {
                        Binderstatus.setText("Running");
                        bluebinderButton.setText("Stop");
                    }
                } else {
                    if (outputHCI.contains("hci0")) {
                        Binderstatus.setText("Enabled");
                        bluebinderButton.setText("Stop");
                    } else {
                        Binderstatus.setText("Disabled");
                        bluebinderButton.setText("Start");
                    }
                }
                String dbus_statusCMD = exe.RunAsRootOutput(NhPaths.APP_SCRIPTS_PATH + "/bootkali custom_cmd service dbus status | grep dbus");
                if (dbus_statusCMD.equals("dbus is running.")) {
                    DBUSstatus.setText("Running");
                    dbusButton.setText("Stop");
                }
                else {
                    DBUSstatus.setText("Stopped");
                    dbusButton.setText("Start");
                }
                String bt_statusCMD = exe.RunAsRootOutput(NhPaths.APP_SCRIPTS_PATH + "/bootkali custom_cmd service bluetooth status | grep bluetooth");
                if (bt_statusCMD.equals("bluetooth is running.")) {
                    BTstatus.setText("Running");
                    btButton.setText("Stop");
                }
                else {
                    BTstatus.setText("Stopped");
                    btButton.setText("Start");
                }
                String hci_statusCMD = exe.RunAsRootOutput(NhPaths.APP_SCRIPTS_PATH + "/bootkali custom_cmd hciconfig "+ selected_iface + " | grep 'UP RUNNING' | cut -f2 -d$'\\t'");
                if (hci_statusCMD.equals("UP RUNNING ")) {
                    HCIstatus.setText("Up");
                    hciButton.setText("Stop");
                }
                else {
                    HCIstatus.setText("Down");
                    hciButton.setText("Start");
                }
            });
        }
    }

    public static class ToolsFragment extends BTFragment {
        private Context context;
        private Activity activity;
        final ShellExecuter exe = new ShellExecuter();
        private String reverse = "";
        private String flood = "";

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            context = getContext();
            activity = getActivity();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.bt_tools, container, false);
            final EditText hci_interface = rootView.findViewById(R.id.hci_interface);
            CheckBox floodCheckBox = rootView.findViewById(R.id.l2ping_flood);
            CheckBox reverseCheckBox = rootView.findViewById(R.id.l2ping_reverse);

            //Target address
            final EditText sdp_address = rootView.findViewById(R.id.sdp_address);

            //Set target
            Button SetTarget = rootView.findViewById(R.id.set_target);

            SetTarget.setOnClickListener( v -> {
                String selected_addr = PreferencesData.getString(context, "selected_address", "");
                sdp_address.setText(selected_addr);
            });

            //L2ping
            Button StartL2ping = rootView.findViewById(R.id.start_l2ping);
            final EditText l2ping_Size = rootView.findViewById(R.id.l2ping_size);
            final EditText l2ping_Count = rootView.findViewById(R.id.l2ping_count);
            final EditText redfang_Range = rootView.findViewById(R.id.redfang_range);
            final EditText redfang_Log = rootView.findViewById(R.id.redfang_log);

            // Checkbox for flood and reverse ping
            floodCheckBox.setOnClickListener( v -> {
                if (floodCheckBox.isChecked())
                    flood = " -f ";
                else
                    flood = "";
            });
            reverseCheckBox.setOnClickListener( v -> {
                if (reverseCheckBox.isChecked())
                    reverse = " -r ";
                else
                    reverse = "";
            });

            StartL2ping.setOnClickListener( v -> {
                String l2ping_target = sdp_address.getText().toString();
                if (!l2ping_target.equals("")) {
                    String l2ping_size = l2ping_Size.getText().toString();
                    String l2ping_count = l2ping_Count.getText().toString();
                    String l2ping_interface = hci_interface.getText().toString();
                    run_cmd("echo -ne \"\\033]0;Pinging BT device\\007\" && clear;l2ping -i " + l2ping_interface + " -s " + l2ping_size + " -c " + l2ping_count + flood + reverse + " " + l2ping_target + " && echo \"\nPinging done, closing in 3 secs..\";sleep 3 && exit");
                } else {
                    Toast.makeText(requireActivity().getApplicationContext(), "No target address!", Toast.LENGTH_SHORT).show();
                }
            });

            //RFComm_scan
            Button StartRFCommscan = rootView.findViewById(R.id.start_rfcommscan);

            StartRFCommscan.setOnClickListener( v -> {
                String sdp_target = sdp_address.getText().toString();
                if (!sdp_target.isEmpty())
                    run_cmd("echo -ne \"\\033]0;RFComm Scan\\007\" && clear;rfcomm_scan " + sdp_target);
                else
                    Toast.makeText(requireActivity().getApplicationContext(), "No target address!", Toast.LENGTH_SHORT).show();
            });

            //Redfang
            Button StartRedfang = rootView.findViewById(R.id.start_redfang);

            StartRedfang.setOnClickListener( v -> {
                String redfang_range = redfang_Range.getText().toString();
                String redfang_logfile = redfang_Log.getText().toString();
                if (!redfang_range.isEmpty())
                    run_cmd("echo -ne \"\\033]0;Redfang\\007\" && clear;fang -r " + redfang_range + " -o " + redfang_logfile);
                else
                    Toast.makeText(requireActivity().getApplicationContext(), "No target range!", Toast.LENGTH_SHORT).show();
            });

            //Blueranger
            Button StartBlueranger = rootView.findViewById(R.id.start_blueranger);
            StartBlueranger.setOnClickListener( v -> {
                String blueranger_target = sdp_address.getText().toString();
                String blueranger_interface = hci_interface.getText().toString();
                if (!blueranger_target.isEmpty())
                    run_cmd("echo -ne \"\\033]0;Blueranger\\007\" && clear;blueranger " + blueranger_interface + " " + blueranger_target);
                else
                    Toast.makeText(requireActivity().getApplicationContext(), "No target address!", Toast.LENGTH_SHORT).show();
            });

            //Start SDP Tool
            Button StartSDPButton = rootView.findViewById(R.id.start_sdp);
            StartSDPButton.setOnClickListener( v -> {
                Toast.makeText(getContext(), "Discovery started..\nCheck the output below", Toast.LENGTH_SHORT).show();
                AsyncTask.execute(() -> startSDPtool(rootView));
            });
            return rootView;
        }

        private void startSDPtool(View BTFragment) {
            final EditText sdp_address = BTFragment.findViewById(R.id.sdp_address);
            final EditText hci_interface = BTFragment.findViewById(R.id.hci_interface);
            final TextView output = BTFragment.findViewById(R.id.SDPoutput);
            ShellExecuter exe = new ShellExecuter();
            String sdp_target = sdp_address.getText().toString();
            String sdp_interface = hci_interface.getText().toString();

            requireActivity().runOnUiThread(() -> {
                if (!sdp_target.isEmpty()) {
                    String CMDout = exe.RunAsRootOutput(NhPaths.APP_SCRIPTS_PATH + "/bootkali custom_cmd sdptool -i " + sdp_interface + " browse " + sdp_target + " | sed '/^\\[/d' | sed '/^Linux/d'");
                    output.setText(CMDout);
                } else
                    Toast.makeText(requireActivity().getApplicationContext(), "No target address!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    public static class SpoofFragment extends BTFragment {
        private Context context;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            context = getContext();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.bt_spoof, container, false);

            //Selected iface
            final EditText spoof_interface = rootView.findViewById(R.id.spoof_interface);

            //Target address
            final EditText targetAddress = rootView.findViewById(R.id.targetAddress);

            //Target Class
            final EditText targetClass = rootView.findViewById(R.id.targetClass);

            //Target Name
            final EditText targetName = rootView.findViewById(R.id.targetName);

            //Set target
            Button SetTarget = rootView.findViewById(R.id.set_target);

            SetTarget.setOnClickListener(v -> {
                String selected_address = PreferencesData.getString(context, "selected_address", "");
                String selected_class = PreferencesData.getString(context, "selected_class", "");
                String selected_name = PreferencesData.getString(context, "selected_name", "");
                targetAddress.setText(selected_address);
                targetClass.setText(selected_class);
                targetName.setText(selected_name);
            });

            //Refresh
            Button RefreshStatus = rootView.findViewById(R.id.refreshSpoof);
            RefreshStatus.setOnClickListener(v -> refreshSpoof(rootView));

            //Apply
            Button ApplySpoof = rootView.findViewById(R.id.apply_spoof);

            ApplySpoof.setOnClickListener(v -> {
                String target_interface = spoof_interface.getText().toString();
                String target_address = " -a " + targetAddress.getText().toString();
                String target_class = " -c " + targetClass.getText().toString();
                String target_name = " -n \"" + targetName.getText().toString() + "\"";
                if (target_class.equals(" -c ")) target_class = "";
                if (target_name.equals(" -n \"\"")) target_name = "";
                if (target_address.equals(" -a ") && target_name.isEmpty() && target_class.isEmpty()) {
                    Toast.makeText(requireActivity().getApplicationContext(), "Please enter at least one parameter!", Toast.LENGTH_SHORT).show();
                } else {
                    final String target_classname = target_class + target_name;
                    if (!target_address.equals(" -a ")) {
                        run_cmd("echo -ne \"\\033]0;Spoofing Bluetooth\\007\" && clear;echo 'Spooftooph started..';spooftooph -i " + target_interface + target_address +
                                "; sleep 2 && hciconfig " + target_interface + " up && spooftooph -i " + target_interface + target_classname + " && echo '\nBringing interface up with hciconfig..\n\nClass/Name changed, closing in 3 secs..';sleep 3 && exit");
                    } else {
                        run_cmd("echo -ne \"\\033]0;Spoofing Bluetooth\\007\" && clear;echo 'Spooftooph started..';spooftooph -i " + target_interface + target_classname + " && echo '\nClass/Name changed, closing in 3 secs..';sleep 3 && exit");
                    }
                }
            });
            return rootView;
        }

        private void refreshSpoof(View BTFragment) {
            ShellExecuter exe = new ShellExecuter();
            final EditText spoof_interface = BTFragment.findViewById(R.id.spoof_interface);
            final TextView currentAddress = BTFragment.findViewById(R.id.currentAddress);
            final TextView currentClass = BTFragment.findViewById(R.id.currentClass);
            final TextView currentClassType = BTFragment.findViewById(R.id.currentClassType);
            final TextView currentName = BTFragment.findViewById(R.id.currentName);

            requireActivity().runOnUiThread(() -> {
                String selectedIface = spoof_interface.getText().toString();
                String currentAddress_CMD = exe.RunAsRootOutput(NhPaths.APP_SCRIPTS_PATH + "/bootkali custom_cmd hciconfig " + selectedIface + " | awk '/Address/ { print $3 }'");
                if (!currentAddress_CMD.isEmpty()) {
                    currentAddress.setText(currentAddress_CMD);

                    String currentClassCMD = exe.RunAsRootOutput(NhPaths.APP_SCRIPTS_PATH + "/bootkali custom_cmd hciconfig " + selectedIface + " -a | awk '/Class:/ { print $2 }' | sed '/^Class:/d'");
                    currentClass.setText(currentClassCMD);

                    String currentClassTypeCMD = exe.RunAsRootOutput(NhPaths.APP_SCRIPTS_PATH + "/bootkali custom_cmd hciconfig " + selectedIface + " -a | awk '/Device Class:/ { print $3, $4, $5 }'");
                    currentClassType.setText(currentClassTypeCMD);

                    String currentNameCMD = exe.RunAsRootOutput(NhPaths.APP_SCRIPTS_PATH + "/bootkali custom_cmd hciconfig " + selectedIface + " -a | grep Name | cut -d\\\' -f2");
                    currentName.setText(currentNameCMD);
                } else
                    Toast.makeText(requireActivity().getApplicationContext(), "Interface is down!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    public static class CWFragment extends BTFragment {
        private Context context;
        private String selected_mode;
        final ShellExecuter exe = new ShellExecuter();
        private Boolean iswatch;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            context = getContext();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.bt_carwhisperer, container, false);

            SharedPreferences sharedpreferences = context.getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);

            final TextView CWdesc = rootView.findViewById(R.id.carwhisp_desc);
            iswatch = sharedpreferences.getBoolean("running_on_wearos", false);
            if (iswatch) {
                CWdesc.setVisibility(View.GONE);
            }

            //Selected iface
            final EditText cw_interface = rootView.findViewById(R.id.hci_interface);

            //Target address
            final EditText cw_address = rootView.findViewById(R.id.hci_address);

            //Set target
            Button SetTarget = rootView.findViewById(R.id.set_target);

            SetTarget.setOnClickListener( v -> {
                String selected_address = PreferencesData.getString(context, "selected_address", "");
                cw_address.setText(selected_address);
            });

            //Channel
            final EditText hci_channel = rootView.findViewById(R.id.hci_channel);

            //CW Mode
            Spinner cwmode = rootView.findViewById(R.id.cwmode);
            final ArrayList<String> modes = new ArrayList<>();
            modes.add("Listen");
            modes.add("Inject");
            cwmode.setAdapter(new ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, modes));
            cwmode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
                    selected_mode = parentView.getItemAtPosition(pos).toString();
                }
                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });

            //Listening
            final EditText listenfilename = rootView.findViewById(R.id.listenfilename);

            //Injecting
            final EditText injectfilename = rootView.findViewById(R.id.injectfilename);
            final Button injectfilebrowse = rootView.findViewById(R.id.injectfilebrowse);

            injectfilebrowse.setOnClickListener( v -> {
                Intent intent = new Intent();
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("audio/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select audio file"),1001);
                });

            //Launch
            Button StartCWButton = rootView.findViewById(R.id.start_cw);
            StartCWButton.setOnClickListener( v -> {
                String cw_iface = cw_interface.getText().toString();
                String cw_target = cw_address.getText().toString();
                if (!cw_target.equals("")) {
                    String cw_channel = hci_channel.getText().toString();
                    String cw_listenfile = listenfilename.getText().toString();
                    String cw_injectfile = injectfilename.getText().toString();

                    if (selected_mode.equals("Listen")) {
                        run_cmd("echo -ne \"\\033]0;Listening BT audio\\007\" && clear;echo 'Carwhisperer starting..\nReturn to NetHunter to kill, or to listen live!'$'\n';carwhisperer " + cw_iface + " /root/carwhisperer/in.raw /sdcard/rec.raw " + cw_target + " " + cw_channel +
                                " && echo 'Converting to wav to target directory..';sox -t raw -r 8000 -e signed -b 16 /sdcard/rec.raw -r 8000 -b 16 /sdcard/" + cw_listenfile + ";echo Done! || echo 'No convert file!';sleep 3 && exit");
                    } else if (selected_mode.equals("Inject")) {
                        run_cmd("echo -ne \"\\033]0;Injecting BT audio\\007\" && clear;echo 'Carwhisperer starting..';length=$(($(soxi -D '" + cw_injectfile + "' | cut -d. -f1)+8));sox '" + cw_injectfile + "' -r 8000 -b 16 -c 1 tempi.raw && timeout $length " +
                                "carwhisperer " + cw_iface + " tempi.raw tempo.raw " + cw_target + " " + cw_channel + "; rm tempi.raw && rm tempo.raw;echo '\nInjection done, closing in 3 secs..';sleep 3 && exit");
                    }
                } else
                    Toast.makeText(requireActivity().getApplicationContext(), "No target address!", Toast.LENGTH_SHORT).show();
            });

            //Kill
            Button StopCWButton = rootView.findViewById(R.id.stop_cw);
            StopCWButton.setOnClickListener( v -> {
                    exe.RunAsRoot(new String[]{NhPaths.APP_SCRIPTS_PATH + "/bootkali custom_cmd pkill carwhisperer"});
                    Toast.makeText(requireActivity().getApplicationContext(), "Killed", Toast.LENGTH_SHORT).show();
                    });

            //Stream or play audio
            ImageButton PlayAudioButton = rootView.findViewById(R.id.play_audio);
            ImageButton StopAudioButton = rootView.findViewById(R.id.stop_audio);
            AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, 20000, AudioTrack.MODE_STREAM);
            PlayAudioButton.setOnClickListener( v -> {
                File cw_listenfile = new File(NhPaths.SD_PATH + "/rec.raw");
                if (cw_listenfile.length() == 0) {
                    Toast.makeText(getContext(), "File not found!", Toast.LENGTH_SHORT).show();
                } else {
                    AsyncTask.execute(() -> {
                        InputStream s = null;
                        try {
                            s = new FileInputStream(cw_listenfile);
                        } catch (NullPointerException | IOException e) {
                            e.printStackTrace();
                        }
                        audioTrack.play();
                        // Reading data.
                        byte[] data = new byte[200];
                        int n = 0;
                        try {
                            while (true) {
                                assert s != null;
                                if ((n = s.read(data)) == -1) break;
                                synchronized (audioTrack) {
                                    audioTrack.write(data, 0, n);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            });
            StopAudioButton.setOnClickListener(v -> {
                        audioTrack.pause();
                        audioTrack.flush();
            });
            return rootView;
        }
    }
    public static class BadBtFragment extends BTFragment {
        private Context context;
        private String selected_badbtmode;
        private String selected_preset;
        private String selected_preset_uac;
        private String selected_prefix;
        private String selected_badbt_class;
        String prefixCMD = "";
        String uacCMD = "";
        final ShellExecuter exe = new ShellExecuter();

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            context = getContext();
        }

        @Override
        public void onResume(){
            super.onResume();
            Toast.makeText(requireActivity().getApplicationContext(), "Status updated", Toast.LENGTH_SHORT).show();
            AsyncTask.execute(() -> refresh_badbt(requireView().getRootView()));
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.bt_badbt, container, false);
            final Button badbtServerButton = rootView.findViewById(R.id.badbtserver_button);
            SharedPreferences sharedpreferences = context.getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
            boolean iswatch = requireContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_WATCH);

            //Watch optimisation
            final TextView BadBTdesc = rootView.findViewById(R.id.badbt_desc);
            if (iswatch) {
                BadBTdesc.setVisibility(View.GONE);
            }

            //Selected iface, name, bdaddr, class
            final EditText badbt_interface = rootView.findViewById(R.id.badbt_interface);
            final EditText badbt_name = rootView.findViewById(R.id.badbt_name);
            final EditText badbt_bdaddr = rootView.findViewById(R.id.badbt_address);
            final EditText badbt_class = rootView.findViewById(R.id.badbt_class);

            //Class spinner
            Spinner badbtclass = rootView.findViewById(R.id.badbt_class_spinner);
            final ArrayList<String> classes = new ArrayList<>();
            classes.add("Keyboard");
            classes.add("Headset");
            classes.add("Speaker");
            classes.add("Mouse");
            classes.add("Printer");
            classes.add("PC");
            classes.add("Mobile");
            classes.add("Custom");
            badbtclass.setAdapter(new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, classes));
            badbtclass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
                    selected_prefix = parentView.getItemAtPosition(pos).toString();
                    if (selected_prefix.equals("Keyboard")) {
                        badbt_class.setText("0x000540");
                    } else if (selected_prefix.equals("Headset")) {
                        badbt_class.setText("0x000408");
                    } else if (selected_prefix.equals("Speaker")) {
                        badbt_class.setText("0x240414");
                    } else if (selected_prefix.equals("Mouse")) {
                        badbt_class.setText("0x002580");
                    } else if (selected_prefix.equals("Printer")) {
                        badbt_class.setText("0x040680");
                    } else if (selected_prefix.equals("PC")) {
                        badbt_class.setText("0x02010c");
                    } else if (selected_prefix.equals("Mobile")) {
                        badbt_class.setText("0x000204");
                    } else if (selected_prefix.equals("Custom")) {
                        badbt_class.setText("");
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });


            //Refresh
            refresh_badbt(rootView);
            String prevbadbtname = sharedpreferences.getString("badbt-name", "");
            if (!prevbadbtname.isEmpty()) badbt_name.setText(prevbadbtname);
            String prevbadbtiface = sharedpreferences.getString("badbt-iface", "");
            if (!prevbadbtiface.isEmpty()) badbt_interface.setText(prevbadbtiface);
            String prevbadbtaddr = sharedpreferences.getString("badbt-bdaddr", "");
            if (!prevbadbtaddr.isEmpty()) badbt_bdaddr.setText(prevbadbtaddr);
            String prevbadbtclass = sharedpreferences.getString("badbt-class", "");
            if (!prevbadbtclass.isEmpty()) badbt_class.setText(prevbadbtclass);

            //Refresh Status
            ImageButton RefreshBadBTStatus = rootView.findViewById(R.id.refreshBadBTStatus);
            RefreshBadBTStatus.setOnClickListener(v -> refresh_badbt(rootView));

            //String
            final EditText badbt_string = rootView.findViewById(R.id.editBadBT);

            //Services
            badbtServerButton.setOnClickListener( v -> {
                if (badbtServerButton.getText().equals("Start")) {
                    String BadBT_name = badbt_name.getText().toString();
                    String BadBT_iface = badbt_interface.getText().toString();
                    String BadBT_bdaddr = badbt_bdaddr.getText().toString();
                    String BadBT_class = badbt_class.getText().toString();
                    String dbus_statusCMD = exe.RunAsRootOutput(NhPaths.APP_SCRIPTS_PATH + "/bootkali custom_cmd service dbus status | grep dbus");
                    String bt_statusCMD = exe.RunAsRootOutput(NhPaths.APP_SCRIPTS_PATH + "/bootkali custom_cmd service bluetooth status | grep bluetooth");
                    String bt_ifaceCMD = exe.RunAsRootOutput(NhPaths.APP_SCRIPTS_PATH + "/bootkali custom_cmd hciconfig | grep hci");
                    sharedpreferences.edit().putString("badbt-name", BadBT_name).apply();
                    sharedpreferences.edit().putString("badbt-iface", BadBT_iface).apply();
                    sharedpreferences.edit().putString("badbt-bdaddr", BadBT_bdaddr).apply();
                    sharedpreferences.edit().putString("badbt-class", BadBT_class).apply();

                    if (dbus_statusCMD.equals("dbus is running.") && bt_statusCMD.equals("bluetooth is running.") && !bt_ifaceCMD.isEmpty()) {
                        if (!BadBT_name.isEmpty() && !BadBT_iface.isEmpty() && !BadBT_bdaddr.isEmpty()) {
                            Toast.makeText(requireActivity().getApplicationContext(), "Starting server...", Toast.LENGTH_SHORT).show();
                            run_cmd("echo -ne \"\\033]0;BadBT Server\\007\" && clear;python3 /root/badbt/btk_server.py -n '"
                                    + BadBT_name + "' -i " + BadBT_iface + " -c " + BadBT_class + " -a " + BadBT_bdaddr + "&;sleep 1 && echo 'Starting agent...' && sleep 1 && bluetoothctl --agent NoInputNoOutput && exit");
                            refresh_badbt(rootView);
                        } else {
                            Toast.makeText(requireActivity().getApplicationContext(), "Please enter interface, keyboard name, and address!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireActivity().getApplicationContext(), "Bluetooth interface or service not running!", Toast.LENGTH_LONG).show();
                    }
                } else if (badbtServerButton.getText().equals("Stop")) {
                    exe.RunAsRoot(new String[]{"kill `ps -ef | grep '[btk]_server' | awk {'print $2'}`"});
                    exe.RunAsRoot(new String[]{"pkill bluetoothctl"});
                    refresh_badbt(rootView);
                }
            });

            //Mode
            Spinner badbtmode = rootView.findViewById(R.id.badbtmode);
            View BadBTSettingsView = rootView.findViewById(R.id.badbtsettings_layout);
            final ArrayList<String> modes = new ArrayList<>();
            modes.add("Send strings");
            modes.add("Interactive");
            badbtmode.setAdapter(new ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, modes));
            badbtmode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
                    selected_badbtmode = parentView.getItemAtPosition(pos).toString();
                    if (selected_badbtmode.equals("Interactive")) {
                        BadBTSettingsView.setVisibility(View.GONE);
                    } else if (selected_badbtmode.equals("Send strings")){
                        BadBTSettingsView.setVisibility(View.VISIBLE);
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });

            //Prefix
            CheckBox uacCheckBox = rootView.findViewById(R.id.uac_bypass);
            View BadBTUACView = rootView.findViewById(R.id.badbtuac_layout);
            Spinner badbtprefix = rootView.findViewById(R.id.badbtprefix);
            Spinner badbtpresets_uac = rootView.findViewById(R.id.badbtpresets_uac);
            final ArrayList<String> presets_uac = new ArrayList<>();
            final ArrayList<String> prefixes = new ArrayList<>();
            prefixes.add("Mobile Home");
            prefixes.add("Mobile Browser");
            prefixes.add("Windows CMD");
            prefixes.add("Mac Terminal");
            prefixes.add("Linux Terminal");
            prefixes.add("None");
            badbtprefix.setAdapter(new ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, prefixes));
            badbtprefix.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
                    selected_prefix = parentView.getItemAtPosition(pos).toString();
                    if (selected_prefix.equals("Mobile Home")) {
                        BadBTUACView.setVisibility(View.GONE);
                        prefixCMD = "mobile";
                        uacCheckBox.setChecked(false);
                        presets_uac.clear();
                        presets_uac.add("None");
                        badbtpresets_uac.setAdapter(new ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, presets_uac));
                        uacCMD = "-";
                    } else if (selected_prefix.equals("Mobile Browser")) {
                        BadBTUACView.setVisibility(View.GONE);
                        prefixCMD = "mobilewww";
                        uacCheckBox.setChecked(false);
                        presets_uac.clear();
                        presets_uac.add("None");
                        badbtpresets_uac.setAdapter(new ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, presets_uac));
                        uacCMD = "-";
                    } else if (selected_prefix.equals("Windows CMD")) {
                        BadBTUACView.setVisibility(View.VISIBLE);
                        prefixCMD = "windows";
                    } else if (selected_prefix.equals("Mac Terminal")) {
                        BadBTUACView.setVisibility(View.GONE);
                        prefixCMD = "mac";
                        uacCheckBox.setChecked(false);
                        presets_uac.clear();
                        presets_uac.add("None");
                        badbtpresets_uac.setAdapter(new ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, presets_uac));
                        uacCMD = "-";
                    } else if (selected_prefix.equals("Linux Terminal")) {
                        BadBTUACView.setVisibility(View.GONE);
                        prefixCMD = "linux";
                        uacCheckBox.setChecked(false);
                        presets_uac.clear();
                        presets_uac.add("None");
                        badbtpresets_uac.setAdapter(new ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, presets_uac));
                        uacCMD = "-";
                    } else if (selected_prefix.equals("None")) {
                        BadBTUACView.setVisibility(View.GONE);
                        uacCMD = "-";
                        uacCheckBox.setChecked(false);
                        presets_uac.clear();
                        presets_uac.add("None");
                        badbtpresets_uac.setAdapter(new ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, presets_uac));
                        uacCMD = "-";
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });

            //Presets
            Spinner badbtpresets = rootView.findViewById(R.id.badbtpresets);
            EditText badbtstring = rootView.findViewById(R.id.editBadBT);
            final ArrayList<String> presets = new ArrayList<>();
            presets.add("Rickroll");
            presets.add("Fake Windows Update");
            presets.add("None");
            badbtpresets.setAdapter(new ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, presets));
            badbtpresets.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
                    selected_preset = parentView.getItemAtPosition(pos).toString();
                    if (selected_preset.equals("Rickroll")) {
                        badbtstring.setText("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
                    } else if (selected_preset.equals("Fake Windows Update")) {
                        badbtstring.setText("iexplore -k http://fakeupdate.net/win10ue");
                    } else if (selected_preset.equals("None")) {
                        badbtstring.setText("");
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });

            //UAC
            uacCheckBox.setOnClickListener( v -> {
                if (uacCheckBox.isChecked()) {
                    badbtpresets_uac.setVisibility(View.VISIBLE);
                    presets_uac.clear();
                    presets_uac.add("Windows 7");
                    presets_uac.add("Windows 8");
                    presets_uac.add("Windows 10");
                    presets_uac.add("Windows 11");
                    badbtpresets_uac.setAdapter(new ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, presets_uac));
                }
                else {
                    presets_uac.clear();
                    presets_uac.add("None");
                    badbtpresets_uac.setAdapter(new ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, presets_uac));
                    badbtpresets_uac.setVisibility(View.GONE);
                    uacCMD = "-";
                }
            });
            badbtpresets_uac.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
                    selected_preset_uac = parentView.getItemAtPosition(pos).toString();
                    if (selected_preset_uac.equals("Windows 7")) {
                        uacCMD = "win7";
                    } else if (selected_preset_uac.equals("Windows 8")) {
                        uacCMD = "win8";
                    } else if (selected_preset_uac.equals("Windows 10")) {
                        uacCMD = "win10";
                    } else if (selected_preset_uac.equals("Windows 11")) {
                        uacCMD = "win11";
                    } else if (selected_preset.equals("None")) {
                        uacCMD = "-";
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });

            //Load from file
            final Button injectStringButton = rootView.findViewById(R.id.injectstringbrowse);
            injectStringButton.setOnClickListener( v -> {
                Intent intent2 = new Intent();
                intent2.addCategory(Intent.CATEGORY_OPENABLE);
                intent2.setType("text/*");
                intent2.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent2, "Select text file"),1002);
            });

            //Start
            Button StartBadBtButton = rootView.findViewById(R.id.start_badbt);
            StartBadBtButton.setOnClickListener( v -> {
                    if (selected_badbtmode.equals("Send strings")) {
                        String BadBT_string = badbt_string.getText().toString();
                        run_cmd("echo -ne \"\\033]0;BadBT Send Strings\\007\" && clear;python3 /root/badbt/send_string.py '" + BadBT_string + "' " + prefixCMD + " " + uacCMD + ";sleep 2 && echo 'Exiting..' && exit");
                        Toast.makeText(requireActivity().getApplicationContext(), "Sending strings..", Toast.LENGTH_SHORT).show();
                        } else if (selected_badbtmode.equals("Interactive")) {
                        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity(), R.style.DialogStyleCompat);
                        builder.setTitle("Are you sure?");
                        builder.setMessage("Interactive mode will run in NetHunter terminal, but needs a physical keyboard connected as of now.");
                        builder.setPositiveButton("Ok", (dialog, which) -> {
                            run_cmd("echo -ne \"\\033]0;BadBT Client\\007\" && clear;python3 /root/badbt/kb_client.py");
                            Toast.makeText(requireActivity().getApplicationContext(), "Starting keyboard client..", Toast.LENGTH_SHORT).show();
                        });
                        builder.setNegativeButton("Cancel", (dialog, which) -> {
                        });
                        builder.show();
                    }
        });

            return rootView;
        }

        //Refresh badbt
        private void refresh_badbt(View BTFragment) {

            final TextView BadBTServerStatus = BTFragment.findViewById(R.id.BadBTServerStatus);
            final Button badbtserverButton = BTFragment.findViewById(R.id.badbtserver_button);
            SharedPreferences sharedpreferences = context.getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);

            requireActivity().runOnUiThread(() -> {
                String badbtserver_statusCMD = exe.RunAsRootOutput("ps -ef | grep btk_server");
                if (!badbtserver_statusCMD.contains("btk_server.py")) {
                    BadBTServerStatus.setText("Stopped");
                    badbtserverButton.setText("Start");
                }
                else {
                    BadBTServerStatus.setText("Running");
                    badbtserverButton.setText("Stop");
                }
            });
        }
    }
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 1001 && (resultCode == Activity.RESULT_OK)) {
                    ShellExecuter exe = new ShellExecuter();
                    EditText injectfilename = requireActivity().findViewById(R.id.injectfilename);
                    String FilePath = Objects.requireNonNull(data.getData()).getPath();
                    FilePath = exe.RunAsRootOutput("echo " + FilePath + " | sed -e 's/\\/document\\/primary:/\\/sdcard\\//g' ");
                    injectfilename.setText(FilePath);
            }
            if (requestCode == 1002) {
                if (resultCode == Activity.RESULT_OK) {
                    ShellExecuter exe = new ShellExecuter();
                    EditText badbtstring = requireActivity().findViewById(R.id.editBadBT);
                    String FilePath = Objects.requireNonNull(data.getData()).getPath();
                    Toast.makeText(requireActivity().getApplicationContext(), FilePath, Toast.LENGTH_SHORT).show();
                    FilePath = exe.RunAsRootOutput("echo " + FilePath + " | sed -e 's/\\/document\\/primary:/\\/sdcard\\//g' ");
                    FilePath = exe.RunAsRootOutput("cat " + FilePath);
                    badbtstring.setText(FilePath);
                }
            }
        }

    public static class PreferencesData {
        public static void saveString(Context context, String key, String value) {
            SharedPreferences sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(context);
            sharedPrefs.edit().putString(key, value).apply();
        }

        public static String getString(Context context, String key, String defaultValue) {
            SharedPreferences sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(context);
            return sharedPrefs.getString(key, defaultValue);
        }
    }

    ////
    // Bridge side functions
    ////

    public void run_cmd(String cmd) {
        Intent intent = Bridge.createExecuteIntent("/data/data/com.offsec.nhterm/files/usr/bin/kali", cmd);
        activity.startActivity(intent);
    }
}
