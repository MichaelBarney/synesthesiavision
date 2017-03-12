package com.bananadigital.sound3d;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionManager {

    public static boolean checkPermission(Activity activity, String[] permissions, int requestCode){

        List<String> listPermission = new ArrayList<>();

        if(Build.VERSION.SDK_INT >= 23) {
            for(String permission : permissions) {
                boolean isOkPermission = ContextCompat.checkSelfPermission(activity,permission) ==
                        PackageManager.PERMISSION_GRANTED;
                if(!isOkPermission) listPermission.add(permission);
            }

            if(listPermission.isEmpty()) return true;
            String[] newPermission = new String[listPermission.size()];
            listPermission.toArray(newPermission);
            ActivityCompat.requestPermissions(activity, newPermission, requestCode);
        }

        return true;
    }
}