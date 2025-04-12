package com.offsec.nethunter;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.offsec.nethunter.AsyncTask.ChrootManagerAsynctask;
import com.offsec.nethunter.bridge.Bridge;
import com.offsec.nethunter.service.CompatCheckService;
import com.offsec.nethunter.service.NotificationChannelService;
import com.offsec.nethunter.utils.NhPaths;
import com.offsec.nethunter.utils.SharePrefTag;
import com.offsec.nethunter.utils.ShellExecuter;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


public class ChrootManagerFragment extends Fragment {
    public static final String TAG = "ChrootManager";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String IMAGE_SERVER = "image-nethunter.kali.org";
    private static final String IMAGE_DIRECTORY = "/nethunter-fs/kali-daily/";
    private static String ARCH = "";
    private static String MINORFULL = "";
    private TextView mountStatsTextView;
    private TextView baseChrootPathTextView;
    private TextView resultViewerLoggerTextView;
    private TextView kaliFolderTextView;
    private Button kaliFolderEditButton;
    private Button mountChrootButton;
    private Button unmountChrootButton;
    private Button installChrootButton;
    private Button addMetaPkgButton;
    private Button removeChrootButton;
    private Button backupChrootButton;
    private LinearLayout ChrootDesc;
    private static SharedPreferences sharedPreferences;
    private ChrootManagerAsynctask chrootManagerAsynctask;
    private final Intent backPressedintent = new Intent();
    private static final int IS_MOUNTED = 0;
    private static final int IS_UNMOUNTED = 1;
    private static final int NEED_TO_INSTALL = 2;
    public static boolean isAsyncTaskRunning = false;
    private Context context;
    private Activity activity;

