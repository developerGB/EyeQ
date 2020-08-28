package com.goodbits.eyeq.bleservice;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;

public class UART {
    private static final float deviceWidth = 1920.0f; // Width of binocular touch pad
    private static final float deviceHeight = 1080.0f; // height of binacular touch pad
    private static UartService mService;

    private int actionType = MotionEvent.ACTION_UP; // Touch pad event is Mouse Up initially
    private int prevActionType = MotionEvent.ACTION_UP; // Touch pad event is Mouse Up initially
    private static float xRatio;                     // screen size to touch pad ratio
    private static float yRatio;
    private static float x = 0;
    private static float y = 0;

    private final static String TAG = "[EyeQ] UART";
    private static UART uart = null;
    private static View view;
    private static UartCallback appCallback;

    private UART(Activity activity, View view, UartCallback appCallback) {
        Context context = activity.getApplicationContext();
        this.view = view;
        this.appCallback = appCallback;

        // get screen resolution of Android Phone
        Display display = activity.getWindowManager().getDefaultDisplay();
        Log.d (TAG,  " screen size = " + display.getHeight() + ":" + display.getWidth());

        this.setViewPort (display.getWidth(), display.getHeight());

        serviceInit(context);
    }

    /**
     * Singleton call for creating or getting an instance of this class UART
     *
     * @param activity that is currently active
     * @param view that is currently active
     * @param appCallback for calling back
     *
     * @return an instance of UART
     */
    public static UART getInstance (Activity activity, View view, UartCallback appCallback) {
        if (uart == null) {
            uart = new UART (activity, view, appCallback);
        } else {
            uart.view = view;
            uart.appCallback = appCallback;
        }
        return uart;
    }

    /**
     * set viewport size for EyeQ App, which is expected to be smaller than screen resolution
     * @param width of EyeQ view port
     * @param height of EyeQ view port
     */
    public static void setViewPort (float width, float height) {
        xRatio = width / deviceWidth;
        yRatio = height / deviceHeight;
    }

    /**
     * Connects to a specified EyeQ deivce
     *
     * @param device to be conenected with
     *
     */
    public void connect (BluetoothDevice device) {
        if (mService != null){
            mService.connect(device);
        }
    }

    /**
     * An Activity or an application must call destroy onDestroy to properly cleanup and close BLE connection
     * @param context application context (getApplicationContext())
     *
     */
    public void destroy(Context context) {
        Log.d(TAG, "onDestroy()");

        try {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        context.unbindService(mServiceConnection);
        mService.stopSelf();
        mService= null;
    }

    private void serviceInit(Context context) {
        Intent bindIntent = new Intent(context, UartService.class);
        context.bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(context).registerReceiver(UARTStatusChangeReceiver, UartIntents.makeUpdateIntentFilter());
    }

    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            ArrayList<BluetoothDevice> deviceList = null;
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if ((deviceList = mService.initialize()).isEmpty()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                appCallback.handleException (UartIntents.NO_PAIRED_DEVICE_FOUND);
            } else if (deviceList.size() == 1){
                mService.connect(deviceList.get(0));
            } else {
                //TODO: send the list to EyeQ App to handle it
            }
        }

        public void onServiceDisconnected(ComponentName classname) {
            mService = null;
            appCallback.handleException (UartIntents.DEVICE_DISCONNECTED);
        }
    };

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //Log.d (TAG, "UARTStatusChangeReceiver action= " + action);
            if (action.equals(UartIntents.ACTION_DATA_AVAILABLE)) {
                dispatchMotionEvent (intent.getByteArrayExtra(UartIntents.EXTRA_DATA));
            } else {
                appCallback.handleException (action);
            }
        }
    };

    private void dispatchMotionEvent (final byte[] uartData){
        // Currently handling only one finger touch
        // It is a total of 15 bytes. First 5 bytes are unused as of now.
        // Bytes 5, 6, 7 represent first finger touch behavior. Default is 0xFF, 0xFF, 0xFF
        // On touch, first 12 bits provide x coordinate and the second 12 bits represent y coordinate of a touch

        if (Byte.toUnsignedInt(uartData[5]) != 0xFF) {
            actionType = MotionEvent.ACTION_DOWN;
            //float t3 = (Byte.toUnsignedInt(uartData[6]) << 4) & 0x000000F0; //(Byte.toUnsignedInt(txData[6]) << 0);
            //float t4 = Byte.toUnsignedInt(uartData[7]);
            float x1 = ((16 * Byte.toUnsignedInt(uartData[5])) + (Byte.toUnsignedInt(uartData[6]) >> 4));
            //int y1 = 16*t3 + t4;
            float y1 = 16 * ((Byte.toUnsignedInt(uartData[6]) << 4) & 0x000000F0) + Byte.toUnsignedInt(uartData[7]);

            // Binocular touch pad is very sensitive
            if (Math.abs(x - x1) > 6 || Math.abs (y - y1) > 6){
                actionType = MotionEvent.ACTION_MOVE;
                //Log.d (TAG, "Moving");
                x = x1;
                y = y1;
            }

            //TODO: It's sending too many touchevents resulting into flickering buttons. dispatchtouchevents should be called on touch state change.
            MotionEvent motionEvent = MotionEvent.obtain(
                    SystemClock.uptimeMillis(),
                    SystemClock.uptimeMillis(),
                    actionType,
                    x*xRatio,
                    y*yRatio,
                    0 // metastate;
            );
            //String debugData = "UART Data " + uartData + " : x1=" + (int)x + " y1=" + (int)y + " action=" + actionType + " View=" + view;
            //Log.d (TAG, debugData);
            view.dispatchTouchEvent(motionEvent);

        } else {
            if (actionType != MotionEvent.ACTION_UP) {
                actionType = MotionEvent.ACTION_UP;
                MotionEvent motionEvent = MotionEvent.obtain(
                        SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(),
                        actionType,
                        (float) x,
                        (float) y,
                        0 // metatate;
                );
                view.dispatchTouchEvent(motionEvent);
            }
        }
    }
}
