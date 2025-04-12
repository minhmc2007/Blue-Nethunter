package com.offsec.nethunter.SQL;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.offsec.nethunter.BuildConfig;
import com.offsec.nethunter.models.CustomCommandsModel;
import com.offsec.nethunter.utils.NhPaths;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;


public class CustomCommandsSQL extends SQLiteOpenHelper {
    private static CustomCommandsSQL instance;
    private static final String DATABASE_NAME = "CustomCommandsFragment";
    private static final String TAG = "CustomCommandsSQL";
    private static final String TABLE_NAME = DATABASE_NAME;
    private static final ArrayList<String> COLUMNS = new ArrayList<>();
    private static final String[][] customcommandsData = {
            {"1", "Update Kali Metapackages",
                    "echo -ne \"\\033]0;Updating Kali\\007\" && clear;apt update && apt -y upgrade",
                    "kali", "interactive", "0"},
            {"2", "Launch Wifite",
                    "echo -ne \"\\033]0;Wifite\\007\" && clear;wifite",
                    "kali", "interactive", "0"},
            {"3", "Launch hcxdumptool",
                    "echo -ne \"\\033]0;hcxdumptool\\007\" && clear;hcxdumptool -i wlan1 -w $HOME/$(date +\"%Y-%m-%d_%H-%M-%S\").pcapng",
                    "kali", "interactive", "0"},
            {"4", "Start wlan1 in monitor mode",
                    "echo -ne \"\\033]0;Wlan1 monitor mode\\007\" && clear;ip link set wlan1 down && iw wlan1 set monitor control && ip link set wlan1 up;sleep 2 && exit",
                    "kali", "interactive", "0"},
            {"5", "Start wlan0 in monitor mode",
                    "echo -ne \"\\033]0;Wlan0 Monitor Mode\\007\" && clear;su -c \"[ -f /sys/module/wlan/parameters/con_mode ] && echo 4 > /sys/module/wlan/parameters/con_mode;ip link set wlan0 down;ip link set wlan0 up || echo 'Your device is not QCACLD3.0 or does not support monitor mode! Exiting..'\";echo 'Done! Exiting..' && sleep 2 && exit",
                    "android", "interactive", "0"},
            {"6", "Stop wlan0 monitor mode",
                    "echo -ne \"\\033]0;Stopping Wlan0 Mon Mode\\007\" && clear;su -c \"[ -f /sys/module/wlan/parameters/con_mode ] && ip link set wlan0 down; echo 0 > /sys/module/wlan/parameters/con_mode;ip link set wlan0 up; svc wifi enable || echo 'Your device is not QCACLD3.0 or does not support monitor mode! Exiting..'\";echo 'Done! Exiting..' && sleep 2 && exit",
                    "android", "interactive", "0"},
    };

    public static synchronized CustomCommandsSQL getInstance(Context context){
        if (instance == null) {
            instance = new CustomCommandsSQL(context.getApplicationContext());
        }
        return instance;
    }