    public static ChrootManagerFragment newInstance(int sectionNumber) {
        ChrootManagerFragment fragment = new ChrootManagerFragment();
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
        View rootView = inflater.inflate(R.layout.chroot_manager, container, false);
        sharedPreferences = activity.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        baseChrootPathTextView = rootView.findViewById(R.id.f_chrootmanager_base_path_tv);
        mountStatsTextView = rootView.findViewById(R.id.f_chrootmanager_mountresult_tv);
        resultViewerLoggerTextView = rootView.findViewById(R.id.f_chrootmanager_viewlogger);
        kaliFolderTextView = rootView.findViewById(R.id.f_chrootmanager_kalifolder_tv);
        kaliFolderEditButton = rootView.findViewById(R.id.f_chrootmanager_edit_btn);
        mountChrootButton = rootView.findViewById(R.id.f_chrootmanager_mount_btn);
        unmountChrootButton = rootView.findViewById(R.id.f_chrootmanager_unmount_btn);
        installChrootButton = rootView.findViewById(R.id.f_chrootmanager_install_btn);
        addMetaPkgButton = rootView.findViewById(R.id.f_chrootmanager_addmetapkg_btn);
        removeChrootButton = rootView.findViewById(R.id.f_chrootmanager_removechroot_btn);
        backupChrootButton = rootView.findViewById(R.id.f_chrootmanager_backupchroot_btn);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        resultViewerLoggerTextView.setMovementMethod(new ScrollingMovementMethod());
        kaliFolderTextView.setClickable(true);
        kaliFolderTextView.setText(sharedPreferences.getString(SharePrefTag.CHROOT_ARCH_SHAREPREF_TAG, NhPaths.ARCH_FOLDER));
        final LinearLayoutCompat kaliViewFolderlinearLayout = view.findViewById(R.id.f_chrootmanager_viewholder);
        kaliViewFolderlinearLayout.setOnClickListener(view1 -> new MaterialAlertDialogBuilder(activity, R.style.DialogStyleCompat)
                .setMessage(baseChrootPathTextView.getText().toString() +
                        kaliFolderTextView.getText().toString())
                .create().show());
        setEditButton();
        setStopKaliButton();
        setStartKaliButton();
        setInstallChrootButton();
        setRemoveChrootButton();
        setAddMetaPkgButton();
        setBackupChrootButton();
        // WearOS optimisation
        SharedPreferences sharedpreferences = activity.getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        Boolean iswatch = sharedpreferences.getBoolean("running_on_wearos", false);
        if (iswatch) {
            kaliViewFolderlinearLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isAsyncTaskRunning){
            compatCheck();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mountStatsTextView = null;
        baseChrootPathTextView = null;
        resultViewerLoggerTextView = null;
        kaliFolderTextView = null;
        kaliFolderEditButton = null;
        mountChrootButton = null;
        unmountChrootButton = null;
        installChrootButton = null;
        addMetaPkgButton = null;
        removeChrootButton = null;
        backupChrootButton = null;
        chrootManagerAsynctask = null;
    }

    private void setEditButton(){
        kaliFolderEditButton.setOnClickListener(view -> {
            MaterialAlertDialogBuilder adb = new MaterialAlertDialogBuilder(activity, R.style.DialogStyleCompat);
            final AlertDialog ad = adb.create();
            LinearLayout ll = new LinearLayout(activity);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.setLayoutParams(layoutParams);
            EditText chrootPathEditText = new EditText(activity);
            TextView availableChrootPathextview = new TextView(activity);
            LinearLayout.LayoutParams editTextParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            editTextParams.setMargins(58,0,58,0);
            chrootPathEditText.setText(sharedPreferences.getString(SharePrefTag.CHROOT_ARCH_SHAREPREF_TAG, ""));
            chrootPathEditText.setSingleLine();
            chrootPathEditText.setLayoutParams(editTextParams);
            availableChrootPathextview.setLayoutParams(editTextParams);
            availableChrootPathextview.setTextColor(ContextCompat.getColor(activity, R.color.clearTitle));
            availableChrootPathextview.setText(String.format(getString(R.string.list_of_available_folders), NhPaths.NH_SYSTEM_PATH));
            File chrootDir = new File(NhPaths.NH_SYSTEM_PATH);
            int count = 0;
            for (File file : Objects.requireNonNull(chrootDir.listFiles())) {
                if (file.isDirectory()) {
                    if (file.getName().equals("kalifs")) continue;
                    count += 1;
                    availableChrootPathextview.append("    " + count + ". " + file.getName() + "\n");
                }
            }
            ll.addView(chrootPathEditText);
            ll.addView(availableChrootPathextview);
            ad.setCancelable(true);
            ad.setTitle("Setup Chroot Path");
            ad.setMessage("The Chroot Path is prefixed to \n\"/data/local/nhsystem/\"\n\n" +
                    "Just put the basename of your Kali Chroot Folder:");
            ad.setView(ll);
            ad.setButton(DialogInterface.BUTTON_POSITIVE, "Apply", (dialogInterface, i) -> {
                if (chrootPathEditText.getText().toString().matches("^\\.(.*$)|^\\.\\.(.*$)|^/+(.*$)|^.*/+(.*$)|^$")){
                    NhPaths.showMessage(activity, "Invalid Name, please try again.");
                } else {
                    NhPaths.ARCH_FOLDER = chrootPathEditText.getText().toString();
                    kaliFolderTextView.setText(NhPaths.ARCH_FOLDER);
                    sharedPreferences.edit().putString(SharePrefTag.CHROOT_ARCH_SHAREPREF_TAG, NhPaths.ARCH_FOLDER).apply();
                    sharedPreferences.edit().putString(SharePrefTag.CHROOT_PATH_SHAREPREF_TAG, NhPaths.CHROOT_PATH()).apply();
                    new ShellExecuter().RunAsRootOutput("ln -sfn " + NhPaths.CHROOT_PATH() + " " + NhPaths.CHROOT_SYMLINK_PATH);
                    compatCheck();
                }
                dialogInterface.dismiss();
            });
            ad.show();
        });
    }

    private void setStartKaliButton() {
        mountChrootButton.setOnClickListener(view -> {
            chrootManagerAsynctask = new ChrootManagerAsynctask(ChrootManagerAsynctask.MOUNT_CHROOT);
            chrootManagerAsynctask.setListener(new ChrootManagerAsynctask.ChrootManagerAsyncTaskListener() {
                @Override
                public void onAsyncTaskPrepare() {
                    setAllButtonEnable(false);
                }

                @Override
                public void onAsyncTaskProgressUpdate(int progress) {

                }

                @Override
                public void onAsyncTaskFinished(int resultCode, ArrayList<String> resultString) {
                    if (resultCode == 0){
                        setButtonVisibility(IS_MOUNTED);
                        setMountStatsTextView(IS_MOUNTED);
                        setAllButtonEnable(true);
                        compatCheck();
                        context.startService(new Intent(context, NotificationChannelService.class).setAction(NotificationChannelService.USENETHUNTER));
                    }
                }
            });
            resultViewerLoggerTextView.setText("");
            chrootManagerAsynctask.execute(resultViewerLoggerTextView);
        });
    }

    private void setStopKaliButton(){
        unmountChrootButton.setOnClickListener(view -> {
            chrootManagerAsynctask = new ChrootManagerAsynctask(ChrootManagerAsynctask.UNMOUNT_CHROOT);
            chrootManagerAsynctask.setListener(new ChrootManagerAsynctask.ChrootManagerAsyncTaskListener() {
                @Override
                public void onAsyncTaskPrepare() {
                    setAllButtonEnable(false);
                }

                @Override
                public void onAsyncTaskProgressUpdate(int progress) {

                }

                @Override
                public void onAsyncTaskFinished(int resultCode, ArrayList<String> resultString) {
                    if (resultCode == 0){
                        setMountStatsTextView(IS_UNMOUNTED);
                        setButtonVisibility(IS_UNMOUNTED);
                        setAllButtonEnable(true);
                        compatCheck();
                    }
                }
            });
            resultViewerLoggerTextView.setText("");
            chrootManagerAsynctask.execute(resultViewerLoggerTextView);
        });
    }

    private void setInstallChrootButton() {
        installChrootButton.setOnClickListener(view -> {
            MaterialAlertDialogBuilder ad = new MaterialAlertDialogBuilder(activity, R.style.DialogStyleCompat);
            LinearLayout ll = new LinearLayout(activity);
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

            Button downloadButton = new Button(activity);
            Button restoreButton = new Button(activity);
            downloadButton.setText("DOWNLOAD LATEST KALI CHROOT");
            restoreButton.setText("INSTALL FROM LOCAL STORAGE");
            downloadButton.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, 1f));
            restoreButton.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, 1f));

