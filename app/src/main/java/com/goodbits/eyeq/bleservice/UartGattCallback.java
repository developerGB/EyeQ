package com.goodbits.eyeq.bleservice;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class UartGattCallback extends BluetoothGattCallback {

    private final static String TAG = "[EyeQ] UARTGattCallback";
    private static Context context;

    public UartGattCallback(Context c) {
        context = c;
    }

    @Override
    public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        Log.d(TAG, "onConnectionStateChange- " + status + ":" + newState);

        StringBuilder newLog = new StringBuilder();

        switch (newState) {
            case BluetoothGatt.STATE_CONNECTING:
                Log.d(TAG, "onConnectionStateChange for device: " + gatt.getDevice() + " STATE_CONNECTING");
                broadcastUpdate(UartIntents.DEVICE_CONNECTING);
                break;
            case BluetoothGatt.STATE_CONNECTED:
                Log.d(TAG, "onConnectionStateChange for device: " + gatt.getDevice() + " STATE_CONNECTED");
                broadcastUpdate(UartIntents.DEVICE_CONNECTED);
                boolean started = gatt.discoverServices();
                Log.d(TAG, "discoverServices started: " + started);
                break;
            case BluetoothGatt.STATE_DISCONNECTING:
                Log.d(TAG, "onConnectionStateChange for device: " + gatt.getDevice() + " STATE_DISCONNECTING");
                broadcastUpdate(UartIntents.DEVICE_DISCONNECTING);
                break;
            case BluetoothGatt.STATE_DISCONNECTED:
                Log.d(TAG, "onConnectionStateChange for device: " + gatt.getDevice() + " STATE_DISCONNECTED");
                broadcastUpdate(UartIntents.DEVICE_DISCONNECTED);
                break;
            default:
                Log.d(TAG, "onConnectionStateChange for device: " + gatt.getDevice() + " " + newState + " from " + status);
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        Log.d(TAG, "onServicesDiscovered");
        enableTXNotification(gatt);
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic characteristic,
                                     int status) {
        Log.d(TAG, "onCharacteristicRead");
    }
/*
        @Override
        public void onBluetoothStateChange (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onBluetoothStateChange= " + status);
        }

 */

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic characteristic) {
        //Log.d(TAG, "onCharacteristicChanged");
        broadcastUpdate(UartIntents.ACTION_DATA_AVAILABLE, characteristic);
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is handling for the notification on TX Character of NUS service
        if (UUID.TX_CHAR_UUID.equals(characteristic.getUuid())) {
            intent.putExtra(UartIntents.EXTRA_DATA, characteristic.getValue());
        } else {
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * Enable Notification on TX characteristic
     *
     * @return
     */
    public void enableTXNotification(BluetoothGatt mBluetoothGatt) {
        Log.d(TAG, "EnableTXNotification");

        BluetoothGattService RxService = mBluetoothGatt.getService(UUID.RX_SERVICE_UUID);
        if (RxService == null) {
            Log.d(TAG, "Rx service not found!");
            return;
        }
        BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(UUID.TX_CHAR_UUID);
        if (TxChar == null) {
            Log.d(TAG, "Tx charateristic not found!");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(TxChar, true);

        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(UUID.CCCD);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);

    }

}