    private CustomCommandsSQL(Context context) {
        super(context, DATABASE_NAME, null, 3);
        COLUMNS.add("id");
        COLUMNS.add("CommandLabel");
        COLUMNS.add("Command");
        COLUMNS.add("RuntimeEnv");
        COLUMNS.add("ExecutionMode");
        COLUMNS.add("RunOnBoot");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COLUMNS.get(0) + " INTEGER, " +
                COLUMNS.get(1) + " TEXT, " + COLUMNS.get(2) +  " TEXT, " +
                COLUMNS.get(3) + " TEXT, " + COLUMNS.get(4) + " TEXT, " +
                COLUMNS.get(5) + " INTEGER)");
        // For devices update from db version 2 to 3 only;
        if (new File(NhPaths.APP_DATABASE_PATH + "/KaliLaunchers").exists()) {
            convertOldDBtoNewDB(db);
        } else {
            ContentValues initialValues = new ContentValues();
            db.beginTransaction();
            for (String[] data : customcommandsData) {
                initialValues.put(COLUMNS.get(0), data[0]);
                initialValues.put(COLUMNS.get(1), data[1]);
                initialValues.put(COLUMNS.get(2), data[2]);
                initialValues.put(COLUMNS.get(3), data[3]);
                initialValues.put(COLUMNS.get(4), data[4]);
                initialValues.put(COLUMNS.get(5), data[5]);
                db.insert(TABLE_NAME, null, initialValues);
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrade if needed
    }

    public List<CustomCommandsModel> bindData(List<CustomCommandsModel> customCommandsModelArrayList) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMNS.get(0) + ";", null);
        while (cursor.moveToNext()) {
            int index1 = cursor.getColumnIndex(COLUMNS.get(1));
            int index2 = cursor.getColumnIndex(COLUMNS.get(2));
            int index3 = cursor.getColumnIndex(COLUMNS.get(3));
            int index4 = cursor.getColumnIndex(COLUMNS.get(4));
            int index5 = cursor.getColumnIndex(COLUMNS.get(5));

            if (index1 >= 0 && index2 >= 0 && index3 >= 0 && index4 >= 0 && index5 >= 0) {
                // Use the indices safely
                String commandLabel = cursor.getString(index1);
                String command = cursor.getString(index2);
                String runtimeEnv = cursor.getString(index3);
                String executionMode = cursor.getString(index4);
                String runOnBoot = cursor.getString(index5);

                customCommandsModelArrayList.add(new CustomCommandsModel(commandLabel, command, runtimeEnv, executionMode, runOnBoot));
            }
        }
        cursor.close();
        db.close();
        return customCommandsModelArrayList;
    }

    public void addData(int targetPositionId, @NonNull List<String> Data) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues initialValues = new ContentValues();
        db.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMNS.get(0) + " = " + COLUMNS.get(0) + " + 1 WHERE " + COLUMNS.get(0) + " >= " + targetPositionId + ";");
        initialValues.put(COLUMNS.get(0), targetPositionId);
        initialValues.put(COLUMNS.get(1), Data.get(0));
        initialValues.put(COLUMNS.get(2), Data.get(1));
        initialValues.put(COLUMNS.get(3), Data.get(2));
        initialValues.put(COLUMNS.get(4), Data.get(3));
        initialValues.put(COLUMNS.get(5), Data.get(4));
        db.beginTransaction();
        db.insert(TABLE_NAME, null, initialValues);
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    public void deleteData(List<Integer> selectedTargetIds){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + COLUMNS.get(0) + " in (" + TextUtils.join(",", selectedTargetIds) + ");");
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMNS.get(0) + ";", null);

        while (cursor.moveToNext()) {
            int index1 = cursor.getColumnIndex(COLUMNS.get(1));
            int index2 = cursor.getColumnIndex(COLUMNS.get(2));
            int index3 = cursor.getColumnIndex(COLUMNS.get(3));
            int index4 = cursor.getColumnIndex(COLUMNS.get(4));
            int index5 = cursor.getColumnIndex(COLUMNS.get(5));

            if (index1 >= 0 && index2 >= 0 && index3 >= 0 && index4 >= 0 && index5 >= 0) {
                // Use the indices safely
                String commandLabel = cursor.getString(index1);
                String command = cursor.getString(index2);
                String runtimeEnv = cursor.getString(index3);
                String executionMode = cursor.getString(index4);
                String runOnBoot = cursor.getString(index5);

                // Process the data as needed
            }
        }
        cursor.close();
        db.close();
    }

    public void moveData(Integer originalPosition, Integer targetPosition){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMNS.get(0) + " = 0 - 1 WHERE " + COLUMNS.get(0) + " = " + (originalPosition + 1) + ";");
        if (originalPosition < targetPosition){
            db.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMNS.get(0) + " = " + COLUMNS.get(0) + " - 1 WHERE " + COLUMNS.get(0) + " > " + originalPosition + " AND " + COLUMNS.get(0) + " <= " + targetPosition + ";");
        } else {
            db.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMNS.get(0) + " = " + COLUMNS.get(0) + " + 1 WHERE " + COLUMNS.get(0) + " < " + originalPosition + " AND " + COLUMNS.get(0) + " >= " + targetPosition + ";");
        }
        db.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMNS.get(0) + " = " + (targetPosition + 1) + " WHERE " + COLUMNS.get(0) + " = -1;");
        db.close();
    }

    public void editData(Integer targetPosition, List<String> editData){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMNS.get(1) + " = '" + editData.get(0).replace("'", "''") + "', " +
                COLUMNS.get(2) + " = '" + editData.get(1).replace("'", "''") + "', " +
                COLUMNS.get(3) + " = '" + editData.get(2).replace("'", "''") + "', " +
                COLUMNS.get(4) + " = '" + editData.get(3).replace("'", "''") + "', " +
                COLUMNS.get(5) + " = '" + editData.get(4).replace("'", "''") + "' WHERE " + COLUMNS.get(0) + " = " + targetPosition + ";");
        db.close();
    }

    public void resetData(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COLUMNS.get(0) + " INTEGER, " +
                COLUMNS.get(1) + " TEXT, " +
                COLUMNS.get(2) + " TEXT, " +
                COLUMNS.get(3) + " TEXT, " +
                COLUMNS.get(4) + " TEXT, " +
                COLUMNS.get(5) + " TEXT);");
        ContentValues initialValues = new ContentValues();
        db.beginTransaction();
        for (String[] data: customcommandsData){
            initialValues.put(COLUMNS.get(0), data[0]);
            initialValues.put(COLUMNS.get(1), data[1]);
            initialValues.put(COLUMNS.get(2), data[2]);
            initialValues.put(COLUMNS.get(3), data[3]);
            initialValues.put(COLUMNS.get(4), data[4]);
            initialValues.put(COLUMNS.get(5), data[5]);
            db.insert(TABLE_NAME, null, initialValues);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    public String backupData(String storedDBpath) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            if (sd.canWrite()) {
                String currentDBPath = data.getAbsolutePath() + "/data/" + BuildConfig.APPLICATION_ID + "/databases/" + DATABASE_NAME;
                File currentDB = new File(currentDBPath);
                File backupDB = new File(storedDBpath);

                if (currentDB.exists()) {
                    try (FileChannel src = new FileInputStream(currentDB).getChannel();
                         FileChannel dst = new FileOutputStream(backupDB).getChannel()) {
                        dst.transferFrom(src, 0, src.size());
                    } catch (IOException e) {
                        Log.e(TAG, e.toString());
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }

    public String restoreData(String storedDBpath) {
        if (!new File(storedDBpath).exists()){
            return null;
        }
        if (SQLiteDatabase.openDatabase(storedDBpath, null, SQLiteDatabase.OPEN_READONLY).getVersion() != 3) {
            return null;
        }
        if (!verifyDB(storedDBpath)) {
            return null;
        }
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            if (sd.canWrite()) {
                String currentDBPath = data.getAbsolutePath() + "/data/" + BuildConfig.APPLICATION_ID + "/databases/" + DATABASE_NAME;
                File currentDB = new File(currentDBPath);
                File backupDB = new File(storedDBpath);

                if (currentDB.exists()) {
                    try (FileChannel src = new FileInputStream(backupDB).getChannel();
                         FileChannel dst = new FileOutputStream(currentDB).getChannel()) {
                        dst.transferFrom(src, 0, src.size());
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, "File not found: " + e.getMessage());
                    } catch (IOException e) {
                        Log.e(TAG, "IO Exception: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }

    private boolean verifyDB(String storedDBpath){
        SQLiteDatabase tempDB = SQLiteDatabase.openDatabase(storedDBpath, null, SQLiteDatabase.OPEN_READWRITE);
        if (ifTableExists(tempDB, TABLE_NAME)) {
            tempDB.close();
            return true;
        }
        tempDB.close();
        return false;
    }

    private boolean isOldDB(String storedDBpath) {
        try (SQLiteDatabase tempDB = SQLiteDatabase.openDatabase(storedDBpath, null, SQLiteDatabase.OPEN_READWRITE)) {
            String oldDBTableName = "LAUNCHERS";
            return ifTableExists(tempDB, oldDBTableName);
        }
    }

    //Convert the old db of customcommands sql to the new one.
    private boolean restoreOldDBtoNewDB(String storedDBpath) {
        try (SQLiteDatabase tempDB = SQLiteDatabase.openDatabase(storedDBpath, null, SQLiteDatabase.OPEN_READWRITE)) {
            convertOldDBtoNewDB(tempDB);
            return true;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    private void convertOldDBtoNewDB(SQLiteDatabase currentDB) {
        currentDB.execSQL("ATTACH DATABASE ? AS oldDB", new String[]{NhPaths.APP_DATABASE_PATH + "/KaliLaunchers"});
        currentDB.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMNS.get(0) + "," +
                COLUMNS.get(1) + "," +
                COLUMNS.get(2) + "," +
                COLUMNS.get(3) + "," +
                COLUMNS.get(4) + "," +
                COLUMNS.get(5) + ")" +
                " SELECT id, CommandLabel, Command, RuntimeEnv, ExecutionMode, RunOnBoot" +
                " FROM oldDB.LAUNCHERS;");
        currentDB.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMNS.get(3) + " = LOWER(" + COLUMNS.get(3) + ");");
        currentDB.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMNS.get(4) + " = LOWER(" + COLUMNS.get(4) + ");");
        SQLiteDatabase.deleteDatabase(new File(NhPaths.APP_DATABASE_PATH + "/KaliLaunchers"));
    }

    private boolean ifTableExists (SQLiteDatabase tempDB, String tableName) {
        boolean tableExists = false;
        try {
            Cursor c = tempDB.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'", null);
            if (c.getCount()==1) {
                tableExists = true;
            }
            c.close();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return tableExists;
    }
}
