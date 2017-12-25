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

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by Ravy on 2017. 12. 24..
 */

abstract public class BluetoothConnectActivity extends AppCompatActivity {

    protected static final String EXTRA_ID = "id";
    protected static final String EXTRA_MAC_ADDR = "mac_addr";

    private static final int REQUEST_ENABLE_BT = 1;

    protected static final int BLT_CONNECT = 0;
    protected static final int BLT_DISCONNECT = 1;
    protected static final int BLT_ERROR = 2;

    protected static final int ACTION_LOCK = 3;

    protected static final byte COMMAND_GET_KEY = 0x11;
    protected static final byte COMMAND_LOCK = 0x22;

    private BluetoothAdapter mAdapter;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic mBLEGCWrite;
    private BluetoothGattCharacteristic mBLEGCRead;

    PublishSubject<Integer> publishSubject = PublishSubject.create();

    private CompositeDisposable compositeDisposable;

    private ProgressDialog dialog;

    private String macAddr;

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

    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            BluetoothGattService bleGattService = gatt.getService(GattAttributes.UUID_SERVICE);

            mBLEGCWrite = bleGattService.getCharacteristic(GattAttributes.UUID_CHARACTERISTIC_WRITE);
            mBLEGCRead = bleGattService.getCharacteristic(GattAttributes.UUID_CHARACTERISTIC_READ);

            BluetoothGattDescriptor descriptor = mBLEGCRead.getDescriptor(GattAttributes.UUID_NOTIFICATION_DESCRIPTOR);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            gatt.setCharacteristicNotification(mBLEGCRead, true);
            onRequestReady(gatt);
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        compositeDisposable = new CompositeDisposable();

        macAddr = getIntent().getStringExtra(EXTRA_MAC_ADDR);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        mAdapter = BluetoothAdapter.getDefaultAdapter();

        publishSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    if (integer == BLT_CONNECT) {
                        Toast.makeText(getApplicationContext(), "블루투스 기기가 연결되었습니다.", Toast.LENGTH_SHORT).show();
                    } else if (integer == BLT_DISCONNECT) {
                        Toast.makeText(getApplicationContext(), "블루투스 기기가 해제되었습니다.", Toast.LENGTH_SHORT).show();
                    } else if (integer == BLT_ERROR) {
                        Toast.makeText(this, "에러 발생", Toast.LENGTH_SHORT).show();
                    } else if (integer == ACTION_LOCK) {
                        Toast.makeText(this, "기기가 잠겼습니다.", Toast.LENGTH_SHORT).show();
                    }
                }, throwable -> {
                    Toast.makeText(this, "에러 발생", Toast.LENGTH_SHORT).show();
                });
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

    protected void startDiscovery() {
        if (mAdapter == null) {
            Toast.makeText(this, "블루투스가 지원되지 않는 단말입니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!mAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        if (dialog == null) {
            dialog = ProgressDialog.show(this, "", "디바이스를 찾는중입니다..");
            dialog.setCancelable(true);
        } else {
            dialog.show();
        }

        mAdapter.startDiscovery();
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

    private void onDataReceived(byte[] values) {
        if (values.length == 2 && values[0] == 32 && values[1] == 0) {
            return;
        }

        byte[] command = new byte[values.length - 2];
        command[0] = values[0];
        if (CRCUtil.CheckCRC(values)) {
            byte head = (byte) (values[1] - 0x32);
            command[1] = head;
            for (int i = 2; i < values.length - 2; i++) {
                command[i] = (byte) (values[i] ^ head);
            }
            handCommand(command);
        } else {
            publishSubject.onNext(BLT_ERROR);
        }
    }

    private void setBluetoothGatt(BluetoothGatt gatt) {
        this.gatt = gatt;
    }

    public BluetoothGatt getGatt() {
        return gatt;
    }

    public BluetoothGattCharacteristic getBLEGCWrite() {
        return mBLEGCWrite;
    }

    public BluetoothGattCharacteristic getBLEGCRead() {
        return mBLEGCRead;
    }

    protected abstract void onRequestReady(BluetoothGatt gatt);

    protected abstract void handCommand(byte[] command);
}
