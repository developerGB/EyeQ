package com.goodbits.eyeq.bleservice;

import android.content.IntentFilter;

public final class UartIntents {
    public final static String NO_PAIRED_DEVICE_FOUND = "No Paierd EyeQ Devices Found";
    public final static String DEVICE_CONNECTING ="Attempting to connect with EyeQ";
    public final static String DEVICE_CONNECTED ="Connected with EyeQ";
    public final static String DEVICE_DISCONNECTING ="Attempting to disconnect from EyeQ";
    public final static String DEVICE_DISCONNECTED ="Disconnected from EyeQ";
    public final static String EXTRA_DATA = "Extra Data";
    public static final String ACTION_DATA_AVAILABLE = "UART Data is Available";

    static IntentFilter makeUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartIntents.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartIntents.NO_PAIRED_DEVICE_FOUND);
        intentFilter.addAction(UartIntents.DEVICE_CONNECTING);
        intentFilter.addAction(UartIntents.DEVICE_CONNECTED);
        intentFilter.addAction(UartIntents.DEVICE_DISCONNECTING);
        intentFilter.addAction(UartIntents.DEVICE_DISCONNECTED);
        intentFilter.addAction(UartIntents.EXTRA_DATA);
        return intentFilter;
    }
}
