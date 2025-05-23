// Generated by data binding compiler. Do not edit!
package com.offsec.nethunter.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Guideline;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import com.google.android.material.button.MaterialButton;
import com.offsec.nethunter.R;
import java.lang.Deprecated;
import java.lang.Object;

public abstract class BtMainBinding extends ViewDataBinding {
  @NonNull
  public final TextView BTstatus;

  @NonNull
  public final TextView BinderStatus;

  @NonNull
  public final TextView DBUSstatus;

  @NonNull
  public final TextView HCIstatus;

  @NonNull
  public final TextView bluebinder;

  @NonNull
  public final Button bluebinderButton;

  @NonNull
  public final Button btButton;

  @NonNull
  public final TextView btIf;

  @NonNull
  public final TextView btMaindesc;

  @NonNull
  public final TextView btService;

  @NonNull
  public final EditText btTime;

  @NonNull
  public final Button dbusButton;

  @NonNull
  public final View divider1;

  @NonNull
  public final View divider4;

  @NonNull
  public final Guideline guideline1;

  @NonNull
  public final Guideline guideline2;

  @NonNull
  public final Guideline guideline3;

  @NonNull
  public final Guideline guideline4;

  @NonNull
  public final Button hciButton;

  @NonNull
  public final Spinner hciInterface;

  @NonNull
  public final ImageButton refreshStatus;

  @NonNull
  public final MaterialButton startScan;

  @NonNull
  public final ListView targets;

  @NonNull
  public final TextView textView11;

  @NonNull
  public final TextView textView18;

  @NonNull
  public final TextView textView19;

  @NonNull
  public final TextView textView8;

  protected BtMainBinding(Object _bindingComponent, View _root, int _localFieldCount,
      TextView BTstatus, TextView BinderStatus, TextView DBUSstatus, TextView HCIstatus,
      TextView bluebinder, Button bluebinderButton, Button btButton, TextView btIf,
      TextView btMaindesc, TextView btService, EditText btTime, Button dbusButton, View divider1,
      View divider4, Guideline guideline1, Guideline guideline2, Guideline guideline3,
      Guideline guideline4, Button hciButton, Spinner hciInterface, ImageButton refreshStatus,
      MaterialButton startScan, ListView targets, TextView textView11, TextView textView18,
      TextView textView19, TextView textView8) {
    super(_bindingComponent, _root, _localFieldCount);
    this.BTstatus = BTstatus;
    this.BinderStatus = BinderStatus;
    this.DBUSstatus = DBUSstatus;
    this.HCIstatus = HCIstatus;
    this.bluebinder = bluebinder;
    this.bluebinderButton = bluebinderButton;
    this.btButton = btButton;
    this.btIf = btIf;
    this.btMaindesc = btMaindesc;
    this.btService = btService;
    this.btTime = btTime;
    this.dbusButton = dbusButton;
    this.divider1 = divider1;
    this.divider4 = divider4;
    this.guideline1 = guideline1;
    this.guideline2 = guideline2;
    this.guideline3 = guideline3;
    this.guideline4 = guideline4;
    this.hciButton = hciButton;
    this.hciInterface = hciInterface;
    this.refreshStatus = refreshStatus;
    this.startScan = startScan;
    this.targets = targets;
    this.textView11 = textView11;
    this.textView18 = textView18;
    this.textView19 = textView19;
    this.textView8 = textView8;
  }

  @NonNull
  public static BtMainBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup root,
      boolean attachToRoot) {
    return inflate(inflater, root, attachToRoot, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.bt_main, root, attachToRoot, component)
   */
  @NonNull
  @Deprecated
  public static BtMainBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup root,
      boolean attachToRoot, @Nullable Object component) {
    return ViewDataBinding.<BtMainBinding>inflateInternal(inflater, R.layout.bt_main, root, attachToRoot, component);
  }

  @NonNull
  public static BtMainBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.bt_main, null, false, component)
   */
  @NonNull
  @Deprecated
  public static BtMainBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable Object component) {
    return ViewDataBinding.<BtMainBinding>inflateInternal(inflater, R.layout.bt_main, null, false, component);
  }

  public static BtMainBinding bind(@NonNull View view) {
    return bind(view, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.bind(view, component)
   */
  @Deprecated
  public static BtMainBinding bind(@NonNull View view, @Nullable Object component) {
    return (BtMainBinding)bind(component, view, R.layout.bt_main);
  }
}
