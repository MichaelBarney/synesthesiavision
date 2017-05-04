package com.bananadigital.sound3d;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for get permissions from user.
 */
public class PermissionManager {

    /**
     * Function that get permissions from user to use any phone elements
     *
     * @param      activity     The activity
     * @param      permissions  The permissions
     * @param      requestCode  The request code
     *
     * @return     return true when the permission is granted, else start activity compat to request permissions
     */
    public static boolean checkPermission(Activity activity, String[] permissions, int requestCode){

        List<String> listPermission = new ArrayList<>();

        //If the phone has too old Android, this is not necessary.
        if(Build.VERSION.SDK_INT >= 23) {
            for(String permission : permissions) {

                //Check if the permission is granted
                boolean isOkPermission = ContextCompat.checkSelfPermission(activity,permission) ==
                        PackageManager.PERMISSION_GRANTED;

                //If not, add to listPermission to get permission later.
                if(!isOkPermission) listPermission.add(permission);
            }
            
            //if list has no elements, so all permissions are granted.
            if(listPermission.isEmpty()) return true;

            //Creates a new Array of String which contains the permissions not granted.
            String[] newPermission = new String[listPermission.size()];
            listPermission.toArray(newPermission);
            
            //Show a dialog box to get permission from user.
            ActivityCompat.requestPermissions(activity, newPermission, requestCode);
        }

        return true;
    }
}