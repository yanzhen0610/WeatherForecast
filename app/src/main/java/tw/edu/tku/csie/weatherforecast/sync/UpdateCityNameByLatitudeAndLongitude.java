package tw.edu.tku.csie.weatherforecast.sync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import tw.edu.tku.csie.weatherforecast.data.WeatherAppPreferences;

public class UpdateCityNameByLatitudeAndLongitude extends IntentService {

    public UpdateCityNameByLatitudeAndLongitude() {
        super("UpdateCityNameByLatitudeAndLongitude");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (WeatherAppPreferences.isLocationLatLonAvailable(this)) {
            Geocoder geocoder = new Geocoder(this);
            double[] coordinate = WeatherAppPreferences.getLocationCoordinates(this);
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(coordinate[0], coordinate[1], 1);
            } catch (IOException e) {
                Log.d("Address", "IOException" + e.getMessage());
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                Log.d("Address", "IllegalArgumentException" + e.getMessage());
                e.printStackTrace();
            }
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String city = address.getLocality();
                String previous = WeatherAppPreferences.getCurrentCity(this);
                if (previous == null || !previous.equals(city)) {
                    WeatherAppPreferences.resetCurrentCity(this);
                    WeatherAppPreferences.setCurrentCity(this, city);
                    // auto sync only when it's changed
                    SyncUtils.startImmediateSync(this);
                }
                Log.d("Address", city);
            } else {
                Log.d("Address", "null or 0");
            }
        }
    }

    public static void startUpdate(Context context) {
        context.startService(new Intent(context, UpdateCityNameByLatitudeAndLongitude.class));
    }
}
