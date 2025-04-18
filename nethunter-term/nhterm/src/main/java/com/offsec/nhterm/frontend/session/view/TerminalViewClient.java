package com.offsec.nhterm.frontend.session.view;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.offsec.nhterm.backend.TerminalSession;
import com.offsec.nhterm.backend.TerminalSession;

/**
 * Input and scale listener which may be set on a {@link TerminalView} through
 * {@link TerminalView#setTerminalViewClient(TerminalViewClient)}.
 * <p/>
 */
public interface TerminalViewClient {
  /**
   * Callback function on scale events according to {@link ScaleGestureDetector#getScaleFactor()}.
   */
  float onScale(float scale);

  /**
   * On a single tap on the terminal if terminal mouse reporting not enabled.
   */
  void onSingleTapUp(MotionEvent e);

  boolean shouldBackButtonBeMappedToEscape();

  void copyModeChanged(boolean copyMode);

  boolean onKeyDown(int keyCode, KeyEvent e, TerminalSession session);

  boolean onKeyUp(int keyCode, KeyEvent e);

  boolean readControlKey();

  boolean readAltKey();

  boolean readShiftKey();

  boolean readFnKey();

  boolean onCodePoint(int codePoint, boolean ctrlDown, TerminalSession session);

  boolean onLongPress(MotionEvent event);

}
