package com.offsec.nethunter;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Arrays;
import java.util.List;

public class AudioFragment extends Fragment {

    public final static String TAG = "AudioFragment";
    public static final int DEFAULT_INDEX_BUFFER_HEADROOM = 4;
    public static final int DEFAULT_INDEX_TARGET_LATENCY = 6;

    private static final List<Long> VALUES_BUFFER_HEADROOM = Arrays.asList(0L, 15625L, 31250L, 62500L, 125000L, 250000L, 500000L, 1000000L, 2000000L);
    private static final List<Long> VALUES_TARGET_LATENCY = Arrays.asList(0L, 15625L, 31250L, 62500L, 125000L, 250000L, 500000L, 1000000L, 2000000L, 5000000L, 10000000L, -1L);

    private Button playButton;
    private Spinner bufferHeadroomSpinner;
    private Spinner targetLatencySpinner;
    private EditText serverInput;
    private EditText portInput;
    private CheckBox autoStartCheckBox;
    private TextView errorText;
    private ScrollView fullScrollView;

    private Throwable error;

    private boolean isServiceBound = false;
    private AudioPlaybackService boundService;
    private int itemId; // Store the itemId passed via newInstance

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            boundService = ((AudioPlaybackService.LocalBinder) service).getService();
            if (boundService != null) {
                // Now that the service is bound, update the UI and enable the play button
                boundService.playState().observe(getViewLifecycleOwner(), playState -> updatePlayState(playState));
                boundService.showNotification();
                updatePrefs(boundService);

                if (boundService.getAutostartPref() && boundService.isStartable()) {
                    play(); // Optionally start playback if autostart is enabled
                }
            }
            isServiceBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            boundService = null;
            isServiceBound = false; // Clear the reference when the service is disconnected
        }
    };

    public Throwable getError() {
        return error;
    }

    // Add the newInstance method
    public static AudioFragment newInstance(int itemId) {
        AudioFragment fragment = new AudioFragment();
        Bundle args = new Bundle();
        args.putInt("ITEM_ID", itemId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateAudioFragment");

        // Retrieve the itemId passed in newInstance
        if (getArguments() != null) {
            itemId = getArguments().getInt("ITEM_ID", -1);
        }

        // Log or use the itemId as needed
        Log.d(TAG, "Received itemId: " + itemId);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.audio, container, false);

        // Initialize UI elements
        fullScrollView = view.findViewById(R.id.fullScrollView);
        serverInput = view.findViewById(R.id.EditTextServer);
        portInput = view.findViewById(R.id.EditTextPort);
        autoStartCheckBox = view.findViewById(R.id.auto_start);
        playButton = view.findViewById(R.id.ButtonPlay);
        errorText = view.findViewById(R.id.errorText);
        bufferHeadroomSpinner = view.findViewById(R.id.bufferHeadroomSpinner);
        targetLatencySpinner = view.findViewById(R.id.targetLatencySpinner);
        TextView moduleInfoLabel = view.findViewById(R.id.moduleInfoLabel);
        TextView builderinfoLabel = view.findViewById(R.id.builderinfoLabel);
        TextView moduleVerLabel = view.findViewById(R.id.buildVersionLabel);

        String builderinfo = getString(R.string.builderinfo);
        builderinfoLabel.setText("Maintainer: " + builderinfo);

        String moduleInfo = getString(R.string.moduleInfo);
        moduleInfoLabel.setText("Info: " + moduleInfo);

        String BuildVerInfo = getString(R.string.build_version);
        moduleVerLabel.setText("Version: " + BuildVerInfo);

        playButton.setOnClickListener(v -> {
            if (boundService != null) {
                if (boundService.getPlayState().isActive()) {
                    stop();
                } else {
                    play();
                }
            } else {
                // Optionally, show a message or handle the case where the service is not connected yet
                errorText.setText("Service not connected yet. Please wait.");
            }
        });

        // Set up spinners with default values
        setupDefaultAudioConfig();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Bind to AudioPlaybackService
        Intent intent = new Intent(getActivity(), AudioPlaybackService.class);
        requireActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        if (isServiceBound) {
            requireActivity().unbindService(mConnection);
            isServiceBound = false;
        }
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear view references
        autoStartCheckBox = null;
        fullScrollView = null;
        playButton = null;
        portInput = null;
        bufferHeadroomSpinner = null;
        serverInput = null;
        targetLatencySpinner = null;
        errorText = null;

        if (isServiceBound) {
            requireActivity().unbindService(mConnection);
            isServiceBound = false;
        }
        boundService = null;
    }

    private void setupDefaultAudioConfig() {

        serverInput.setText("127.0.0.1");
        portInput.setText("8000");

        // Format values for buffer headroom and target latency as seconds
        List<String> formattedBufferHeadroom = formatValuesAsSeconds(VALUES_BUFFER_HEADROOM);
        List<String> formattedTargetLatency = formatValuesAsSeconds(VALUES_TARGET_LATENCY);

        // Set up adapters with the formatted string values
        ArrayAdapter<String> bufferAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_dropdown_item, formattedBufferHeadroom);
        bufferHeadroomSpinner.setAdapter(bufferAdapter);

        ArrayAdapter<String> latencyAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_dropdown_item, formattedTargetLatency);
        targetLatencySpinner.setAdapter(latencyAdapter);
    }

    // Helper method to format values as seconds
    private List<String> formatValuesAsSeconds(List<Long> values) {
        List<String> formattedValues = new ArrayList<>();
        for (Long value : values) {
            if (value >= 0) {
                formattedValues.add(String.format(Locale.getDefault(), "%.3fs", value / 1000000.0));
            } else {
                formattedValues.add("Default"); // or another label for special values like -1
            }
        }
        return formattedValues;
    }


    private void updatePrefs(AudioPlaybackService service) {
        String serverPref = service.getServerPref();
        if (serverPref != null && !serverPref.isEmpty()) {
            serverInput.setText(serverPref);
        }

        int portPref = service.getPortPref();
        if (portPref > 0) {
            portInput.setText(String.valueOf(portPref));
        }
        autoStartCheckBox.setChecked(service.getAutostartPref());

        setUpSpinner(bufferHeadroomSpinner, VALUES_BUFFER_HEADROOM, service.getBufferHeadroom(), DEFAULT_INDEX_BUFFER_HEADROOM);
        setUpSpinner(targetLatencySpinner, VALUES_TARGET_LATENCY, service.getTargetLatency(), DEFAULT_INDEX_TARGET_LATENCY);
    }

    private void setUpSpinner(Spinner spinner, List<Long> values, long value, int defaultIndex) {
        int pos = values.indexOf(value);
        spinner.setSelection(pos >= 0 ? pos : defaultIndex);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (boundService != null) {
                    long headroomUsec = VALUES_BUFFER_HEADROOM.get(bufferHeadroomSpinner.getSelectedItemPosition());
                    long latencyUsec = VALUES_TARGET_LATENCY.get(targetLatencySpinner.getSelectedItemPosition());
                    boundService.setBufferUsec(headroomUsec, latencyUsec);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updatePlayState(@NonNull AudioPlayState playState) {
        playButton.setText(getPlayButtonText(playState));
        playButton.setEnabled(true);

        switch (playState) {
            case STOPPED:
                appendErrorText("Disconnected State", android.R.color.holo_orange_light);
                appendDashes();
                playButton.setEnabled(true);
                break;
            case STARTING:
                appendErrorText("Connection Starting", android.R.color.holo_green_dark);
                playButton.setEnabled(true);
                break;
            case BUFFERING:
                appendErrorText("Establishing Connection", android.R.color.holo_orange_light);
                playButton.setEnabled(true);
                break;
            case STARTED:
                appendErrorText("Everything is working fine! Enjoy!", android.R.color.holo_green_dark);
                appendDashes();
                playButton.setEnabled(true);
                break;
            case STOPPING:
                appendErrorText("Connection Disconnecting", android.R.color.holo_red_light);
                playButton.setEnabled(false);
                break;
        }

        if (boundService != null && boundService.getError() != null) {
            appendErrorText("An error occurred: " + boundService.getError().getMessage(), android.R.color.holo_red_dark);
            appendDashes();
        }
    }

    private String getPlayButtonText(@NonNull AudioPlayState playState) {
        switch (playState) {
            case STOPPED:
                return getString(R.string.btn_play);
            case STARTING:
                return getString(R.string.btn_starting);
            case BUFFERING:
                return getString(R.string.btn_buffering);
            case STARTED:
                return getString(R.string.btn_stop);
            case STOPPING:
                return getString(R.string.btn_stopping);
            default:
                return getString(R.string.btn_waiting);
        }
    }

    private void appendErrorText(String message, int colorId) {
        SpannableString spannable = new SpannableString(message + "\n");
        spannable.setSpan(new ForegroundColorSpan(getResources().getColor(colorId)), 0, spannable.length(), 0);
        errorText.append(spannable);
    }

    private void appendDashes() {
        SpannableString dashes = new SpannableString("------------------\n");
        dashes.setSpan(new ForegroundColorSpan(getResources().getColor(android.R.color.holo_purple)), 0, dashes.length(), 0);
        errorText.append(dashes);
    }

    public void play() {
        String server = serverInput.getText().toString().trim();
        int port;
        try {
            port = Integer.parseInt(portInput.getText().toString());
        } catch (NumberFormatException e) {
            portInput.setError("Invalid port number");
            return;
        }
        // Clear any previous error messages
        portInput.setError(null);

        if (server.isEmpty()) {
            serverInput.setError("Server cannot be empty");
            return;
        }

        if (boundService != null) {
            // Log the server and port being used
            Log.d(TAG, "Attempting to play on server: " + server + " port: " + port);

            // Set preferences and start playback
            boundService.setPrefs(server, port, autoStartCheckBox.isChecked());
            boundService.play(server, port);
        } else {
            // Handle case where service is not bound
            errorText.setText("Service not bound. Please try again.");
            Log.e(TAG, "Service not bound when attempting to play.");
        }
    }

    public void stop() {
        if (boundService != null) {
            boundService.stop();
        }
    }
}
