package com.offsec.nethunter.RecyclerViewData;

import android.content.Context;

import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.MutableLiveData;

import com.offsec.nethunter.AsyncTask.KaliServicesAsyncTask;
import com.offsec.nethunter.SQL.KaliServicesSQL;
import com.offsec.nethunter.models.KaliServicesModel;
import com.offsec.nethunter.utils.NhPaths;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class KaliServicesData {
	private static KaliServicesData instance;
	public static boolean isDataInitiated = false;
	private final ArrayList<KaliServicesModel> kaliServicesModelArrayList = new ArrayList<>();
	private final MutableLiveData<List<KaliServicesModel>> data = new MutableLiveData<>();
	public List<KaliServicesModel> kaliServicesModelListFull;
	private final List<KaliServicesModel> copyOfKaliServicesModelListFull = new ArrayList<>();

	public static synchronized KaliServicesData getInstance(){
		if (instance == null) {
			instance = new KaliServicesData();
		}
		return instance;
	}

	public MutableLiveData<List<KaliServicesModel>> getKaliServicesModels(Context context){
		if (!isDataInitiated) {
			data.setValue(KaliServicesSQL.getInstance(context).bindData(kaliServicesModelArrayList));
			kaliServicesModelListFull = new ArrayList<>(Objects.requireNonNull(data.getValue()));
			isDataInitiated = true;
		}
		return data;
	}

	public MutableLiveData<List<KaliServicesModel>> getKaliServicesModels(){
		return data;
	}

	public void refreshData(){
		KaliServicesAsyncTask kaliServicesAsyncTask = new KaliServicesAsyncTask(KaliServicesAsyncTask.GETITEMSTATUS);
		kaliServicesAsyncTask.setListener(new KaliServicesAsyncTask.KaliServicesAsyncTaskListener() {
			@Override
			public void onAsyncTaskPrepare() {
				// TODO document why this method is empty
			}

			@Override
			public void onAsyncTaskFinished(List<KaliServicesModel> kaliServicesModelList) {
				Objects.requireNonNull(getKaliServicesModels().getValue()).clear();
				getKaliServicesModels().getValue().addAll(kaliServicesModelList);
				getKaliServicesModels().postValue(getKaliServicesModels().getValue());
			}
		});
		kaliServicesAsyncTask.execute(getInitCopyOfKaliServicesModelListFull());
	}

	public void startServiceforItem(int position, SwitchCompat mSwitch, Context context){
		KaliServicesAsyncTask kaliServicesAsyncTask = new KaliServicesAsyncTask(KaliServicesAsyncTask.START_SERVICE_FOR_ITEM, position);
		kaliServicesAsyncTask.setListener(new KaliServicesAsyncTask.KaliServicesAsyncTaskListener() {
			@Override
			public void onAsyncTaskPrepare() {
				mSwitch.setEnabled(false);
			}

			@Override
			public void onAsyncTaskFinished(List<KaliServicesModel> kaliServicesModelList) {
				mSwitch.setEnabled(true);
				mSwitch.setChecked(kaliServicesModelList.get(position).getStatus().startsWith("[+]"));
				Objects.requireNonNull(getKaliServicesModels().getValue()).clear();
				getKaliServicesModels().getValue().addAll(kaliServicesModelList);
				getKaliServicesModels().postValue(getKaliServicesModels().getValue());
				if (!mSwitch.isChecked()) NhPaths.showMessage(context, "Failed starting " + getKaliServicesModels().getValue().get(position).getServiceName() + " service");
			}
		});
		kaliServicesAsyncTask.execute(getInitCopyOfKaliServicesModelListFull());
	}

	public void stopServiceforItem(int position, SwitchCompat mSwitch, Context context){
		KaliServicesAsyncTask kaliServicesAsyncTask = new KaliServicesAsyncTask(KaliServicesAsyncTask.STOP_SERVICE_FOR_ITEM, position);
		kaliServicesAsyncTask.setListener(new KaliServicesAsyncTask.KaliServicesAsyncTaskListener() {
			@Override
			public void onAsyncTaskPrepare() {
				mSwitch.setEnabled(false);
			}

			@Override
			public void onAsyncTaskFinished(List<KaliServicesModel> kaliServicesModelList) {
				mSwitch.setEnabled(true);
				mSwitch.setChecked(kaliServicesModelList.get(position).getStatus().startsWith("[+]"));
				Objects.requireNonNull(getKaliServicesModels().getValue()).clear();
				getKaliServicesModels().getValue().addAll(kaliServicesModelList);
				getKaliServicesModels().postValue(getKaliServicesModels().getValue());
				if (mSwitch.isChecked()) NhPaths.showMessage(context, "Failed stopping " + getKaliServicesModels().getValue().get(position).getServiceName() + " service");
			}
		});
		kaliServicesAsyncTask.execute(getInitCopyOfKaliServicesModelListFull());
	}

	public void editData(int position, List<String> dataArrayList, KaliServicesSQL kaliServicesSQL){
		KaliServicesAsyncTask kaliServicesAsyncTask = new KaliServicesAsyncTask(KaliServicesAsyncTask.EDITDATA, position, (ArrayList<String>) dataArrayList, kaliServicesSQL);
		kaliServicesAsyncTask.setListener(new KaliServicesAsyncTask.KaliServicesAsyncTaskListener() {
			@Override
			public void onAsyncTaskPrepare() {
				// TODO document why this method is empty
			}

			@Override
			public void onAsyncTaskFinished(List<KaliServicesModel> kaliServicesModelList) {
				updateKaliServicesModelListFull(kaliServicesModelList);
				Objects.requireNonNull(getKaliServicesModels().getValue()).clear();
				getKaliServicesModels().getValue().addAll(kaliServicesModelList);
				getKaliServicesModels().postValue(getKaliServicesModels().getValue());
			}
		});
		kaliServicesAsyncTask.execute(getInitCopyOfKaliServicesModelListFull());
	}

	public void addData(int position, List<String> dataArrayList, KaliServicesSQL kaliServicesSQL){
		KaliServicesAsyncTask kaliServicesAsyncTask = new KaliServicesAsyncTask(KaliServicesAsyncTask.ADDDATA, position, (ArrayList<String>) dataArrayList, kaliServicesSQL);
		kaliServicesAsyncTask.setListener(new KaliServicesAsyncTask.KaliServicesAsyncTaskListener() {
			@Override
			public void onAsyncTaskPrepare() {
				// TODO document why this method is empty
			}

			@Override
			public void onAsyncTaskFinished(List<KaliServicesModel> kaliServicesModelList) {
				updateKaliServicesModelListFull(kaliServicesModelList);
				Objects.requireNonNull(getKaliServicesModels().getValue()).clear();
				getKaliServicesModels().getValue().addAll(kaliServicesModelList);
				getKaliServicesModels().postValue(getKaliServicesModels().getValue());
			}
		});
		kaliServicesAsyncTask.execute(getInitCopyOfKaliServicesModelListFull());
	}

	public void deleteData(List<Integer> selectedPositionsIndex, List<Integer> selectedTargetIds, KaliServicesSQL kaliServicesSQL){
		KaliServicesAsyncTask kaliServicesAsyncTask = new KaliServicesAsyncTask(KaliServicesAsyncTask.DELETEDATA, (ArrayList<Integer>) selectedPositionsIndex, (ArrayList<Integer>) selectedTargetIds, kaliServicesSQL);
		kaliServicesAsyncTask.setListener(new KaliServicesAsyncTask.KaliServicesAsyncTaskListener() {
			@Override
			public void onAsyncTaskPrepare() {
				// TODO document why this method is empty
			}

			@Override
			public void onAsyncTaskFinished(List<KaliServicesModel> kaliServicesModelList) {
				updateKaliServicesModelListFull(kaliServicesModelList);
				Objects.requireNonNull(getKaliServicesModels().getValue()).clear();
				getKaliServicesModels().getValue().addAll(kaliServicesModelList);
				getKaliServicesModels().postValue(getKaliServicesModels().getValue());
			}
		});
		kaliServicesAsyncTask.execute(getInitCopyOfKaliServicesModelListFull());
	}

	public void moveData(int originalPositionIndex, int targetPositionIndex, KaliServicesSQL kaliServicesSQL){
		KaliServicesAsyncTask kaliServicesAsyncTask = new KaliServicesAsyncTask(KaliServicesAsyncTask.MOVEDATA, originalPositionIndex, targetPositionIndex, kaliServicesSQL);
		kaliServicesAsyncTask.setListener(new KaliServicesAsyncTask.KaliServicesAsyncTaskListener() {
			@Override
			public void onAsyncTaskPrepare() {
				// TODO document why this method is empty
			}

			@Override
			public void onAsyncTaskFinished(List<KaliServicesModel> kaliServicesModelList) {
				updateKaliServicesModelListFull(kaliServicesModelList);
				Objects.requireNonNull(getKaliServicesModels().getValue()).clear();
				getKaliServicesModels().getValue().addAll(kaliServicesModelList);
				getKaliServicesModels().postValue(getKaliServicesModels().getValue());
			}
		});
		kaliServicesAsyncTask.execute(getInitCopyOfKaliServicesModelListFull());
	}

	public String backupData(KaliServicesSQL kaliServicesSQL, String storedDBpath){
		return kaliServicesSQL.backupData(storedDBpath);
	}

	public String restoreData(KaliServicesSQL kaliServicesSQL, String storedDBpath){
		String returnedResult = kaliServicesSQL.restoreData(storedDBpath);
		if (returnedResult == null){
			KaliServicesAsyncTask kaliServicesAsyncTask = new KaliServicesAsyncTask(KaliServicesAsyncTask.RESTOREDATA, kaliServicesSQL);
			kaliServicesAsyncTask.setListener(new KaliServicesAsyncTask.KaliServicesAsyncTaskListener() {
				@Override
				public void onAsyncTaskPrepare() {
					// TODO document why this method is empty
				}

				@Override
				public void onAsyncTaskFinished(List<KaliServicesModel> kaliServicesModelList) {
					updateKaliServicesModelListFull(kaliServicesModelList);
					Objects.requireNonNull(getKaliServicesModels().getValue()).clear();
					getKaliServicesModels().getValue().addAll(kaliServicesModelList);
					getKaliServicesModels().postValue(getKaliServicesModels().getValue());
					refreshData();
				}
			});
			kaliServicesAsyncTask.execute(getInitCopyOfKaliServicesModelListFull());
			return null;
		} else {
			return returnedResult;
		}
	}

	public void resetData(KaliServicesSQL kaliServicesSQL){
		kaliServicesSQL.resetData();
		KaliServicesAsyncTask kaliServicesAsyncTask = new KaliServicesAsyncTask(KaliServicesAsyncTask.RESTOREDATA, kaliServicesSQL);
		kaliServicesAsyncTask.setListener(new KaliServicesAsyncTask.KaliServicesAsyncTaskListener() {
			@Override
			public void onAsyncTaskPrepare() {
				// TODO document why this method is empty
			}

			@Override
			public void onAsyncTaskFinished(List<KaliServicesModel> kaliServicesModelList) {
				updateKaliServicesModelListFull(kaliServicesModelList);
				Objects.requireNonNull(getKaliServicesModels().getValue()).clear();
				getKaliServicesModels().getValue().addAll(kaliServicesModelList);
				getKaliServicesModels().postValue(getKaliServicesModels().getValue());
				refreshData();
			}
		});
		kaliServicesAsyncTask.execute(getInitCopyOfKaliServicesModelListFull());
	}

	public void updateRunOnChrootStartServices(int position, List<String> dataArrayList, KaliServicesSQL kaliServicesSQL) {
		KaliServicesAsyncTask kaliServicesAsyncTask = new KaliServicesAsyncTask(KaliServicesAsyncTask.UPDATE_RUNONCHROOTSTART_SCRIPTS, position, (ArrayList<String>) dataArrayList, kaliServicesSQL);
		kaliServicesAsyncTask.setListener(new KaliServicesAsyncTask.KaliServicesAsyncTaskListener() {
			@Override
			public void onAsyncTaskPrepare() {
				// TODO document why this method is empty
			}

			@Override
			public void onAsyncTaskFinished(List<KaliServicesModel> kaliServicesModelList) {
				updateKaliServicesModelListFull(kaliServicesModelList);
				Objects.requireNonNull(getKaliServicesModels().getValue()).clear();
				getKaliServicesModels().getValue().addAll(kaliServicesModelList);
				getKaliServicesModels().postValue(getKaliServicesModels().getValue());
			}
		});
		kaliServicesAsyncTask.execute(getInitCopyOfKaliServicesModelListFull());
	}

	public void updateKaliServicesModelListFull(List<KaliServicesModel> copyOfKaliServicesModelList){
		kaliServicesModelListFull.clear();
		kaliServicesModelListFull.addAll(copyOfKaliServicesModelList);
	}

	private List<KaliServicesModel> getInitCopyOfKaliServicesModelListFull(){
		copyOfKaliServicesModelListFull.clear();
		copyOfKaliServicesModelListFull.addAll(kaliServicesModelListFull);
		return copyOfKaliServicesModelListFull;
	}
}
