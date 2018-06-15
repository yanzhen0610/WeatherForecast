package tw.edu.tku.csie.weatherforecast.sync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import tw.edu.tku.csie.weatherforecast.data.WeatherAppPreferences;

public class UpdateLocalizedAndEnglishCityName extends IntentService {

    public UpdateLocalizedAndEnglishCityName() {
        super("UpdateLocalizedAndEnglishCityName");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Geocoder localeGeoCoder = new Geocoder(this);
        Geocoder engGeoCoder = new Geocoder(this, Locale.ENGLISH);
        String preferredLocation = WeatherAppPreferences.getLocalePreferredWeatherLocation(this);
        List<Address> localeAddresses = null;
        List<Address> engAddresses = null;
        try {
            localeAddresses = localeGeoCoder.getFromLocationName(preferredLocation, 1);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        try {
            engAddresses = engGeoCoder.getFromLocationName(preferredLocation, 1);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        if (localeAddresses != null && localeAddresses.size() > 0) {
            Address address = localeAddresses.get(0);
            String location = address.getLocality();
            WeatherAppPreferences.setLocalePreferredWeatherLocation(this, location);
        }
        if (engAddresses != null && engAddresses.size() > 0) {
            Address address = engAddresses.get(0);
            String location = address.getLocality();
            WeatherAppPreferences.setEngPreferredWeatherLocation(this, location);
            SyncUtils.startImmediateSync(this);
        }
    }

    public static void startUpdate(Context context) {
        context.startService(new Intent(context, UpdateLocalizedAndEnglishCityName.class));
    }

}
