package com.sample.mosquito.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.sample.mosquito.R;
import com.sample.mosquito.utils.Notify;

import java.util.ArrayList;
import java.util.List;

public class PermissionActivity extends Activity {

    private String[] PERMISSIONS_RUNTIME = new String[]{
            Manifest.permission.POST_NOTIFICATIONS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestPermissions();
    }

    private void requestPermissions() {
        String[] deniedPermissions = getDeniedPermissions();
        if (deniedPermissions.length > 0) {
            requestPermissions(deniedPermissions, 1234);
            return;
        }

        startActivity(new Intent(this, ConnectActivity.class));
        finish();
    }

    private String[] getDeniedPermissions() {
        List<String> permissions = new ArrayList<>();
        PackageManager packageManager = getPackageManager();
        String packageName = getPackageName();
        for (String permission : PERMISSIONS_RUNTIME) {
            if (packageManager.checkPermission(permission, packageName) != PackageManager.PERMISSION_GRANTED)
                permissions.add(permission);
        }
        return permissions.toArray(new String[permissions.size()]);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                Notify.showToast(permissions[i]);
                finish();
                break;
            }
        }
    }
}