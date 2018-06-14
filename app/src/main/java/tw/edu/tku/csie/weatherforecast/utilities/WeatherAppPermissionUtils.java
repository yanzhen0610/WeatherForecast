package tw.edu.tku.csie.weatherforecast.utilities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class WeatherAppPermissionUtils {

    private static final String TAG = "Permission";

    public static boolean checkAccessFineLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED;
    }

    public static void requestAccessFineLocationPermission(Context context) {
        if (!checkAccessFineLocationPermission(context)) {
            Log.v(TAG, "Request permission: ACCESS_FINE_LOCATION");
            ActivityCompat.requestPermissions(
                    (Activity) context,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        }
    }

}
