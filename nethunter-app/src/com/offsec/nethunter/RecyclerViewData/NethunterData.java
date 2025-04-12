package com.offsec.nethunter.RecyclerViewData;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.offsec.nethunter.AsyncTask.NethunterAsynctask;
import com.offsec.nethunter.SQL.NethunterSQL;
import com.offsec.nethunter.models.NethunterModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
    A singleton repository class for processing the data of nethunter fragment

    Mainly passing a copy of current observed MutableLiveData List<NethunterModel>
    to process the data in background thread and update the MutableLiveData List<NethunterModel>
    by the returned AsyncTask result of the copy List<NethunterModel>.

    Do not pass the observed List<NethunterModel> to AsyncTask as parameter as it should always stay on the main thread.
    Instead you should pass a new copy ArrayList<NethunterModel> of the the observed List<NethunterModel>,
    and update the observed List<NethunterModel> with the returned AsyncTask result of the copy List<NethunterModel>
 */

public class NethunterData {
    private static NethunterData instance;
    public static boolean isDataInitiated = false;
    private final ArrayList<NethunterModel> nethunterModelArrayList = new ArrayList<>();
    private final MutableLiveData<List<NethunterModel>> data = new MutableLiveData<>();
    public List<NethunterModel> nethunterModelListFull;
    private final List<NethunterModel> copyOfNethunterModelListFull = new ArrayList<>();

    public static synchronized NethunterData getInstance(){
        if (instance == null) {
            instance = new NethunterData();
        }
        return instance;
    }

    public MutableLiveData<List<NethunterModel>> getNethunterModels(Context context){
        if (!isDataInitiated) {
            data.setValue(NethunterSQL.getInstance(context).bindData(nethunterModelArrayList));
            nethunterModelListFull = new ArrayList<>(Objects.requireNonNull(data.getValue()));
            isDataInitiated = true;
        }
        return data;
    }

    public MutableLiveData<List<NethunterModel>> getNethunterModels(){
        return data;
    }

    public void refreshData(){
        NethunterAsynctask nethunterAsynctask = new NethunterAsynctask(NethunterAsynctask.GETITEMRESULTS);
        nethunterAsynctask.setListener(new NethunterAsynctask.NethunterAsynctaskListener() {
            @Override
            public void onAsyncTaskPrepare() {
                // TODO document why this method is empty
            }

            @Override
            public void onAsyncTaskFinished(List<NethunterModel> nethunterModelList) {
                Objects.requireNonNull(getNethunterModels().getValue()).clear();
                getNethunterModels().getValue().addAll(nethunterModelList);
                getNethunterModels().postValue(getNethunterModels().getValue());
            }
        });
        nethunterAsynctask.execute(getInitCopyOfNethunterModelListFull());
    }

    public void runCommandforItem(int position){
        NethunterAsynctask nethunterAsynctask = new NethunterAsynctask(NethunterAsynctask.RUNCMDFORITEM, position);
        nethunterAsynctask.setListener(new NethunterAsynctask.NethunterAsynctaskListener() {
            @Override
            public void onAsyncTaskPrepare() {
                // TODO document why this method is empty
            }

            @Override
            public void onAsyncTaskFinished(List<NethunterModel> nethunterModelList) {
                Objects.requireNonNull(getNethunterModels().getValue()).clear();
                getNethunterModels().getValue().addAll(nethunterModelList);
                getNethunterModels().postValue(getNethunterModels().getValue());
            }
        });
        nethunterAsynctask.execute(getInitCopyOfNethunterModelListFull());
    }

    public void editData(int position, List<String> dataArrayList, NethunterSQL nethunterSQL){
        NethunterAsynctask nethunterAsynctask = new NethunterAsynctask(NethunterAsynctask.EDITDATA, position, (ArrayList<String>) dataArrayList, nethunterSQL);
        nethunterAsynctask.setListener(new NethunterAsynctask.NethunterAsynctaskListener() {
            @Override
            public void onAsyncTaskPrepare() {
                // TODO document why this method is empty
            }

            @Override
            public void onAsyncTaskFinished(List<NethunterModel> nethunterModelList) {
                updateNethunterModelListFull(nethunterModelList);
                Objects.requireNonNull(getNethunterModels().getValue()).clear();
                getNethunterModels().getValue().addAll(nethunterModelList);
                getNethunterModels().postValue(getNethunterModels().getValue());
            }
        });
        nethunterAsynctask.execute(getInitCopyOfNethunterModelListFull());
    }

    public void addData(int position, List<String> dataArrayList, NethunterSQL nethunterSQL){
        NethunterAsynctask nethunterAsynctask = new NethunterAsynctask(NethunterAsynctask.ADDDATA, position, (ArrayList<String>) dataArrayList, nethunterSQL);
        nethunterAsynctask.setListener(new NethunterAsynctask.NethunterAsynctaskListener() {
            @Override
            public void onAsyncTaskPrepare() {
                // TODO document why this method is empty
            }

            @Override
            public void onAsyncTaskFinished(List<NethunterModel> nethunterModelList) {
                updateNethunterModelListFull(nethunterModelList);
                Objects.requireNonNull(getNethunterModels().getValue()).clear();
                getNethunterModels().getValue().addAll(nethunterModelList);
                getNethunterModels().postValue(getNethunterModels().getValue());
            }
        });
        nethunterAsynctask.execute(getInitCopyOfNethunterModelListFull());
    }

