package tw.edu.tku.csie.weatherforecast.utilities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class PermissionUtils {

    private static final String TAG = "Permission";

    public static boolean checkAccessFineLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED;
    }

    public static void requestAccessFineLocationPermission(Activity activity) {
        if (!checkAccessFineLocationPermission(activity)) {
            Log.v(TAG, "Request permission: ACCESS_FINE_LOCATION");
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        }
    }

}
