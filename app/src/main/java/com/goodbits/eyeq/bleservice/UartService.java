package com.goodbits.eyeq.bleservice;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Set;

public class UartService extends Service {
    private final static String TAG = "[EyeQ] UartService";
    private final IBinder mBinder = new LocalBinder();
    private BluetoothManager mBluetoothManager;
    private BluetoothGatt mBluetoothGatt;
    //private BluetoothDevice eyeqDevice = null;
    private BluetoothGattCallback mGattCallback;

    public UartService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "OnCreate");
        mGattCallback = new UartGattCallback(this);
    }

    /**
     * Called from UART
     * Initializes a reference to the local Bluetooth adapter and retrieves a list ot EyeQ Devices.
     *
     * @return Return null is no EyeQ device found. Else, EyeQ devices
     * Todo: return should be a list of EyeQ devices.
     */
    @Nullable
    public ArrayList<BluetoothDevice> initialize() {
        Log.d(TAG, "initialize");
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
            }
        }

        BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
        }

        return getEyeQDevice (mBluetoothAdapter);
        //BluetoothDevice eyeqDevice = getEyeQDevice (mBluetoothAdapter);
        //if (eyeqDevice != null){
        //    connect (eyeqDevice);
        //}
        //return eyeqDevice;
    }

    public void connect (@NonNull BluetoothDevice device){
        if (device != null){
            Log.d(TAG, device.toString());
            device.connectGatt(this, false, mGattCallback);
        }

    }

    public class LocalBinder extends Binder {
        public UartService getService() {
            Log.d (TAG, "Binder: getService");
            return UartService.this;
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        Log.d (TAG, "onBind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d (TAG, "onUnbind");
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private ArrayList<BluetoothDevice> getEyeQDevice (BluetoothAdapter mBluetoothAdapter) {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        ArrayList<BluetoothDevice> eyeqDevices = new ArrayList<BluetoothDevice>();

        //todo: look for more thn one eyeq devices and add them to list of devices
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                Log.d (TAG, device.getName() + " : " + device.getAddress());
                if (device.getName().contains("eyeQ")) {
                    //return device;
                    eyeqDevices.add(device);
                }
            }
        }
        Log.d(TAG, "No Paired Device.");
        return eyeqDevices;
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt != null) {
            Log.w(TAG, "mGattCallback closed");
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }
}
