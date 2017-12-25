package com.boombike.lock;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.boombike.omni.CommandUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Ravy on 2017. 12. 16..
 */

public class UnLockActivity extends BluetoothConnectActivity {

    private static final String TAG = "UnLockActivity";

    private static final int RC_LOCATION = 1;

    private int id;

    private byte bleCKey;

    private FusedLocationProviderClient client;

    private FirebaseDatabase database;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        id = getIntent().getIntExtra(EXTRA_ID, 0);

        client = LocationServices.getFusedLocationProviderClient(this);

        database = FirebaseDatabase.getInstance();

        publishSubject.
                subscribe(integer -> {
                    if (integer == ACTION_LOCK) {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                            ActivityCompat.requestPermissions(this, permissions, RC_LOCATION);
                            return;
                        }

                        sendCurrentLocation();
                    }
                });

        startDiscovery();
    }

    @SuppressLint("MissingPermission")
    private void sendCurrentLocation() {
        client.getLastLocation()
                .addOnSuccessListener(command -> {
                    LatLng latLng = new LatLng(command.getLatitude(), command.getLongitude());
                    database.getReference().child("location").child(String.valueOf(id)).setValue(latLng)
                            .addOnSuccessListener(this, command1 -> {
                                Toast.makeText(getApplicationContext(), "자전거가 정상적으로 잠겼습니다.", Toast.LENGTH_SHORT ).show();
                                finish();
                            })
                            .addOnFailureListener(this, command1 -> {
                                Toast.makeText(getApplicationContext(), "자전거가 불안정하게 잠겼습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT ).show();
                                finish();
                            });
                })
                .addOnFailureListener(command -> {
                    Toast.makeText(getApplicationContext(), "자전거가 불안정하게 잠겼습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT ).show();
                    finish();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RC_LOCATION) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "위치 권한을 다시 한번 확인해주세요.", Toast.LENGTH_SHORT ).show();
                    finish();
                    return;
                }
            }

            startDiscovery();
        }
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