            ll.addView(downloadButton);
            ll.addView(restoreButton);
            ad.setView(ll);
            AlertDialog dialog = ad.show();

            downloadButton.setOnClickListener(view1 -> {
                dialog.dismiss();
                AlertDialog ad1;
                ad1 = new MaterialAlertDialogBuilder(activity, R.style.DialogStyleCompat)
                        .setView(getLayoutInflater().inflate(R.layout.chroot_manager_download_diaglog, null))
                        .setMessage("Select the options below:")
                        .setPositiveButton("OK", (dialogInterface, i) -> {
                            AlertDialog finalAd = (AlertDialog) dialogInterface;
                            EditText storepathEditText = finalAd.findViewById(R.id.f_chrootmanager_storepath_et);
                            Spinner archSpinner = finalAd.findViewById(R.id.f_chrootmanager_arch_adb_spr);
                            Spinner minorfullSpinner = finalAd.findViewById(R.id.f_chrootmanager_minorfull_adb_spr);
                            assert storepathEditText != null;
                            File downloadDir = new File(storepathEditText.getText().toString());

                            if (downloadDir.isDirectory() && downloadDir.canWrite()) {
                                sharedPreferences.edit().putString(SharePrefTag.CHROOT_DEFAULT_STORE_DOWNLOAD_SHAREPREF_TAG, downloadDir.getAbsolutePath()).apply();
                                assert archSpinner != null;
                                ARCH = archSpinner.getSelectedItemPosition() == 0 ? "arm64" : "armhf";
                                assert minorfullSpinner != null;
                                MINORFULL = minorfullSpinner.getSelectedItemPosition() == 0 ? "full" : "minimal";
                                String targetDownloadFileName = "kali-nethunter-daily-dev-rootfs-" + MINORFULL + "-" + ARCH + ".tar.xz";

                                if (new File(downloadDir, targetDownloadFileName).exists()) {
                                    new MaterialAlertDialogBuilder(activity, R.style.DialogStyleCompat)
                                            .setMessage(downloadDir + "/" + targetDownloadFileName + " exists. Do you want to overwrite it?")
                                            .setPositiveButton("YES", (dialogInterface1, i1) -> {
                                                context.startService(new Intent(context, NotificationChannelService.class).setAction(NotificationChannelService.DOWNLOADING));
                                                startDownloadChroot(targetDownloadFileName, downloadDir);
                                            })
                                            .setNegativeButton("NO", (dialogInterface12, i12) -> dialogInterface12.dismiss())
                                            .create().show();
                                } else {
                                    context.startService(new Intent(context, NotificationChannelService.class).setAction(NotificationChannelService.DOWNLOADING));
                                    startDownloadChroot(targetDownloadFileName, downloadDir);
                                }
                            } else {
                                NhPaths.showMessage_long(context, downloadDir.getAbsolutePath() + " is not a Directory or cannot be accessed.");
                                dialogInterface.dismiss();
                            }
                        }).create();
                ad1.show();
            });

