package com.offsec.nethunter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.offsec.nethunter.bridge.Bridge;
import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class KernelFragment extends Fragment {
    public static final String TAG = "KernelFragment";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private Activity activity;
    private final ShellExecuter exe = new ShellExecuter();

    public static KernelFragment newInstance(int sectionNumber) {
        KernelFragment fragment = new KernelFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getContext();
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.kernel, container, false);
        SharedPreferences sharedpreferences = activity.getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);

        //Check if kernel is available
        checkKernel(rootView, "");

        //Device codename, android version
        final TextView Device = rootView.findViewById(R.id.device);
        final TextView Codename = rootView.findViewById(R.id.codename);
        final TextView Android = rootView.findViewById(R.id.android_ver);
        Device.setText(Build.MODEL);
        Codename.setText(Build.DEVICE);
        Android.setText(Build.VERSION.RELEASE);

        //Custom codename
        final Spinner repoSpinner = rootView.findViewById(R.id.repo_list);
        final Button codenamesearchButton = rootView.findViewById(R.id.custom_search);
        EditText customCodename = rootView.findViewById(R.id.custom_codename);

        final ArrayList<String> repoKernels = new ArrayList<>();
        repoKernels.add("None");
        final String[] codenamesList = exe.RunAsRootOutput("echo None;curl -s https://nethunter.kali.org/kernels.html | sed -n '/<tr class/{n;p;n;p;}' | sed 's/<[^>]*>//g' | sed 'n;/,/!s/^/- /' | paste - - | awk '!x[$0]++' | tail -n +2").split("\n");;
        repoSpinner.setAdapter(new ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, codenamesList));

        repoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
                String selected_device = parentView.getItemAtPosition(pos).toString();
                if (!selected_device.equals("None")) {
                    String[] selected_codename = selected_device.split("- ");
                    customCodename.setText(selected_codename[1]);
                } else customCodename.setText("");
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // TODO document why this method is empty
            }
        });

                codenamesearchButton.setOnClickListener( v -> {
            String CustomCodename = customCodename.getText().toString();
            checkKernel(rootView, CustomCodename);
        });

        //Browse
        final Button kernelbrowseButton = rootView.findViewById(R.id.kernelfilebrowse);

        kernelbrowseButton.setOnClickListener( v -> {
            Intent intent = new Intent();
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/zip");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select zip file"),1001);
        });

        //Flash
        Button flashButton = rootView.findViewById(R.id.flash_kernel);
        EditText kernelPath = rootView.findViewById(R.id.kernelpath);

        flashButton.setOnClickListener( v -> {
            String kernelfilepath = kernelPath.getText().toString();
            run_cmd_android(NhPaths.APP_SCRIPTS_PATH + "/bin/magic-flash " + kernelfilepath + " | awk 'gsub(/ui_print /,\" \") && !/^ $/'; echo 'Exiting..';exit");
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

    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && (resultCode == Activity.RESULT_OK)) {
            ShellExecuter exe = new ShellExecuter();
            EditText kernelPath = requireActivity().findViewById(R.id.kernelpath);
            String FilePath = Objects.requireNonNull(data.getData()).getPath();
            FilePath = exe.RunAsRootOutput("echo " + FilePath + " | sed -e 's/\\/document\\/primary:/\\/sdcard\\//g' ");
            kernelPath.setText(FilePath);
        }
    }
    private void checkKernel(View KernelFragment, String custom) {

        AsyncTask.execute(() -> requireActivity().runOnUiThread(() -> {
            String codename = "";
            if (custom.equals("")) {
                codename = Build.DEVICE;
            } else {
                codename = custom;
            }
            String version = Build.VERSION.RELEASE;
            String version_text = "";
            switch (version) {
                case "4":
                    version_text = "kitkat";
                    break;
                case "5":
                    version_text = "lollipop";
                    break;
                case "6":
                    version_text = "marshmallow";
                    break;
                case "7":
                    version_text = "nougat";
                    break;
                case "8":
                    version_text = "oreo";
                    break;
                case "9":
                    version_text = "pie";
                    break;
                case "10":
                    version_text = "ten";
                    break;
                case "11":
                    version_text = "eleven";
                    break;
                case "12":
                    version_text = "twelve";
                    break;
                case "13":
                    version_text = "thirteen";
                    break;
                case "14":
                    version_text = "fourteen";
                    break;
                case "15":
                    version_text = "fifteen";
                    break;
                case "16":
                    version_text = "sixteen";
                    break;
                case "17":
                    version_text = "seventeen";
                    break;
            }
            String kernel_zip = exe.RunAsRootOutput("curl -s https://gitlab.com/yesimxev/kali-nethunter-kernels/-/raw/main/kernels.txt | grep " + codename + " | grep " + version_text + " || echo NA");
            Toast.makeText(requireActivity().getApplicationContext(), "Searching for " + codename + " on Android " + version, Toast.LENGTH_SHORT).show();
            if (!kernel_zip.equals("NA")) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity(), R.style.DialogStyleCompat);
                MaterialAlertDialogBuilder builderInner = new MaterialAlertDialogBuilder(requireActivity(), R.style.DialogStyleCompat);
                builder.setTitle("Your device is supported!");
                builder.setMessage("Do you want to check and flash the available kernel(s)?");
                builder.setPositiveButton("Ok", (dialog, which) -> {
                    if (kernel_zip.contains("\n")) {
                        final String[] kernelsArray = kernel_zip.split("\n");
                        builderInner.setTitle("Multiple kernels available. Please select");
                        builderInner.setItems(kernelsArray, (dialog2, which2) -> {
                            run_cmd_android("echo -ne \"\\033]0;Flashing Kernel\\007\" && clear;cd /sdcard && curl https://gitlab.com/yesimxev/kali-nethunter-kernels/-/raw/main/" + kernelsArray[which2] + " > " + kernelsArray[which2] + "; " +
                                    NhPaths.APP_SCRIPTS_PATH + "/bin/magic-flash " + kernelsArray[which2] + " | awk 'gsub(/ui_print /,\" \") && !/^ $/'; echo 'Exiting..';exit");
                            Toast.makeText(requireActivity().getApplicationContext(), "Downloading to /sdcard and flashing...", Toast.LENGTH_SHORT).show();
                        });
                        builderInner.show();
                    } else {
                        run_cmd_android("echo -ne \"\\033]0;Flashing Kernel\\007\" && clear;cd /sdcard && curl https://gitlab.com/yesimxev/kali-nethunter-kernels/-/raw/main/" + kernel_zip + " > " + kernel_zip + "; " +
                                NhPaths.APP_SCRIPTS_PATH + "/bin/magic-flash " + kernel_zip + " | awk 'gsub(/ui_print /,\" \") && !/^ $/'; echo 'Exiting..'; exit");
                        Toast.makeText(requireActivity().getApplicationContext(), "Downloading to /sdcard and flashing...", Toast.LENGTH_SHORT).show();
                    }
                    });
                builder.setNegativeButton("Cancel", (dialog, which) -> {
                });
                builder.show();
            } else Toast.makeText(requireActivity().getApplicationContext(), "Codename not found for your Android version. Please download kernel manually", Toast.LENGTH_LONG).show();
        }));
    }
    ////
    // Bridge side functions
    ////

    public void run_cmd(String cmd) {
        Intent intent = Bridge.createExecuteIntent("/data/data/com.offsec.nhterm/files/usr/bin/kali", cmd);
        activity.startActivity(intent);
    }
    public void run_cmd_android(String cmd) {
        Intent intent = Bridge.createExecuteIntent("/data/data/com.offsec.nhterm/files/usr/bin/android-su", cmd);
        activity.startActivity(intent);
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
}
