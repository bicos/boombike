package com.boombike.lock;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.boombike.omni.CRCUtil;
import com.boombike.omni.CommandUtil;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by Ravy on 2017. 12. 16..
 */

public class UnLockActivity extends AppCompatActivity {

    private static final String TAG = "UnLockActivity";

    private static final String EXTRA_ID = "id";
    private static final String EXTRA_MAC_ADDR = "mac_addr";

    private static final int REQUEST_ENABLE_BT = 1;

    private static final int BLT_CONNECT = 0;
    private static final int BLT_DISCONNECT = 1;
    private static final int BLT_ERROR = 2;
    private static final int ACTION_LOCK = 3;

    private static final byte COMMAND_GET_KEY = 0x11;
    private static final byte COMMAND_LOCK = 0x22;

    private BluetoothAdapter mAdapter;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic mBLEGCWrite;
    private BluetoothGattCharacteristic mBLEGCRead;

    PublishSubject<Integer> publishSubject = PublishSubject.create();

    private CompositeDisposable compositeDisposable;

    private ProgressDialog dialog;

    private String id;
    private String macAddr;

    private byte bleCKey;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getAddress().equals(macAddr)) {
                    if (dialog != null) {
                        dialog.setMessage(macAddr + " 에 연결중입니다..");
                    }
                    connect(device)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(gatt -> {
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                                mAdapter.cancelDiscovery();
                                setBluetoothGatt(gatt);
                            }, throwable -> {
                                Log.e("test", "connect", throwable);
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                            });
                }
            }
        }
    };

    private void setBluetoothGatt(BluetoothGatt gatt) {
        this.gatt = gatt;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        compositeDisposable = new CompositeDisposable();

        id = getIntent().getStringExtra(EXTRA_ID);
        macAddr = getIntent().getStringExtra(EXTRA_MAC_ADDR);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        mAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(this, "블루투스 권한을 확인해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!mAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        publishSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    if (integer == BLT_CONNECT) {
                        Toast.makeText(UnLockActivity.this, "블루투스 기기가 연결되었습니다.", Toast.LENGTH_SHORT).show();
                    } else if (integer == BLT_DISCONNECT) {
                        Toast.makeText(UnLockActivity.this, "블루투스 기기가 해제되었습니다.", Toast.LENGTH_SHORT).show();
                    } else if (integer == BLT_ERROR) {
                        Toast.makeText(this, "에러 발생", Toast.LENGTH_SHORT).show();
                    } else if (integer == ACTION_LOCK) {
                        Toast.makeText(this, "기기가 잠겼습니다.", Toast.LENGTH_SHORT).show();
                    }
                }, throwable -> {
                    Toast.makeText(this, "에러 발생", Toast.LENGTH_SHORT).show();
                });

        startDiscovery();
    }

    private void startDiscovery() {
        if (dialog == null) {
            dialog = ProgressDialog.show(this, "", "디바이스를 찾는중입니다..");
        } else {
            dialog.show();
        }

        mAdapter.startDiscovery();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                startDiscovery();
            } else {
                Toast.makeText(this, "블루투스 권한을 확인해주세요.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        unregisterReceiver(mReceiver);
        if (gatt != null) {
            gatt.close();
            gatt = null;
        }
        super.onDestroy();
    }

    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            BluetoothGattService bleGattService = gatt.getService(GattAttributes.UUID_SERVICE);
            for (BluetoothGattCharacteristic bgc : bleGattService.getCharacteristics()) {
                Log.i(TAG, "onServicesDiscovered: bgc=" + bgc.getUuid());
            }

            mBLEGCWrite = bleGattService.getCharacteristic(GattAttributes.UUID_CHARACTERISTIC_WRITE);
            mBLEGCRead = bleGattService.getCharacteristic(GattAttributes.UUID_CHARACTERISTIC_READ);

            BluetoothGattDescriptor descriptor = mBLEGCRead.getDescriptor(GattAttributes.UUID_NOTIFICATION_DESCRIPTOR);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            gatt.setCharacteristicNotification(mBLEGCRead, true);
            requestGetKey(gatt);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            onDataReceived(characteristic.getValue());
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                publishSubject.onNext(BLT_CONNECT);
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                gatt.disconnect();
                publishSubject.onNext(BLT_DISCONNECT);
            }
        }
    };

    private void onDataReceived(byte[] values) {
        if (values.length == 2 && values[0] == 32 && values[1] == 0) {
            return;
        }

        byte[] command = new byte[values.length - 2];
        command[0] = values[0];
        if (CRCUtil.CheckCRC(values)) {
            Log.i(TAG, "onCharacteristicChanged: CRC success");
            byte head = (byte) (values[1] - 0x32);
            command[1] = head;
            for (int i = 2; i < values.length - 2; i++) {
                command[i] = (byte) (values[i] ^ head);
            }
            handCommand(command);
        } else {
            Log.i(TAG, "onCharacteristicChanged: CRC fail");
        }
    }

    private void handCommand(byte[] command) {
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
        if (mBLEGCWrite != null && gatt != null) {
            byte[] crcOrder = CommandUtil.getCRCOpenCommand(Integer.parseInt(id), bleCKey);
            mBLEGCWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            mBLEGCWrite.setValue(crcOrder);
            gatt.writeCharacteristic(mBLEGCWrite);
        } else {
            publishSubject.onNext(BLT_ERROR);
        }
    }

    private void requestGetKey(BluetoothGatt gatt) {
        if (mBLEGCWrite != null && gatt != null) {
            byte[] crcOrder = CommandUtil.getCRCKeyCommand2(Integer.parseInt(id), (byte) 0);
            mBLEGCWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            mBLEGCWrite.setValue(crcOrder);
            gatt.writeCharacteristic(mBLEGCWrite);
        } else {
            publishSubject.onNext(BLT_ERROR);
        }
    }

    private void sendLockResponse() {
        if (mBLEGCWrite != null && gatt != null) {
            byte[] crcOrder = CommandUtil.getCRCLockCommand(Integer.parseInt(id), bleCKey);
            mBLEGCWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            mBLEGCWrite.setValue(crcOrder);
            gatt.writeCharacteristic(mBLEGCWrite);
        } else {
            publishSubject.onNext(BLT_ERROR);
        }
    }

    private Observable<BluetoothGatt> connect(BluetoothDevice device) {
        return Observable.just(device)
                .map(bluetoothDevice -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        return device.connectGatt(getApplicationContext(), false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE);
                    } else {
                        return device.connectGatt(getApplicationContext(), false, bluetoothGattCallback);
                    }
                });
    }

    public static void start(Context context, String id, String macAddress) {
        Intent intent = new Intent(context, UnLockActivity.class);
        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(EXTRA_MAC_ADDR, macAddress);
        context.startActivity(intent);
    }

}