            restoreButton.setOnClickListener(view12 -> {
                Intent intent = new Intent();
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/x-xz");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select zip file"),1001);
                dialog.cancel();
            });
        });
    }
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && (resultCode == Activity.RESULT_OK)) {
            ShellExecuter exe = new ShellExecuter();
            String FilePath = Objects.requireNonNull(data.getData()).getPath();
            FilePath = exe.RunAsRootOutput("echo " + FilePath + " | sed -e 's/\\/document\\/primary:/\\/storage\\/emulated\\/0\\//g'");
            sharedPreferences.edit().putString(SharePrefTag.CHROOT_DEFAULT_BACKUP_SHAREPREF_TAG, FilePath).apply();
            NhPaths.showMessage(context, FilePath);
            chrootManagerAsynctask = new ChrootManagerAsynctask(ChrootManagerAsynctask.INSTALL_CHROOT);
            chrootManagerAsynctask.setListener(new ChrootManagerAsynctask.ChrootManagerAsyncTaskListener() {
                @Override
                public void onAsyncTaskPrepare() {
                    context.startService(new Intent(context, NotificationChannelService.class).setAction(NotificationChannelService.INSTALLING));
                    broadcastBackPressedIntent(false);
                    setAllButtonEnable(false);
                }

                @Override
                public void onAsyncTaskProgressUpdate(int progress) {}

                @Override
                public void onAsyncTaskFinished(int resultCode, ArrayList<String> resultString) {
                    broadcastBackPressedIntent(true);
                    setAllButtonEnable(true);
                    compatCheck();
                }
            });
            resultViewerLoggerTextView.setText("");
            chrootManagerAsynctask.execute(resultViewerLoggerTextView, FilePath, NhPaths.CHROOT_PATH());

        }
    }
    @NonNull
    public MaterialAlertDialogBuilder getMaterialAlertDialogBuilder(File downloadDir, String targetDownloadFileName) {
        MaterialAlertDialogBuilder adb3 = new MaterialAlertDialogBuilder(activity, R.style.DialogStyleCompat);
        adb3.setMessage(downloadDir.getAbsoluteFile() + "/" + targetDownloadFileName + " exists. Do you want to overwrite it?");
        adb3.setPositiveButton("YES", (dialogInterface1, i1) -> {
            context.startService(new Intent(context, NotificationChannelService.class).setAction(NotificationChannelService.DOWNLOADING));
            startDownloadChroot(targetDownloadFileName, downloadDir);
        });
        adb3.setNegativeButton("NO", (dialogInterface12, i12) -> dialogInterface12.dismiss());
        return adb3;
    }

    private void setRemoveChrootButton(){
        removeChrootButton.setOnClickListener(view -> {
            MaterialAlertDialogBuilder adb = new MaterialAlertDialogBuilder(activity, R.style.DialogStyleCompat)
                    .setTitle("Warning!")
                    .setMessage("Are you sure to remove the below Kali Chroot folder?\n" + NhPaths.CHROOT_PATH())
                    .setPositiveButton("I'm sure.", (dialogInterface, i) -> {
                        MaterialAlertDialogBuilder adb1 = new MaterialAlertDialogBuilder(activity, R.style.DialogStyleCompat)
                            .setTitle("Warning!")
                            .setMessage("This is your last chance!")
                            .setPositiveButton("Just do it.", (dialogInterface1, i1) -> {
                                chrootManagerAsynctask = new ChrootManagerAsynctask(ChrootManagerAsynctask.REMOVE_CHROOT);
                                chrootManagerAsynctask.setListener(new ChrootManagerAsynctask.ChrootManagerAsyncTaskListener() {
                                    @Override
                                    public void onAsyncTaskPrepare() {
                                        broadcastBackPressedIntent(false);
                                        setAllButtonEnable(false);
                                    }

                                    @Override
                                    public void onAsyncTaskProgressUpdate(int progress) {

                                    }

                                    @Override
                                    public void onAsyncTaskFinished(int resultCode, ArrayList<String> resultString) {
                                        broadcastBackPressedIntent(true);
                                        setAllButtonEnable(true);
                                        compatCheck();
                                    }
                                });
                                resultViewerLoggerTextView.setText("");
                                chrootManagerAsynctask.execute(resultViewerLoggerTextView);
                            })
                            .setNegativeButton("Okay, I'm sorry.", (dialogInterface12, i12) -> {

                            });
                        adb1.create().show();
                    })
                    .setNegativeButton("Forget it.", (dialogInterface, i) -> { });
            adb.create().show();
        });
    }

    private void startDownloadChroot(String targetDownloadFileName, File downloadDir) {
        ProgressBar prog = new ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal);
        AlertDialog progressDialog = new MaterialAlertDialogBuilder(activity, R.style.DialogStyleCompat)
                .setTitle("Downloading " + targetDownloadFileName)
                .setMessage("Please do NOT kill the app or clear recent apps..")
                .setCancelable(false)
                .setView(prog)
                .create();

        chrootManagerAsynctask = new ChrootManagerAsynctask(ChrootManagerAsynctask.DOWNLOAD_CHROOT);
        chrootManagerAsynctask.setListener(new ChrootManagerAsynctask.ChrootManagerAsyncTaskListener() {
            @Override
            public void onAsyncTaskPrepare() {
                broadcastBackPressedIntent(false);
                setAllButtonEnable(false);
                progressDialog.show();
            }

            @Override
            public void onAsyncTaskProgressUpdate(int progress) {
                ProgressBar progressBar = prog;
                if (progressBar != null) {
                    progressBar.setProgress(progress);
                }
                if (progress == 100) {
                    progressDialog.dismiss();
                    broadcastBackPressedIntent(true);
                    setAllButtonEnable(true);
                }
            }

            @Override
            public void onAsyncTaskFinished(int resultCode, ArrayList<String> resultString) {
                // Handle task completion
            }
        });
        resultViewerLoggerTextView.setText("");
        chrootManagerAsynctask.execute(resultViewerLoggerTextView, IMAGE_SERVER, IMAGE_DIRECTORY + targetDownloadFileName, downloadDir.getAbsolutePath() + "/" + targetDownloadFileName);
    }

    private void setAddMetaPkgButton() {
        addMetaPkgButton.setOnClickListener(view -> {
            //for now, we'll hardcode packages in the dialog view.  At some point we'll want to grab them automatically.
            MaterialAlertDialogBuilder adb = new MaterialAlertDialogBuilder(activity, R.style.DialogStyleCompat);
            adb.setTitle("Metapackage Install & Upgrade");
            LayoutInflater inflater = activity.getLayoutInflater();
            @SuppressLint("InflateParams") final ScrollView sv = (ScrollView) inflater.inflate(R.layout.metapackagechooser, null);
            adb.setView(sv);
            final Button metapackageButton = sv.findViewById(R.id.metapackagesWeb);
            metapackageButton.setOnClickListener(v -> {
                String metapackagesURL = "https://tools.kali.org/kali-metapackages";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(metapackagesURL));
                startActivity(browserIntent);
            });
            adb.setPositiveButton(R.string.InstallAndUpdateButtonText, (dialog, which) -> {
                StringBuilder sb = new StringBuilder();
                CheckBox cb;
                // now grab all the checkboxes in the dialog and check their status
                // thanks to "user2" for a 2-line sample of how to get the dialog's view:  https://stackoverflow.com/a/13959585/3035127
                final AlertDialog d = (AlertDialog) dialog;
                final LinearLayout ll = d.findViewById(R.id.metapackageLinearLayout);
                int children = Objects.requireNonNull(ll).getChildCount();
                for (int cnt = 0; cnt < children; cnt++) {
                    if (ll.getChildAt(cnt) instanceof CheckBox) {
                        cb = (CheckBox) ll.getChildAt(cnt);
                        if (cb.isChecked()) {
                            sb.append(cb.getText()).append(" ");
                        }
                    }
                }
                try {
                    run_cmd("apt update && apt install " + sb + " -y && echo \"(You can close the terminal now)\n\"");
                } catch (Exception e) {
                    NhPaths.showMessage(context, getString(R.string.toast_install_terminal));
                }
            });
            AlertDialog ad = adb.create();
            ad.setCancelable(true);
            ad.show();
        });
    }

    private void setBackupChrootButton() {
        backupChrootButton.setOnClickListener(view -> {
            AlertDialog ad = new MaterialAlertDialogBuilder(activity, R.style.DialogStyleCompat).create();
            EditText backupFullPathEditText = new EditText(activity);
            LinearLayout ll = new LinearLayout(activity);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.setLayoutParams(layoutParams);
            LinearLayout.LayoutParams editTextParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            editTextParams.setMargins(58,40,58,0);
            backupFullPathEditText.setLayoutParams(editTextParams);
            ll.addView(backupFullPathEditText);
            ad.setView(ll);
            ad.setTitle("Backup Chroot");
            ad.setMessage("* It is strongly suggested to create your backup chroot as tar.gz format just for faster process but bigger file size.\n\nbackup \"" + NhPaths.CHROOT_PATH() + "\" to:" );
            backupFullPathEditText.setText(sharedPreferences.getString(SharePrefTag.CHROOT_DEFAULT_BACKUP_SHAREPREF_TAG, ""));
            ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK", (dialogInterface, i) -> {
                sharedPreferences.edit().putString(SharePrefTag.CHROOT_DEFAULT_BACKUP_SHAREPREF_TAG, backupFullPathEditText.getText().toString()).apply();
                if (new File(backupFullPathEditText.getText().toString()).exists()){
                    ad.dismiss();
                    AlertDialog ad2 = new MaterialAlertDialogBuilder(activity, R.style.DialogStyleCompat).create();
                    ad2.setMessage("File exists already, do you want to overwrite it anyway?");
                    ad2.setButton(DialogInterface.BUTTON_POSITIVE, "YES", (dialogInterface1, i1) -> {
                        chrootManagerAsynctask = new ChrootManagerAsynctask(ChrootManagerAsynctask.BACKUP_CHROOT);
                        chrootManagerAsynctask.setListener(new ChrootManagerAsynctask.ChrootManagerAsyncTaskListener() {
                            @Override
                            public void onAsyncTaskPrepare() {
                                context.startService(new Intent(context, NotificationChannelService.class).setAction(NotificationChannelService.BACKINGUP));
                                broadcastBackPressedIntent(false);
                                setAllButtonEnable(false);
                            }

                            @Override
                            public void onAsyncTaskProgressUpdate(int progress) {

                            }

                            @Override
                            public void onAsyncTaskFinished(int resultCode, ArrayList<String> resultString) {
                                broadcastBackPressedIntent(true);
                                setAllButtonEnable(true);
                            }
                        });
                        resultViewerLoggerTextView.setText("");
                        chrootManagerAsynctask.execute(resultViewerLoggerTextView, NhPaths.CHROOT_PATH(), backupFullPathEditText.getText().toString());
                    });
                    ad2.show();
                } else {
                    chrootManagerAsynctask = new ChrootManagerAsynctask(ChrootManagerAsynctask.BACKUP_CHROOT);
                    chrootManagerAsynctask.setListener(new ChrootManagerAsynctask.ChrootManagerAsyncTaskListener() {
                        @Override
                        public void onAsyncTaskPrepare() {
                            context.startService(new Intent(context, NotificationChannelService.class).setAction(NotificationChannelService.BACKINGUP));
                            broadcastBackPressedIntent(false);
                            setAllButtonEnable(false);
                        }

                        @Override
                        public void onAsyncTaskProgressUpdate(int progress) {

                        }

                        @Override
                        public void onAsyncTaskFinished(int resultCode, ArrayList<String> resultString) {
                            broadcastBackPressedIntent(true);
                            setAllButtonEnable(true);
                        }
                    });
                    chrootManagerAsynctask.execute(resultViewerLoggerTextView, NhPaths.CHROOT_PATH(), backupFullPathEditText.getText().toString());
                }
            });
            ad.show();
        });
    }

    private void showBanner() {
        resultViewerLoggerTextView.setText("");
        chrootManagerAsynctask = new ChrootManagerAsynctask(ChrootManagerAsynctask.ISSUE_BANNER);
        chrootManagerAsynctask.execute(resultViewerLoggerTextView, getResources().getString(R.string.aboutchroot));
    }

    private void compatCheck() {
        chrootManagerAsynctask = new ChrootManagerAsynctask(ChrootManagerAsynctask.CHECK_CHROOT);
        chrootManagerAsynctask.setListener(new ChrootManagerAsynctask.ChrootManagerAsyncTaskListener() {
            @Override
            public void onAsyncTaskPrepare() {
                broadcastBackPressedIntent(false);
            }

            @Override
            public void onAsyncTaskProgressUpdate(int progress) { }

            @Override
            public void onAsyncTaskFinished(int resultCode, ArrayList<String> resultString) {
                broadcastBackPressedIntent(true);
                setButtonVisibility(resultCode);
                setMountStatsTextView(resultCode);
                setAllButtonEnable(true);
                context.startService(new Intent(context, CompatCheckService.class).putExtra("RESULTCODE", resultCode));
            }
        });
        resultViewerLoggerTextView.setText("");
        chrootManagerAsynctask.execute(resultViewerLoggerTextView, sharedPreferences.getString(SharePrefTag.CHROOT_PATH_SHAREPREF_TAG, ""));
    }

    private void setMountStatsTextView(int MODE) {
        if (MODE == IS_MOUNTED) {
            mountStatsTextView.setTextColor(Color.GREEN);
            mountStatsTextView.setText(R.string.running);
        } else if  (MODE == IS_UNMOUNTED) {
            mountStatsTextView.setTextColor(Color.RED);
            mountStatsTextView.setText(R.string.stopped);
        } else if  (MODE == NEED_TO_INSTALL) {
            // Only show about banner if chroot is not installed + clear old logs when showing banner for new peeps
            resultViewerLoggerTextView.setText("");
            showBanner();

            mountStatsTextView.setTextColor(Color.RED);
            mountStatsTextView.setText(R.string.not_yet_installed);
        }
    }

    private void setButtonVisibility(int MODE) {
        SharedPreferences sharedpreferences = activity.getSharedPreferences("com.offsec.nethunter", Context.MODE_PRIVATE);
        Boolean iswatch = sharedpreferences.getBoolean("running_on_wearos", false);

        switch (MODE) {
            case IS_MOUNTED:
                mountChrootButton.setVisibility(View.GONE);
                unmountChrootButton.setVisibility(View.VISIBLE);
                installChrootButton.setVisibility(View.GONE);
                if (iswatch) {
                    addMetaPkgButton.setVisibility(View.GONE);
                } else {
                    addMetaPkgButton.setVisibility(View.VISIBLE);
                }
                removeChrootButton.setVisibility(View.GONE);
                backupChrootButton.setVisibility(View.GONE);
                break;
            case IS_UNMOUNTED:
                mountChrootButton.setVisibility(View.VISIBLE);
                unmountChrootButton.setVisibility(View.GONE);
                installChrootButton.setVisibility(View.GONE);
                addMetaPkgButton.setVisibility(View.GONE);
                removeChrootButton.setVisibility(View.VISIBLE);
                backupChrootButton.setVisibility(View.VISIBLE);
                break;
            case NEED_TO_INSTALL:
                mountChrootButton.setVisibility(View.GONE);
                unmountChrootButton.setVisibility(View.GONE);
                installChrootButton.setVisibility(View.VISIBLE);
                addMetaPkgButton.setVisibility(View.GONE);
                removeChrootButton.setVisibility(View.GONE);
                backupChrootButton.setVisibility(View.GONE);
                break;
        }
    }

    private void setAllButtonEnable(boolean isEnable) {
        mountChrootButton.setEnabled(isEnable);
        unmountChrootButton.setEnabled(isEnable);
        installChrootButton.setEnabled(isEnable);
        addMetaPkgButton.setEnabled(isEnable);
        removeChrootButton.setEnabled(isEnable);
        kaliFolderEditButton.setEnabled(isEnable);
        backupChrootButton.setEnabled(isEnable);
    }

    private void broadcastBackPressedIntent(Boolean isEnabled){
        backPressedintent.setAction(AppNavHomeActivity.NethunterReceiver.BACKPRESSED);
        backPressedintent.putExtra("isEnable", isEnabled);
        context.sendBroadcast(backPressedintent);
        setHasOptionsMenu(isEnabled);
    }

    ////
    // Bridge side functions
    ////

    public void run_cmd(String cmd) {
        Intent intent = Bridge.createExecuteIntent("/data/data/com.offsec.nhterm/files/usr/bin/kali", cmd);
        activity.startActivity(intent);
    }
}