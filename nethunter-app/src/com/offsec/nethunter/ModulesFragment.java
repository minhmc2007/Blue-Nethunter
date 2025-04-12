package com.offsec.nethunter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.offsec.nethunter.bridge.Bridge;
import com.offsec.nethunter.utils.ShellExecuter;

import java.util.ArrayList;


public class ModulesFragment extends Fragment {
    public static final String TAG = "ModulesFragment";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private Activity activity;
    private final ShellExecuter exe = new ShellExecuter();
    public EditText modules_path;

    public static ModulesFragment newInstance(int sectionNumber) {
        ModulesFragment fragment = new ModulesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getContext();
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.modules, container, false);
        SharedPreferences sharedpreferences = activity.getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);

        //Use last path
        modules_path = rootView.findViewById(R.id.modulesPath);
        String LastModulesPath = sharedpreferences.getString("last_modulespath", "");
        if (!LastModulesPath.isEmpty()) modules_path.setText(LastModulesPath);

        //Refresh Modules
        Button refreshButton = rootView.findViewById(R.id.refresh);
        refreshButton.setOnClickListener(view -> refreshModules(rootView));
        refreshModules(rootView);
        refreshModulesLoaded(rootView);
        //String ModulesPath = modules_path.getText().toString();

        //Modules toggle
        ListView modules = rootView.findViewById(R.id.modulesList);
        String ModulesPath = modules_path.getText().toString();
        modules.setOnItemClickListener((adapterView, view, i, l) -> {
            String ModulesPathFull = ModulesPath + "/" + System.getProperty("os.version");
            String selected_module = modules.getItemAtPosition(i).toString();
            String is_it_loaded = exe.RunAsRootOutput("lsmod | cut -d' ' -f1 | grep " + selected_module);

            if (is_it_loaded.equals(selected_module)){
                String disable_module = exe.RunAsRootOutput("rmmod " + selected_module + " && echo Success || echo Failed");
                if (disable_module.contains("Success")) {
                    Toast.makeText(requireActivity().getApplicationContext(), "Module Disabled", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(requireActivity().getApplicationContext(), "Failed - rmmod "+ selected_module, Toast.LENGTH_LONG).show();
                }
            } else {
                String toggle_module = exe.RunAsRootOutput("insmod " + ModulesPathFull + "/" + selected_module + ".ko && echo Success || echo Failed");
                if (toggle_module.contains("Success")) {
                    refreshModulesLoaded(rootView);
                    Toast.makeText(requireActivity().getApplicationContext(), "Module enabled with insmod", Toast.LENGTH_LONG).show();
                } else {
                    toggle_module = exe.RunAsRootOutput("modprobe -d " + ModulesPathFull + " " + selected_module + " && echo Success || echo Failed");
                    if (toggle_module.contains("Success")) {
                        Toast.makeText(requireActivity().getApplicationContext(), "Module enabled with modprobe", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(requireActivity().getApplicationContext(), "Failed - modprobe -d " + ModulesPathFull + " " + selected_module, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void refreshModules(View ModulesFragment) {
        SharedPreferences sharedpreferences = activity.getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        final ListView modules = ModulesFragment.findViewById(R.id.modulesList);

        modules_path = ModulesFragment.findViewById(R.id.modulesPath);
        String ModulesPath = modules_path.getText().toString();
        String ModulesPathFull = ModulesPath + "/" + System.getProperty("os.version");
        sharedpreferences.edit().putString("last_modulespath", ModulesPath).apply();

        AsyncTask.execute(() -> requireActivity().runOnUiThread(() -> {
            if (ModulesPath.isEmpty()) {
                Toast.makeText(requireActivity().getApplicationContext(), "Please enter path", Toast.LENGTH_SHORT).show();
            }
            else {
                String modulesRaw = exe.RunAsRootOutput("find " + ModulesPathFull + " -name *.ko -printf \"%f\\n\" | sed 's/\\.ko$//1'");
                final String[] modulesArray = modulesRaw.split("\n");
                ArrayAdapter<String> modulesAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, modulesArray);

                if (!modulesRaw.isEmpty()) {
                    modules.setAdapter(modulesAdapter);
                } else {
                    final ArrayList<String> nomodules = new ArrayList<>();
                    nomodules.add("No modules found");
                    modules.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, nomodules));
                }
            }
        }));
    }
    private void refreshModulesLoaded(View ModulesFragment) {
        final TextView modules_Loaded = ModulesFragment.findViewById(R.id.modules_loadedText);

        AsyncTask.execute(() -> requireActivity().runOnUiThread(() -> {
            String modules_loadedRaw = exe.RunAsRootOutput("lsmod | tail -n+2 | cut -d' ' -f1");
            if (!modules_loadedRaw.isEmpty()) {
                modules_Loaded.setText(modules_loadedRaw);
            } else {
                modules_Loaded.setText("No modules loaded");
            }
        }));
    }

    ////
    // Bridge side functions
    ////

    public void run_cmd(String cmd) {
        Intent intent = Bridge.createExecuteIntent("/data/data/com.offsec.nhterm/files/usr/bin/kali", cmd);
        activity.startActivity(intent);
    }

    public static class PreferencesData {
        private static final String PREF_NAME = "com.offsec.nethunter_preferences";

        public static void saveString(Context context, String key, String value) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            sharedPrefs.edit().putString(key, value).apply();
        }

        public static String getString(Context context, String key, String defaultValue) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            return sharedPrefs.getString(key, defaultValue);
        }
    }
}
