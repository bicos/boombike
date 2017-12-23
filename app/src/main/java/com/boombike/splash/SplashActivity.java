package com.boombike.splash;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.boombike.MainActivity;

/**
 * Created by Ravy on 2017. 12. 16..
 */

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    private static final int RC_ALL_PERM = 2;

    private String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (hasPermissions(permissions)) {
            startMainActivity();
        } else {
            requestPermission(permissions);
        }
    }

    public boolean hasPermissions(String... permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void requestPermission(String... permissions) {
        ActivityCompat.requestPermissions(this, permissions, RC_ALL_PERM);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RC_ALL_PERM: {
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult == PackageManager.PERMISSION_DENIED) {
                            showAlertNeedPermission();
                            return;
                        }
                    }
                    startMainActivity();
                } else {
                    showAlertNeedPermission();
                }
            }
        }
    }

    private void showAlertNeedPermission() {
        new AlertDialog.Builder(this)
                .setMessage("붐바이크를 실행하기 위해서 권한 획득이 필요합니다.")
                .setPositiveButton("확인", (dialog, which) -> requestPermission(permissions))
                .setNegativeButton("취소", (dialog, which) -> finish())
                .show();
    }
}