    public void deleteData(List<Integer> selectedPositionsIndex, List<Integer> selectedTargetIds, NethunterSQL nethunterSQL){
        NethunterAsynctask nethunterAsynctask = new NethunterAsynctask(NethunterAsynctask.DELETEDATA, (ArrayList<Integer>) selectedPositionsIndex, (ArrayList<Integer>) selectedTargetIds, nethunterSQL);
        nethunterAsynctask.setListener(new NethunterAsynctask.NethunterAsynctaskListener() {
            @Override
            public void onAsyncTaskPrepare() {
                // TODO document why this method is empty
            }

            @Override
            public void onAsyncTaskFinished(List<NethunterModel> nethunterModelList) {
                updateNethunterModelListFull(nethunterModelList);
                Objects.requireNonNull(getNethunterModels().getValue()).clear();
                getNethunterModels().getValue().addAll(nethunterModelList);
                getNethunterModels().postValue(getNethunterModels().getValue());
            }
        });
        nethunterAsynctask.execute(getInitCopyOfNethunterModelListFull());
    }

    public void moveData(int originalPositionIndex, int targetPositionIndex, NethunterSQL nethunterSQL){
        NethunterAsynctask nethunterAsynctask = new NethunterAsynctask(NethunterAsynctask.MOVEDATA, originalPositionIndex, targetPositionIndex, nethunterSQL);
        nethunterAsynctask.setListener(new NethunterAsynctask.NethunterAsynctaskListener() {
            @Override
            public void onAsyncTaskPrepare() {
                // TODO document why this method is empty
            }

            @Override
            public void onAsyncTaskFinished(List<NethunterModel> nethunterModelList) {
                updateNethunterModelListFull(nethunterModelList);
                Objects.requireNonNull(getNethunterModels().getValue()).clear();
                getNethunterModels().getValue().addAll(nethunterModelList);
                getNethunterModels().postValue(getNethunterModels().getValue());
            }
        });
        nethunterAsynctask.execute(getInitCopyOfNethunterModelListFull());
    }

    public String backupData(NethunterSQL nethunterSQL, String storedDBpath){
        return nethunterSQL.backupData(storedDBpath);
    }

    public String restoreData(NethunterSQL nethunterSQL, String storedDBpath){
        String returnedResult = nethunterSQL.restoreData(storedDBpath);
        if (returnedResult == null){
            NethunterAsynctask nethunterAsynctask = new NethunterAsynctask(NethunterAsynctask.RESTOREDATA, nethunterSQL);
            nethunterAsynctask.setListener(new NethunterAsynctask.NethunterAsynctaskListener() {
                @Override
                public void onAsyncTaskPrepare() {
                    // TODO document why this method is empty
                }

                @Override
                public void onAsyncTaskFinished(List<NethunterModel> nethunterModelList) {
                    updateNethunterModelListFull(nethunterModelList);
                    Objects.requireNonNull(getNethunterModels().getValue()).clear();
                    getNethunterModels().getValue().addAll(nethunterModelList);
                    getNethunterModels().postValue(getNethunterModels().getValue());
                    refreshData();
                }
            });
            nethunterAsynctask.execute(getInitCopyOfNethunterModelListFull());
            return null;
        } else {
            return returnedResult;
        }
    }

    public void resetData(NethunterSQL nethunterSQL){
        nethunterSQL.resetData();
        NethunterAsynctask nethunterAsynctask = new NethunterAsynctask(NethunterAsynctask.RESTOREDATA, nethunterSQL);
        nethunterAsynctask.setListener(new NethunterAsynctask.NethunterAsynctaskListener() {
            @Override
            public void onAsyncTaskPrepare() {
                // TODO document why this method is empty
            }

            @Override
            public void onAsyncTaskFinished(List<NethunterModel> nethunterModelList) {
                updateNethunterModelListFull(nethunterModelList);
                Objects.requireNonNull(getNethunterModels().getValue()).clear();
                getNethunterModels().getValue().addAll(nethunterModelList);
                getNethunterModels().postValue(getNethunterModels().getValue());
                refreshData();
            }
        });
        nethunterAsynctask.execute(getInitCopyOfNethunterModelListFull());
    }

    public void updateNethunterModelListFull(List<NethunterModel> copyOfNethunterModelList){
        nethunterModelListFull.clear();
        nethunterModelListFull.addAll(copyOfNethunterModelList);
    }

    private List<NethunterModel> getInitCopyOfNethunterModelListFull(){
        copyOfNethunterModelListFull.clear();
        copyOfNethunterModelListFull.addAll(nethunterModelListFull);
        return copyOfNethunterModelListFull;
    }
}
