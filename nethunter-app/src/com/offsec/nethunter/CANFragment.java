package com.offsec.nethunter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.offsec.nethunter.bridge.Bridge;
import com.offsec.nethunter.utils.BootKali;
import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.ShellExecuter;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class CANFragment extends Fragment {
    public static final String TAG = "CANFragment";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private final ShellExecuter exe = new ShellExecuter();
    private CheckBox DebugCheckbox;
    private CheckBox IDCheckbox;
    private CheckBox DataCheckbox;
    private CheckBox SleepCheckbox;
    private String debugCMD = "";
    private String idCMD = "";
    private String dataCMD = "";
    private String sleepCMD = "";
    private String selected_usb;
    private TextView SelectedBaudrateUSB;
    private TextView SelectedCanSpeedUSB;
    private TextView SelectedData;
    private TextView SelectedID;
    private TextView SelectedIface;
    private TextView SelectedMtu;
    private TextView SelectedBitrate;
    private TextView SelectedRHost;
    private TextView SelectedRPort;
    private TextView SelectedLPort;
    private TextView SelectedSleep;
    private SharedPreferences sharedpreferences;
    private Context context;
    private Activity activity;

    public static CANFragment newInstance(int sectionNumber) {
        CANFragment fragment = new CANFragment();
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

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.can, container, false);

        sharedpreferences = activity.getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);

        // Common used variables

        SelectedIface = rootView.findViewById(R.id.can_iface);
        SelectedMtu = rootView.findViewById(R.id.mtu);
        SelectedBitrate = rootView.findViewById(R.id.bitrate_iface);
        final EditText cansend_sequence = rootView.findViewById(R.id.cansend_sequence);
        final EditText ldattach_cmd = rootView.findViewById(R.id.ldattach_cmd);
        final EditText slcand_cmd = rootView.findViewById(R.id.slcand_cmd);
        final EditText slcanattach_cmd = rootView.findViewById(R.id.slcanattach_cmd);
        final EditText bt_target_mac = rootView.findViewById(R.id.bttarget);
        final EditText CustomCmd = rootView.findViewById(R.id.customcmd);

        SelectedRHost = rootView.findViewById(R.id.cannelloni_rhost);
        SelectedRPort = rootView.findViewById(R.id.cannelloni_rport);
        SelectedLPort = rootView.findViewById(R.id.cannelloni_lport);

        SelectedBaudrateUSB = rootView.findViewById(R.id.baudrate_usb);
        SelectedCanSpeedUSB = rootView.findViewById(R.id.canspeed_usb);

        //Checkboxes
        DebugCheckbox = rootView.findViewById(R.id.debug_canusb);
        IDCheckbox = rootView.findViewById(R.id.id_canusb);
        DataCheckbox = rootView.findViewById(R.id.data_canusb);
        SleepCheckbox = rootView.findViewById(R.id.sleep_canusb);

        //Checkboxes values
        SelectedID = rootView.findViewById(R.id.id_value_canusb);
        SelectedData = rootView.findViewById(R.id.data_value_canusb);
        SelectedSleep = rootView.findViewById(R.id.sleep_value_canusb);

        //First run
        Boolean setupdone = sharedpreferences.getBoolean("setup_done", false);
        if (!setupdone.equals(true)) {
            SetupDialog();
        }

        //USB interfaces
        final Spinner deviceList = rootView.findViewById(R.id.device_interface);

        final String[] outputDevice = {""};
        AsyncTask.execute(() -> outputDevice[0] = exe.RunAsChrootOutput("ls -1 /sys/class/net/ | grep can;ls -1 /dev/ttyUSB*;ls -1 /dev/rfcomm*;ls -1 /dev/ttyACM*"));

        final ArrayList<String> deviceIfaces = new ArrayList<>();
        if (outputDevice[0].isEmpty()) {
            deviceIfaces.add("None");
            deviceList.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, deviceIfaces));
        } else {
            final String[] deviceifacesArray = outputDevice[0].split("\n");
            deviceList.setAdapter(new ArrayAdapter<>(requireContext(),android.R.layout.simple_list_item_1, deviceifacesArray));
            String detected_device = exe.RunAsChrootOutput("dmesg | grep \"now attached to\" | tail -1 | awk '{ $1=$2=$3=$4=\"\"; print substr($0, 5) }'");
            if (detected_device != null && !detected_device.isEmpty() && !detected_device.matches("^(can|vcan|slcan)\\d+$")) {
                Toast.makeText(requireActivity().getApplicationContext(), detected_device, Toast.LENGTH_LONG).show();
            }
        }

        deviceList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
                selected_usb = parentView.getItemAtPosition(pos).toString();
                sharedpreferences.edit().putInt("selected_usb", deviceList.getSelectedItemPosition()).apply();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        //Refresh Status
        ImageButton RefreshUSB = rootView.findViewById(R.id.refreshUSB);
        RefreshUSB.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Refreshing Devices...", Toast.LENGTH_SHORT).show();
            refresh(rootView);
        });
        AsyncTask.execute(() -> refresh(rootView));

        //Input File
        final EditText inputfilepath = rootView.findViewById(R.id.inputfilepath);
        final Button inputfilebrowse = rootView.findViewById(R.id.inputfilebrowse);

        inputfilebrowse.setOnClickListener( v -> {
            Intent intent = new Intent();
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("log/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select input file"),1001);
        });

        //Output File
        final EditText outputfilepath = rootView.findViewById(R.id.outputfilepath);
        final Button outputfilebrowse = rootView.findViewById(R.id.outputfilebrowse);

        outputfilebrowse.setOnClickListener( v -> {
            Intent intent = new Intent();
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("log/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select output file"),1001);
        });

        // Attach
        //Start LDAttach
        Button LdAttachButton = rootView.findViewById(R.id.start_ldattach);

        LdAttachButton.setOnClickListener(v ->  {
            String ldattachcmd = ldattach_cmd.getText().toString();

            if (!ldattachcmd.isEmpty()) {
                run_cmd(ldattachcmd);
                Toast.makeText(requireActivity().getApplicationContext(), "Press CTRL+C to stop.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(requireActivity().getApplicationContext(), "Please set your ldattach commmand!", Toast.LENGTH_LONG).show();
            }

            activity.invalidateOptionsMenu();
        });

        //Start slcand
        Button SlcandAttachButton = rootView.findViewById(R.id.start_slcand);

        SlcandAttachButton.setOnClickListener(v ->  {
            String slcandcmd = slcand_cmd.getText().toString();

            if (!slcandcmd.isEmpty()) {
                run_cmd(slcandcmd);
                Toast.makeText(requireActivity().getApplicationContext(), "Press CTRL+C to stop.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(requireActivity().getApplicationContext(), "Please set your slcand command!", Toast.LENGTH_LONG).show();
            }

            activity.invalidateOptionsMenu();
        });

        //Start SLCAN_Attach
        Button SlcanAttachButton = rootView.findViewById(R.id.start_slcanattach);

        SlcanAttachButton.setOnClickListener(v ->  {
            String slcanattachcmd = slcanattach_cmd.getText().toString();

            if (!slcanattachcmd.isEmpty()) {
                run_cmd(slcanattachcmd);
                Toast.makeText(requireActivity().getApplicationContext(), "Press CTRL+C to stop.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(requireActivity().getApplicationContext(), "Please set your slcan_attach command!", Toast.LENGTH_LONG).show();
            }

            activity.invalidateOptionsMenu();
        });


        // Interfaces
        // Declare SharedPreferences at the class level
        SharedPreferences preferences = requireActivity().getSharedPreferences("CANInterfaceState", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Store CAN Interface States
        Map<String, Boolean> buttonStates = new HashMap<>();

        // Load saved button states from SharedPreferences when fragment/activity is created
        buttonStates.put("start_caniface", preferences.getBoolean("start_caniface", false));

        // Can Type Spinner
        // Spinner for CAN interfaces
        // USB interfaces
        final Spinner canTypeList = rootView.findViewById(R.id.cantype_spinner);
        final String[] interfaceTypeOptions = {"can", "vcan", "slcan"};

        canTypeList.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, interfaceTypeOptions));

        canTypeList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
                String cantype_selected = parentView.getItemAtPosition(pos).toString();
                sharedpreferences.edit().putString("cantype_selected", cantype_selected).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        // Start CAN interface (For experimental version)
        Button StartCanButton = rootView.findViewById(R.id.start_caniface);

        // Set initial button text based on saved state
        StartCanButton.setText(Boolean.TRUE.equals(buttonStates.get("start_caniface")) ? "⏹ CAN" : "▶ CAN");

        StartCanButton.setOnClickListener(v -> {
            String selected_caniface = SelectedIface.getText().toString();
            String selected_mtu = SelectedMtu.getText().toString();
            String selected_bitrate = SelectedBitrate.getText().toString();
            String interface_type = sharedpreferences.getString("cantype_selected", "");
            boolean isStarted = Boolean.TRUE.equals(buttonStates.get("start_caniface"));

            if (!selected_caniface.isEmpty() && !selected_mtu.isEmpty()) {
                if (isStarted) {
                    String stopCanIface = exe.RunAsChrootOutput("sudo ip link set " + selected_caniface + " down && echo Success || echo Failed");
                    stopCanIface = stopCanIface.trim();
                    if("vcan".equals(interface_type)){
                        String delVcanIface = exe.RunAsChrootOutput("sudo ip link delete " + selected_caniface + " && echo Success || echo Failed");
                        if (delVcanIface.contains("FATAL:") || delVcanIface.contains("Failed")) {
                            Toast.makeText(requireActivity().getApplicationContext(), "Failed to delete " + selected_caniface + " interface!", Toast.LENGTH_LONG).show();
                        }
                    }
                    if (stopCanIface.contains("FATAL:") || stopCanIface.contains("Failed")) {
                        Toast.makeText(requireActivity().getApplicationContext(), "Failed to stop " + selected_caniface + " interface!", Toast.LENGTH_LONG).show();
                    } else {
                        buttonStates.put("start_caniface", false);
                        StartCanButton.setText("▶ CAN");
                        Toast.makeText(requireActivity().getApplicationContext(), "Interface " + selected_caniface + " stopped!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    if("can".equals(interface_type)){
                        String SetBitrateIface = exe.RunAsChrootOutput("sudo ip link set " + selected_caniface + "bitrate " + selected_bitrate + " && echo Success || echo Failed");
                        if (SetBitrateIface.contains("FATAL:") || SetBitrateIface.contains("Failed")) {
                            Toast.makeText(requireActivity().getApplicationContext(), "Failed to set " + selected_caniface + " bitrate!", Toast.LENGTH_LONG).show();
                        }
                    }
                    if("vcan".equals(interface_type)){
                        String addVcanIface = exe.RunAsChrootOutput("sudo ip link add dev " + selected_caniface + " type " + interface_type + " && echo Success || echo Failed");
                        if (addVcanIface.contains("FATAL:") || addVcanIface.contains("Failed")) {
                            Toast.makeText(requireActivity().getApplicationContext(), "Failed to add " + selected_caniface + " interface!", Toast.LENGTH_LONG).show();
                        }
                    }
                    String startCanIface = exe.RunAsChrootOutput("sudo ip link set " + selected_caniface + " mtu " + selected_mtu + " && sudo ip link set " + selected_caniface + " up && echo Success || echo Failed");
                    startCanIface = startCanIface.trim();

                    if (startCanIface.contains("FATAL:") || startCanIface.contains("Failed")) {
                        Toast.makeText(requireActivity().getApplicationContext(), "Failed to start " + selected_caniface + " interface!", Toast.LENGTH_LONG).show();
                    } else {
                        buttonStates.put("start_caniface", true);
                        StartCanButton.setText("⏹ CAN");
                        Toast.makeText(requireActivity().getApplicationContext(), "Interface " + selected_caniface + " started!", Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                if (selected_caniface.isEmpty()) {
                    Toast.makeText(requireActivity().getApplicationContext(), "Please set a CAN interface!", Toast.LENGTH_LONG).show();
                }

                if (selected_mtu.isEmpty()) {
                    Toast.makeText(requireActivity().getApplicationContext(), "Please set a MTU value!", Toast.LENGTH_LONG).show();
                }
            }

            // Save button state to SharedPreferences
            editor.putBoolean("start_caniface", Boolean.TRUE.equals(buttonStates.get("start_caniface")));
            editor.apply();

            activity.invalidateOptionsMenu();
        });

        //Start rfcomm binder
        Button RfcommBinderButton = rootView.findViewById(R.id.start_rfcommbinder);

        RfcommBinderButton.setOnClickListener(v ->  {
            String selected_caniface = SelectedIface.getText().toString();
            String bt_target = bt_target_mac.getText().toString();

            if (!selected_caniface.isEmpty() && !bt_target.isEmpty()) {
                run_cmd("rfcomm bind " + selected_caniface + " " + bt_target);
            } else {
                Toast.makeText(requireActivity().getApplicationContext(), "Please ensure your CAN Interface and Target field is set!", Toast.LENGTH_LONG).show();
            }
            activity.invalidateOptionsMenu();
        });

        //Start Socketcand
        Button SocketCandButton = rootView.findViewById(R.id.start_socketcand);

        SocketCandButton.setOnClickListener(v ->  {
            String selected_caniface = SelectedIface.getText().toString();

            if (!selected_caniface.isEmpty()) {
                run_cmd("socketcand -v -l wlan0 -i " + selected_caniface);
            } else {
                Toast.makeText(requireActivity().getApplicationContext(), "Please ensure your CAN Interface field is set!", Toast.LENGTH_LONG).show();
            }
            activity.invalidateOptionsMenu();
        });

        //Tools
        //Start CanGen
        Button CanGenButton = rootView.findViewById(R.id.start_cangen);

        CanGenButton.setOnClickListener(v ->  {
            String selected_caniface = SelectedIface.getText().toString();

            if (!selected_caniface.isEmpty()) {
                run_cmd("cangen " + selected_caniface + " -v");
            } else {
                Toast.makeText(requireActivity().getApplicationContext(), "Please ensure your CAN Interface field is set!", Toast.LENGTH_LONG).show();
            }
            activity.invalidateOptionsMenu();
        });

        //Start CanSniffer
        Button CanSnifferButton = rootView.findViewById(R.id.start_cansniffer);

        CanSnifferButton.setOnClickListener(v ->  {
            String selected_caniface = SelectedIface.getText().toString();

            if (!selected_caniface.isEmpty()) {
                run_cmd("cansniffer " + selected_caniface);
            } else {
                Toast.makeText(requireActivity().getApplicationContext(), "Please ensure your CAN Interface field is set!", Toast.LENGTH_LONG).show();
            }

            activity.invalidateOptionsMenu();
        });

        //Start CanDump
        Button CanDumpButton = rootView.findViewById(R.id.start_candump);

        CanDumpButton.setOnClickListener(v ->  {
            String selected_caniface = SelectedIface.getText().toString();
            String outputfile = outputfilepath.getText().toString();

            if (!selected_caniface.isEmpty() && !outputfile.isEmpty()) {
                run_cmd("candump " + selected_caniface + " -f " + outputfile);
            } else {
                Toast.makeText(requireActivity().getApplicationContext(), "Please ensure your CAN Interface and Output File fields is set!", Toast.LENGTH_LONG).show();
            }

            activity.invalidateOptionsMenu();
        });

        //Start CanSend
        Button CanSendButton = rootView.findViewById(R.id.start_cansend);

        CanSendButton.setOnClickListener(v ->  {
            String selected_caniface = SelectedIface.getText().toString();
            String sequence = cansend_sequence.getText().toString();

            if (!selected_caniface.isEmpty() && !sequence.isEmpty()) {
                run_cmd("cansend " + selected_caniface + " " + sequence);
            } else {
                Toast.makeText(requireActivity().getApplicationContext(), "Please ensure your CAN Interface and Sequence fields is set!", Toast.LENGTH_LONG).show();
            }

            activity.invalidateOptionsMenu();
        });

        //Start CanPlayer
        Button CanPlayerButton = rootView.findViewById(R.id.start_canplayer);

        CanPlayerButton.setOnClickListener(v ->  {
            String inputfile = inputfilepath.getText().toString();

            if (!inputfile.isEmpty()) {
                run_cmd("canplayer -I " + inputfile);
            } else {
                Toast.makeText(requireActivity().getApplicationContext(), "Please ensure your Input File field is set!", Toast.LENGTH_LONG).show();
            }

            activity.invalidateOptionsMenu();
        });

        //Start SequenceFinder
        final Button SequenceFinderButton = rootView.findViewById(R.id.start_sequencefinder);

        SequenceFinderButton.setOnClickListener(v ->  {
            new BootKali("cp " + NhPaths.APP_SD_FILES_PATH + "/can_arsenal/sequence_finder.sh /opt/car_hacking/sequence_finder.sh && chmod +x /opt/car_hacking/sequence_finder.sh").run_bg();
            String inputfile = inputfilepath.getText().toString();

            if (!inputfile.isEmpty()) {
                run_cmd("/opt/car_hacking/sequence_finder.sh " + inputfile);
            } else {
                Toast.makeText(requireActivity().getApplicationContext(), "Please ensure your Input File field is set!", Toast.LENGTH_LONG).show();
            }

            activity.invalidateOptionsMenu();
        });

        //Start Freediag
        Button FreediagButton = rootView.findViewById(R.id.start_freediag);

        FreediagButton.setOnClickListener(v ->  {
            run_cmd("sudo -u kali freediag");

            activity.invalidateOptionsMenu();
        });

        //Start diag_test
        Button diagTestButton = rootView.findViewById(R.id.start_diagtest);

        diagTestButton.setOnClickListener(v ->  {
            run_cmd("sudo -u kali diag_test");

            activity.invalidateOptionsMenu();
        });

        // USB-CAN
        DebugCheckbox.setOnClickListener( v -> {
            if (DebugCheckbox.isChecked())
                debugCMD = " -t";
            else
                debugCMD = "";
        });
        IDCheckbox.setOnClickListener( v -> {
            if (IDCheckbox.isChecked()) {
                String selected_id = SelectedID.getText().toString();
                idCMD = " -i " + selected_id;
            } else {
                idCMD = "";
            }
        });
        DataCheckbox.setOnClickListener( v -> {
            if (DataCheckbox.isChecked()) {
                String selected_data = SelectedData.getText().toString();
                dataCMD = " -j " + selected_data;
            } else {
                dataCMD = "";
            }
        });
        SleepCheckbox.setOnClickListener( v -> {
            if (SleepCheckbox.isChecked()) {
                String selected_sleep = SelectedSleep.getText().toString();
                sleepCMD = " -g " + selected_sleep;
            } else {
                sleepCMD = "";
            }
        });

        //Start USB-CAN Dump
        // Pre-Release : Will remove one button to replace with "Run", as both command will end to be the same depending settings.
        Button USBCanDumpButton = rootView.findViewById(R.id.start_canusb_dump);

        USBCanDumpButton.setOnClickListener(v ->  {
            String USBCANSpeed = SelectedCanSpeedUSB.getText().toString();
            String USBBaudrate = SelectedBaudrateUSB.getText().toString();

            if (selected_usb != null && !selected_usb.isEmpty() && !USBCANSpeed.isEmpty() && !USBBaudrate.isEmpty()) {
                run_cmd("canusb -d " + selected_usb + " -s " + USBCANSpeed + " -b " + USBBaudrate + debugCMD);
            } else {
                Toast.makeText(requireActivity().getApplicationContext(), "Please ensure your USB Device and USB CAN Speed, Baudrate fields is set!", Toast.LENGTH_LONG).show();
            }

            activity.invalidateOptionsMenu();
        });

        //Start USB-CAN Send
        Button USBCanSendButton = rootView.findViewById(R.id.start_canusb_send);

        USBCanSendButton.setOnClickListener(v ->  {
            String USBCANSpeed = SelectedCanSpeedUSB.getText().toString();
            String USBBaudrate = SelectedBaudrateUSB.getText().toString();

            if (selected_usb != null && !selected_usb.isEmpty() && !USBCANSpeed.isEmpty() && !USBBaudrate.isEmpty()) {
                run_cmd("canusb -d " + selected_usb + " -s " + USBCANSpeed + " -b " + USBBaudrate + debugCMD + idCMD + dataCMD + sleepCMD);
            } else {
                Toast.makeText(requireActivity().getApplicationContext(), "Please ensure your USB Device and USB CAN Speed, Baudrate, Data fields is set!", Toast.LENGTH_LONG).show();
            }

            activity.invalidateOptionsMenu();
        });

        //Cannelloni
        Button CannelloniButton = rootView.findViewById(R.id.start_cannelloni);

        CannelloniButton.setOnClickListener(v ->  {
            String selected_caniface = SelectedIface.getText().toString();
            String rhost = SelectedRHost.getText().toString();
            String rport = SelectedRPort.getText().toString();
            String lport = SelectedLPort.getText().toString();

            if (!selected_caniface.isEmpty() && !rhost.isEmpty() && !rport.isEmpty() && !lport.isEmpty()) {
                run_cmd("sudo cannelloni -I " + selected_caniface + " -R " + rhost + " -r " + rport + " -l " + lport);
            } else {
                Toast.makeText(requireActivity().getApplicationContext(), "Please ensure your CAN Interface, RHOST, RPORT, LPORT fields is set!", Toast.LENGTH_LONG).show();
            }

            activity.invalidateOptionsMenu();
        });

        //Logging
        //Start Asc2Log
        Button Asc2LogButton = rootView.findViewById(R.id.start_asc2log);

        Asc2LogButton.setOnClickListener(v ->  {
            String inputfile = inputfilepath.getText().toString();
            String outputfile = outputfilepath.getText().toString();

            if (!inputfile.isEmpty() && !outputfile.isEmpty()) {
                run_cmd("asc2log -I " + inputfile + " -O " + outputfile);
            } else {
                Toast.makeText(requireActivity().getApplicationContext(), "Please ensure your Input and Output File fields is set!", Toast.LENGTH_LONG).show();
            }

            activity.invalidateOptionsMenu();
        });

        //Start Log2asc
        Button Log2AscButton = rootView.findViewById(R.id.start_log2asc);

        Log2AscButton.setOnClickListener(v ->  {
            String selected_caniface = SelectedIface.getText().toString();
            String inputfile = inputfilepath.getText().toString();
            String outputfile = outputfilepath.getText().toString();

            if (!selected_caniface.isEmpty() && !inputfile.isEmpty() && !outputfile.isEmpty()) {
                run_cmd("log2asc -I " + inputfile + " -O " + outputfile + " " + selected_caniface);
            } else {
                Toast.makeText(requireActivity().getApplicationContext(), "Please ensure your CAN Interface, Input and Output File fields is set!", Toast.LENGTH_LONG).show();
            }

            activity.invalidateOptionsMenu();
        });

        //Start CustomCommand
        Button CustomCmdButton = rootView.findViewById(R.id.start_customcmd);

        CustomCmdButton.setOnClickListener(v ->  {
            String command = CustomCmd.getText().toString();

            if (!command.isEmpty()) {
                run_cmd(command);
            } else {
                Toast.makeText(requireActivity().getApplicationContext(), "Please ensure your Custom Command field is set!", Toast.LENGTH_LONG).show();
            }

            activity.invalidateOptionsMenu();
        });

        //Author Contact
        //Website
        ImageView AuthorWebsiteButton = rootView.findViewById(R.id.author_website);
        AuthorWebsiteButton.setOnClickListener(v -> {
            String url = "https://v0lk3n.github.io";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        //𝕏
        ImageView AuthorXButton = rootView.findViewById(R.id.author_x);
        AuthorXButton.setOnClickListener(v -> {
            String url = "https://x.com/v0lk3n";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        //BlueSky
        ImageView AuthorBlueskyButton = rootView.findViewById(R.id.author_bluesky);
        AuthorBlueskyButton.setOnClickListener(v -> {
            String url = "https://bsky.app/profile/v0lk3n.bsky.social";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        //Mastodon
        ImageView AuthorMastodonButton = rootView.findViewById(R.id.author_mastodon);
        AuthorMastodonButton.setOnClickListener(v -> {
            String url = "https://infosec.exchange/@v0lk3n";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        //Instagram
        ImageView AuthorInstagramButton = rootView.findViewById(R.id.author_instagram);
        AuthorInstagramButton.setOnClickListener(v -> {
            String url = "https://www.instagram.com/v0lk3n_/";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        //Discord
        ImageView AuthorDiscordButton = rootView.findViewById(R.id.author_discord);
        AuthorDiscordButton.setOnClickListener(v -> {
            String url = "https://discord.com/users/343776454762430484";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        //GitHub
        ImageView AuthorGitHubButton = rootView.findViewById(R.id.author_github);
        AuthorGitHubButton.setOnClickListener(v -> {
            String url = "https://github.com/V0lk3n";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        //GitLab
        ImageView AuthorGitLabButton = rootView.findViewById(R.id.author_gitlab);
        AuthorGitLabButton.setOnClickListener(v -> {
            String url = "https://gitlab.com/V0lk3n";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        sharedpreferences = activity.getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        setHasOptionsMenu(true);
        return rootView;
    }

    //Refresh main
    private void refresh(View CANFragment) {
        final Spinner deviceList = CANFragment.findViewById(R.id.device_interface);
        SharedPreferences sharedpreferences = context.getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);

        requireActivity().runOnUiThread(() -> {
            String outputDevice = exe.RunAsChrootOutput("ls -1 /sys/class/net/ | grep can;ls -1 /dev/ttyUSB*;ls -1 /dev/rfcomm*;ls -1 /dev/ttyACM*");
            final ArrayList<String> deviceIfaces = new ArrayList<>();
            if (outputDevice.isEmpty()) {
                deviceIfaces.add("None");
                deviceList.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, deviceIfaces));
                sharedpreferences.edit().putInt("selected_device", deviceList.getSelectedItemPosition()).apply();
            } else {
                final String[] deviceifacesArray = outputDevice.split("\n");
                deviceList.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, deviceifacesArray));
                int lastiface = sharedpreferences.getInt("selected_device", 0);
                deviceList.setSelection(lastiface);
                String detected_device = exe.RunAsChrootOutput("dmesg | grep \"now attached to\" | tail -1 | awk '{ $1=$2=$3=$4=\"\"; print substr($0, 5) }'");
                if (detected_device != null && !detected_device.isEmpty() && !detected_device.matches("^(can|vcan|slcan)\\d+$")) {
                    Toast.makeText(requireActivity().getApplicationContext(), detected_device, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //Menu
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater menuinflater) {
        menuinflater.inflate(R.menu.can, menu);
    }

    //Menu Items
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.documentation:
                sharedpreferences = context.getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
                RunDocumentation();
                return true;
            case R.id.setup:
                sharedpreferences = context.getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
                RunSetup();
                return true;
            case R.id.update:
                sharedpreferences = context.getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
                RunUpdate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //First Setup
    public void SetupDialog() {
        sharedpreferences = activity.getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity(), R.style.DialogStyleCompat);
        builder.setTitle("Welcome to CAN Arsenal!");
        builder.setMessage("This seems to be the first run. Install the CAN tools?");
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

    //Documentation item
    public void RunDocumentation() {
        String url = "https://www.kali.org/docs/nethunter/nethunter-canarsenal/";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        activity.startActivity(intent);
    }

    //Setup item
    public void RunSetup() {
        sharedpreferences = activity.getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        run_cmd("echo -ne \"\\033]0;CAN Arsenal Setup\\007\" && clear; echo '\\nUpdating and Installing Packages...\\n' && apt update && apt install -y can-utils libsdl2-dev libsdl2-image-dev libconfig-dev libsocketcan-dev can-utils maven autoconf make cmake meson&& " +
                "echo '\\nSetting up environment...' && if [[ -d /root/candump ]]; then echo '\\nFolder /root/candump detected!'; else echo '\\nCreating /root/candump folder...'; sudo mkdir -p /root/candump;fi;" +
                "if [[ -d /opt/car_hacking ]]; then echo 'Folder /opt/car_hacking detected!'; else echo '\\nCreating /opt/car_hacking folder...'; sudo mkdir -p /opt/car_hacking;fi;" +
                "if [[ -f /usr/bin/cangen && -f /usr/bin/cansniffer && -f /usr/bin/candump && -f /usr/bin/cansend && -f /usr/bin/canplayer && -d /opt/car_hacking/can-utils ]]; then echo '\\nCan-utils is installed!'; else echo '\\nInstalling Can-Utils...\\n'; cd /opt/car_hacking; sudo git clone https://github.com/v0lk3n/can-utils.git; cd /opt/car_hacking/can-utils; sudo make; sudo make install;fi;" +
                "if [[ -f /usr/local/bin/cannelloni ]]; then echo 'Cannelloni is installed!'; else echo '\\nInstalling Cannelloni\\n'; cd /opt/car_hacking; sudo git clone https://github.com/v0lk3n/cannelloni.git; cd /opt/car_hacking/cannelloni; sudo cmake -DCMAKE_BUILD_TYPE=Release; sudo make; sudo make install;fi;" +
                "if [[ -f /usr/local/bin/canusb ]]; then echo 'USB-CAN is installed!'; else echo '\\nInstalling USB-CAN\\n'; cd /opt/car_hacking; sudo git clone https://github.com/v0lk3n/usb-can.git; cd /opt/car_hacking/usb-can;sudo gcc -o canusb canusb.c; sudo cp canusb /usr/local/bin/canusb;fi;" +
                "if [[ -f /usr/local/bin/freediag && -f /usr/local/bin/diag_test ]]; then echo 'Freediag is installed!'; else echo '\\nInstalling Freediag\\n'; cd /opt/car_hacking; sudo git clone https://github.com/v0lk3n/freediag.git; cd /opt/car_hacking/freediag;./build_simple.sh; sudo cp build/scantool/freediag /usr/local/bin/freediag && sudo cp build/scantool/diag_test /usr/local/bin/diag_test;fi;" +
                "if [[ -f /usr/local/sbin/socketcand ]]; then echo 'Socketcand is Installed!'; else echo '\\nInstalling Socketcand\\n'; cd /opt/car_hacking; sudo git clone https://github.com/V0lk3n/socketcand.git; cd /opt/car_hacking/socketcand; sudo meson setup -Dlibconfig=true --buildtype=release build; sudo meson compile -C build; sudo meson install -C build;fi; " +
                "echo '\\nSetup done!' && echo '\\nPress any key to continue...' && read -s -n 1 && exit");
        sharedpreferences.edit().putBoolean("setup_done", true).apply();
    }

    //Update item
    public void RunUpdate() {
        sharedpreferences = activity.getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        run_cmd("echo -ne \"\\033]0;CAN Arsenal Update\\007\" && clear; echo '\\nUpdating Packages...\\n' && apt update && apt install -y can-utils libsdl2-dev libsdl2-image-dev can-utils maven autoconf make cmake && " +
                "if [[ -f /usr/bin/cangen && -f /usr/bin/cansniffer && -f /usr/bin/candump && -f /usr/bin/cansend && -f /usr/bin/canplayer && -d /opt/car_hacking/can-utils  ]]; then echo '\\nCan-Utils detected! Updating...\\n'; cd /opt/car_hacking/can-utils; sudo git pull; sudo make; sudo make install; else echo '\\nCan-Utils not detected! Please run Setup first.';fi; " +
                "if [[ -f /usr/local/bin/cannelloni && -d /opt/car_hacking/cannelloni  ]]; then echo '\\nCannelloni detected! Updating...\\n'; cd /opt/car_hacking/cannelloni; sudo git pull; sudo cmake -DCMAKE_BUILD_TYPE=Release; sudo make; sudo make install; else echo '\\nCannelloni not detected! Please run Setup first.';fi; " +
                "if [[ -f /usr/local/bin/canusb && -d /opt/car_hacking/usb-can  ]]; then echo '\\nUSB-CAN detected! Updating...\\n'; cd /opt/car_hacking/usb-can; sudo git pull; sudo gcc -o canusb canusb.c; sudo cp canusb /usr/local/bin/canusb; else echo '\\nUSB-CAN not detected! Please run Setup first.';fi; " +
                "if [[ -f /usr/local/bin/freediag && -f /usr/local/bin/diag_test && -d /opt/car_hacking/freediag  ]]; then echo '\\nFreediag detected! Updating...\\n'; cd /opt/car_hacking/freediag; sudo git pull;./build_simple.sh; sudo cp build/scantool/freediag /usr/local/bin/freediag && sudo cp build/scantool/diag_test /usr/local/bin/diag_test; else echo '\\nFreediag not detected! Please run Setup first.';fi; " +
                "if [[ -f /usr/local/sbin/socketcand && -d /opt/car_hacking/socketcand ]]; then echo '\\nSocketcand detected! Updating...\\n'; cd /opt/car_hacking; cd /opt/car_hacking/socketcand; sudo git pull; sudo meson setup -Dlibconfig=true --buildtype=release build; sudo meson compile -C build; sudo meson install -C build; else echo '\\nSocketcand not detected! Please run Setup first.';fi; " +
                "echo '\\nEverything is updated! Closing in 3secs..'; sleep 3 && exit");
        sharedpreferences.edit().putBoolean("setup_done", true).apply();
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

    ////
    // Bridge side functions
    ////

    public void run_cmd(String cmd) {
        @SuppressLint("SdCardPath") Intent intent = Bridge.createExecuteIntent("/data/data/com.offsec.nhterm/files/usr/bin/kali", cmd);
        activity.startActivity(intent);
    }
}
