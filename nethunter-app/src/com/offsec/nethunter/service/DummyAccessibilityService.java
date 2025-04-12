package com.offsec.nethunter.service;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

public class DummyAccessibilityService extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        // TODO document why this method is empty
    }
    @Override
    public void onInterrupt() {
        // TODO document why this method is empty
    }
}
