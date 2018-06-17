package tw.edu.tku.csie.weatherforecast.utilities;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import tw.edu.tku.csie.weatherforecast.data.WeatherAppPreferences;

public class UpdateCurrentLocation {

    public static boolean updateCurrentLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            try {
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                if (lastKnownLocation != null) {
                    Log.v("Location:", String.valueOf(lastKnownLocation.getLatitude()));
                    Log.v("Location:", String.valueOf(lastKnownLocation.getLongitude()));
                    WeatherAppPreferences.setLocationDetails(context,
                            lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    return true;
                } else {
                    Log.v("Location:", "lastKnownLocation = null");
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
