package com.boombike.lock;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.boombike.omni.CommandUtil;

/**
 * Created by Ravy on 2017. 12. 16..
 */

public class UnLockActivity extends BluetoothConnectActivity {

    private static final String TAG = "UnLockActivity";

    private int id;

    private byte bleCKey;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        id = getIntent().getIntExtra(EXTRA_ID, 0);
        startDiscovery();
    }

    @Override
    protected void onRequestReady(BluetoothGatt gatt) {
        requestGetKey(gatt);
    }

    @Override
    protected void handCommand(byte[] command) {
        switch (command[7]) {
            case COMMAND_GET_KEY:
                // get key
                bleCKey = command[9];
                requestUnlockKey();
                break;
            case COMMAND_LOCK:
                // lock
                receiveLockEvent();
                sendLockResponse();
                break;
        }
    }

    private void receiveLockEvent() {
        publishSubject.onNext(ACTION_LOCK);
    }

    private void requestUnlockKey() {
        if (getBLEGCWrite() != null && getGatt() != null) {
            byte[] crcOrder = CommandUtil.getCRCOpenCommand(id, bleCKey);
            getBLEGCWrite().setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            getBLEGCWrite().setValue(crcOrder);
            getGatt().writeCharacteristic(getBLEGCWrite());
        } else {
            publishSubject.onNext(BLT_ERROR);
        }
    }

    private void requestGetKey(BluetoothGatt gatt) {
        if (getBLEGCWrite() != null && gatt != null) {
            byte[] crcOrder = CommandUtil.getCRCKeyCommand2(id, (byte) 0);
            getBLEGCWrite().setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            getBLEGCWrite().setValue(crcOrder);
            gatt.writeCharacteristic(getBLEGCWrite());
        } else {
            publishSubject.onNext(BLT_ERROR);
        }
    }

    private void sendLockResponse() {
        if (getBLEGCWrite() != null && getGatt() != null) {
            byte[] crcOrder = CommandUtil.getCRCLockCommand(id, bleCKey);
            getBLEGCWrite().setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            getBLEGCWrite().setValue(crcOrder);
            getGatt().writeCharacteristic(getBLEGCWrite());
        } else {
            publishSubject.onNext(BLT_ERROR);
        }
    }

    public static void start(Context context, int id, String macAddress) {
        Intent intent = new Intent(context, UnLockActivity.class);
        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(EXTRA_MAC_ADDR, macAddress);
        context.startActivity(intent);
    }

}
