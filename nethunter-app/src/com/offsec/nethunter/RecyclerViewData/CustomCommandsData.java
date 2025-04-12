package com.offsec.nethunter.RecyclerViewData;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.offsec.nethunter.AsyncTask.CustomCommandsAsyncTask;
import com.offsec.nethunter.SQL.CustomCommandsSQL;
import com.offsec.nethunter.models.CustomCommandsModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CustomCommandsData {
	private static CustomCommandsData instance;
	public static boolean isDataInitiated = false;
	private final ArrayList<CustomCommandsModel> customCommandsModelArrayList = new ArrayList<>();
	private final MutableLiveData<List<CustomCommandsModel>> data = new MutableLiveData<>();
	public List<CustomCommandsModel> customCommandsModelListFull;
	private final List<CustomCommandsModel> copyOfCustomCommandsModelListFull = new ArrayList<>();

	public static synchronized CustomCommandsData getInstance(){
		if (instance == null) {
			instance = new CustomCommandsData();
		}
		return instance;
	}

	public MutableLiveData<List<CustomCommandsModel>> getCustomCommandsModels(Context context){
		if (!isDataInitiated) {
			data.setValue(CustomCommandsSQL.getInstance(context).bindData(customCommandsModelArrayList));
			customCommandsModelListFull = new ArrayList<>(Objects.requireNonNull(data.getValue()));
			isDataInitiated = true;
		}
		return data;
	}

	public MutableLiveData<List<CustomCommandsModel>> getCustomCommandsModels(){
		return data;
	}

	public void runCommandforitem(int position, Context context) {
		CustomCommandsAsyncTask customCommandsAsyncTask = new CustomCommandsAsyncTask(CustomCommandsAsyncTask.RUNCMD, position, context);
		customCommandsAsyncTask.setListener(new CustomCommandsAsyncTask.CustomCommandsAsyncTaskListener() {
			@Override
			public void onAsyncTaskPrepare() {
				// TODO document why this method is empty
			}

			@Override
			public void onAsyncTaskFinished(List<CustomCommandsModel> customCommandsModelList) {
				updateCustomCommandsModelListFull(customCommandsModelList);
				Objects.requireNonNull(getCustomCommandsModels().getValue()).clear();
				getCustomCommandsModels().getValue().addAll(customCommandsModelList);
				getCustomCommandsModels().postValue(getCustomCommandsModels().getValue());
			}
		});
		customCommandsAsyncTask.execute(getInitCopyOfCustomCommandsModelListFull());
	}

	public void editData(int position, List<String> dataArrayList, CustomCommandsSQL customCommandsSQL){
		CustomCommandsAsyncTask customCommandsAsyncTask = new CustomCommandsAsyncTask(CustomCommandsAsyncTask.EDITDATA, position, (ArrayList<String>) dataArrayList, customCommandsSQL);
		customCommandsAsyncTask.setListener(new CustomCommandsAsyncTask.CustomCommandsAsyncTaskListener() {
			@Override
			public void onAsyncTaskPrepare() {
				// TODO document why this method is empty
			}

			@Override
			public void onAsyncTaskFinished(List<CustomCommandsModel> customCommandsModelList) {
				updateCustomCommandsModelListFull(customCommandsModelList);
				Objects.requireNonNull(getCustomCommandsModels().getValue()).clear();
				getCustomCommandsModels().getValue().addAll(customCommandsModelList);
				getCustomCommandsModels().postValue(getCustomCommandsModels().getValue());
			}
		});
		customCommandsAsyncTask.execute(getInitCopyOfCustomCommandsModelListFull());
	}

	public void addData(int position, List<String> dataArrayList, CustomCommandsSQL customCommandsSQL){
		CustomCommandsAsyncTask customCommandsAsyncTask = new CustomCommandsAsyncTask(CustomCommandsAsyncTask.ADDDATA, position, (ArrayList<String>) dataArrayList, customCommandsSQL);
		customCommandsAsyncTask.setListener(new CustomCommandsAsyncTask.CustomCommandsAsyncTaskListener() {
			@Override
			public void onAsyncTaskPrepare() {
				// TODO document why this method is empty
			}

			@Override
			public void onAsyncTaskFinished(List<CustomCommandsModel> customCommandsModelList) {
				updateCustomCommandsModelListFull(customCommandsModelList);
				Objects.requireNonNull(getCustomCommandsModels().getValue()).clear();
				getCustomCommandsModels().getValue().addAll(customCommandsModelList);
				getCustomCommandsModels().postValue(getCustomCommandsModels().getValue());
			}
		});
		customCommandsAsyncTask.execute(getInitCopyOfCustomCommandsModelListFull());
	}

	public void deleteData(List<Integer> selectedPositionsIndex, List<Integer> selectedTargetIds, CustomCommandsSQL customCommandsSQL){
		CustomCommandsAsyncTask customCommandsAsyncTask = new CustomCommandsAsyncTask(CustomCommandsAsyncTask.DELETEDATA, (ArrayList<Integer>) selectedPositionsIndex, (ArrayList<Integer>) selectedTargetIds, customCommandsSQL);
		customCommandsAsyncTask.setListener(new CustomCommandsAsyncTask.CustomCommandsAsyncTaskListener() {
			@Override
			public void onAsyncTaskPrepare() {
				// TODO document why this method is empty
			}

			@Override
			public void onAsyncTaskFinished(List<CustomCommandsModel> customCommandsModelList) {
				updateCustomCommandsModelListFull(customCommandsModelList);
				Objects.requireNonNull(getCustomCommandsModels().getValue()).clear();
				getCustomCommandsModels().getValue().addAll(customCommandsModelList);
				getCustomCommandsModels().postValue(getCustomCommandsModels().getValue());
			}
		});
		customCommandsAsyncTask.execute(getInitCopyOfCustomCommandsModelListFull());
	}

	public void moveData(int originalPositionIndex, int targetPositionIndex, CustomCommandsSQL customCommandsSQL){
		CustomCommandsAsyncTask customCommandsAsyncTask = new CustomCommandsAsyncTask(CustomCommandsAsyncTask.MOVEDATA, originalPositionIndex, targetPositionIndex, customCommandsSQL);
		customCommandsAsyncTask.setListener(new CustomCommandsAsyncTask.CustomCommandsAsyncTaskListener() {
			@Override
			public void onAsyncTaskPrepare() {
				// TODO document why this method is empty
			}

			@Override
			public void onAsyncTaskFinished(List<CustomCommandsModel> customCommandsModelList) {
				updateCustomCommandsModelListFull(customCommandsModelList);
				Objects.requireNonNull(getCustomCommandsModels().getValue()).clear();
				getCustomCommandsModels().getValue().addAll(customCommandsModelList);
				getCustomCommandsModels().postValue(getCustomCommandsModels().getValue());
			}
		});
		customCommandsAsyncTask.execute(getInitCopyOfCustomCommandsModelListFull());
	}

	public String backupData(CustomCommandsSQL customCommandsSQL, String storedDBpath){
		return customCommandsSQL.backupData(storedDBpath);
	}

	public String restoreData(CustomCommandsSQL customCommandsSQL, String storedDBpath){
		String returnedResult = customCommandsSQL.restoreData(storedDBpath);
		if (returnedResult == null){
			CustomCommandsAsyncTask customCommandsAsyncTask = new CustomCommandsAsyncTask(CustomCommandsAsyncTask.RESTOREDATA, customCommandsSQL);
			customCommandsAsyncTask.setListener(new CustomCommandsAsyncTask.CustomCommandsAsyncTaskListener() {
				@Override
				public void onAsyncTaskPrepare() {
					// TODO document why this method is empty
				}

				@Override
				public void onAsyncTaskFinished(List<CustomCommandsModel> customCommandsModelList) {
					updateCustomCommandsModelListFull(customCommandsModelList);
					Objects.requireNonNull(getCustomCommandsModels().getValue()).clear();
					getCustomCommandsModels().getValue().addAll(customCommandsModelList);
					getCustomCommandsModels().postValue(getCustomCommandsModels().getValue());
				}
			});
			customCommandsAsyncTask.execute(getInitCopyOfCustomCommandsModelListFull());
			return null;
		} else {
			return returnedResult;
		}
	}

	public void resetData(CustomCommandsSQL customCommandsSQL){
		customCommandsSQL.resetData();
		CustomCommandsAsyncTask customCommandsAsyncTask = new CustomCommandsAsyncTask(CustomCommandsAsyncTask.RESTOREDATA, customCommandsSQL);
		customCommandsAsyncTask.setListener(new CustomCommandsAsyncTask.CustomCommandsAsyncTaskListener() {
			@Override
			public void onAsyncTaskPrepare() {
				// TODO document why this method is empty
			}

			@Override
			public void onAsyncTaskFinished(List<CustomCommandsModel> customCommandsModelList) {
				updateCustomCommandsModelListFull(customCommandsModelList);
				Objects.requireNonNull(getCustomCommandsModels().getValue()).clear();
				getCustomCommandsModels().getValue().addAll(customCommandsModelList);
				getCustomCommandsModels().postValue(getCustomCommandsModels().getValue());
			}
		});
		customCommandsAsyncTask.execute(getInitCopyOfCustomCommandsModelListFull());
	}

	public void updateCustomCommandsModelListFull(List<CustomCommandsModel> copyOfCustomCommandsModelList){
		customCommandsModelListFull.clear();
		customCommandsModelListFull.addAll(copyOfCustomCommandsModelList);
	}

	private List<CustomCommandsModel> getInitCopyOfCustomCommandsModelListFull(){
		copyOfCustomCommandsModelListFull.clear();
		copyOfCustomCommandsModelListFull.addAll(customCommandsModelListFull);
		return copyOfCustomCommandsModelListFull;
	}
}
