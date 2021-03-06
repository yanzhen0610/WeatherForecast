/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tw.edu.tku.csie.weatherforecast;

import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import tw.edu.tku.csie.weatherforecast.data.WeatherAppContract;
import tw.edu.tku.csie.weatherforecast.data.WeatherAppPreferences;
import tw.edu.tku.csie.weatherforecast.databinding.ActivityForecastBinding;
import tw.edu.tku.csie.weatherforecast.sync.SyncUtils;
import tw.edu.tku.csie.weatherforecast.transition.TransitionUtils;
import tw.edu.tku.csie.weatherforecast.utilities.UpdateCurrentLocation;
import tw.edu.tku.csie.weatherforecast.utilities.PermissionUtils;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        ForecastAdapter.ForecastAdapterOnClickHandler {

    private final String TAG = MainActivity.class.getSimpleName();

    /*
     * The columns of data that we are interested in displaying within our MainActivity's list of
     * weather data.
     */
    public static final String[] MAIN_FORECAST_PROJECTION = {
            WeatherAppContract.WeatherEntry.COLUMN_DATE_TIME,
            WeatherAppContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherAppContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherAppContract.WeatherEntry.COLUMN_WEATHER_ID,
    };

    /*
     * We store the indices of the values in the array of Strings above to more quickly be able to
     * access the data from our query. If the order of the Strings above changes, these indices
     * must be adjusted to match the order of the Strings.
     */
    public static final int INDEX_WEATHER_DATE_TIME = 0;
    public static final int INDEX_WEATHER_MAX_TEMP = 1;
    public static final int INDEX_WEATHER_MIN_TEMP = 2;
    public static final int INDEX_WEATHER_CONDITION_ID = 3;


    /*
     * This ID will be used to identify the Loader responsible for loading our weather forecast. In
     * some cases, one Activity can deal with many Loaders. However, in our case, there is only one.
     * We will still use this ID to initialize the loader and create the loader for best practice.
     * Please note that 44 was chosen arbitrarily. You can use whatever number you like, so long as
     * it is unique and consistent.
     */
    private static final int ID_FORECAST_LOADER = 44;

    private ForecastAdapter mForecastAdapter;
    private int mPosition = RecyclerView.NO_POSITION;

    private ActivityForecastBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_forecast);
        getSupportActionBar().setElevation(0f);

        /*
         * A LinearLayoutManager is responsible for measuring and positioning item views within a
         * RecyclerView into a linear list. This means that it can produce either a horizontal or
         * vertical list depending on which parameter you pass in to the LinearLayoutManager
         * constructor. In our case, we want a vertical list, so we pass in the constant from the
         * LinearLayoutManager class for vertical lists, LinearLayoutManager.VERTICAL.
         *
         * There are other LayoutManagers available to display your data in uniform grids,
         * staggered grids, and more! See the developer documentation for more details.
         *
         * The third parameter (shouldReverseLayout) should be true if you want to reverse your
         * layout. Generally, this is only true with horizontal lists that need to support a
         * right-to-left layout.
         */
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        /* setLayoutManager associates the LayoutManager we created above with our RecyclerView */
        mBinding.recyclerviewForecast.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mBinding.recyclerviewForecast.setHasFixedSize(true);

        /*
         * The ForecastAdapter is responsible for linking our weather data with the Views that
         * will end up displaying our weather data.
         *
         * Although passing in "this" twice may seem strange, it is actually a sign of separation
         * of concerns, which is best programming practice. The ForecastAdapter requires an
         * Android Context (which all Activities are) as well as an onClickHandler. Since our
         * MainActivity implements the ForecastAdapter ForecastOnClickHandler interface, "this"
         * is also an instance of that type of handler.
         */
        mForecastAdapter = new ForecastAdapter(this, this);

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mBinding.recyclerviewForecast.setAdapter(mForecastAdapter);

        mBinding.weatherForecastSwipeRefresh.setOnRefreshListener(this::refresh);


        showLoading();

        /*
         * Ensures a loader is initialized and active. If the loader doesn't already exist, one is
         * created and (if the activity/fragment is currently started) starts the loader. Otherwise
         * the last created loader is re-used.
         */
        getSupportLoaderManager().initLoader(ID_FORECAST_LOADER, null, this);
        loaderCallbackAfterOnCreate = true;

        SyncUtils.initialize(this);

        updateLocation();
    }

    private boolean loaderPreventCallbackAfterOnStart = false;
    private boolean loaderCallbackAfterOnCreate = true;

    @Override
    protected void onStart() {
        if (loaderCallbackAfterOnCreate) {
            loaderCallbackAfterOnCreate = false;
            loaderPreventCallbackAfterOnStart = false;
        } else {
            loaderPreventCallbackAfterOnStart = true;
        }
        super.onStart();
    }

    private void updateLocation() {
        if (WeatherAppPreferences.isUseCurrentLocation(this)) {
            if (PermissionUtils.checkAccessFineLocationPermission(this)) {
                UpdateCurrentLocation.updateCurrentLocation(this);
            } else {
                WeatherAppPreferences.setUseCurrentLocation(this, false);
            }
        }
    }

    /**
     * Uses the URI scheme for showing a location found on a map in conjunction with
     * an implicit Intent. This super-handy Intent is detailed in the "Common Intents" page of
     * Android's developer site:
     *
     * @see "http://developer.android.com/guide/components/intents-common.html#Maps"
     * <p>
     * Protip: Hold Command on Mac or Control on Windows and click that link to automagically
     * open the Common Intents page
     */
    private void openPreferredLocationInMap() {
        double[] coords = WeatherAppPreferences.getLocationCoordinates(this);
        String posLat = Double.toString(coords[0]);
        String posLong = Double.toString(coords[1]);
        Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(TAG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
        }
    }

    /**
     * Called by the {@link android.support.v4.app.LoaderManagerImpl} when a new Loader needs to be
     * created. This Activity only uses one loader, so we don't necessarily NEED to check the
     * loaderId, but this is certainly best practice.
     *
     * @param loaderId The loader ID for which we need to create a loader
     * @param bundle   Any arguments supplied by the caller
     * @return A new Loader instance that is ready to start loading.
     */
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {

        switch (loaderId) {

            case ID_FORECAST_LOADER:
                /* URI for all rows of weather data in our weather table */
                Uri forecastQueryUri = WeatherAppContract.WeatherEntry.CONTENT_URI;
                /* Sort order: Ascending by date */
                String sortOrder = WeatherAppContract.WeatherEntry.COLUMN_DATE_TIME + " ASC";
                /*
                 * A SELECTION in SQL declares which rows you'd like to return. In our case, we
                 * want all weather data from today onwards that is stored in our weather table.
                 * We created a handy method to do that in our WeatherEntry class.
                 */
                String selection = WeatherAppContract.WeatherEntry.getSqlSelectForTodayOnwards();

                return new CursorLoader(this,
                        forecastQueryUri,
                        MAIN_FORECAST_PROJECTION,
                        selection,
                        null,
                        sortOrder);

            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    /**
     * Called when a Loader has finished loading its data.
     *
     * NOTE: There is one small bug in this code. If no data is present in the cursor do to an
     * initial load being performed with no access to internet, the loading indicator will show
     * indefinitely, until data is present from the ContentProvider. This will be fixed in a
     * future version of the course.
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (loaderPreventCallbackAfterOnStart) {
            loaderPreventCallbackAfterOnStart = false;
        } else {
            mForecastAdapter.swapCursor(data);
            if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
            mBinding.recyclerviewForecast.smoothScrollToPosition(mPosition);
            if (data.getCount() != 0) showWeatherDataView();
            mBinding.weatherForecastSwipeRefresh.setRefreshing(false);
        }
    }

    /**
     * Called when a previously created loader is being reset, and thus making its data unavailable.
     * The application should at this point remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        /*
         * Since this Loader's data is now invalid, we need to clear the Adapter that is
         * displaying the data.
         */
        mForecastAdapter.swapCursor(null);
    }

    /**
     * This method is for responding to clicks from our list.
     *
     * @param date Normalized UTC time that represents the local date of the weather in GMT time.
     * @see WeatherAppContract.WeatherEntry#COLUMN_DATE_TIME
     */
    @Override
    public void onClick(View view, long date) {
        Intent weatherDetailIntent = new Intent(MainActivity.this, DetailActivity.class);
        Uri uriForDateClicked = WeatherAppContract.WeatherEntry.buildWeatherUriWithDate(date);
        weatherDetailIntent.setData(uriForDateClicked);

        /* add transition animation */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setDetailActivityTransitionData(weatherDetailIntent, view);
            ActivityOptionsCompat activityOptionsCompat = getStartDetailActivityOptions(view);
            startActivity(weatherDetailIntent, activityOptionsCompat.toBundle());
        } else {
            startActivity(weatherDetailIntent);
        }
    }

    private void setDetailActivityTransitionData(Intent intent, View view) {
        intent.setAction(Intent.ACTION_VIEW);

        final TextView dateTime = view.findViewById(R.id.date);
        TransitionUtils.addTextViewInfo(intent, getString(R.string.transition_name_date), dateTime);

        final TextView description = view.findViewById(R.id.weather_description);
        TransitionUtils.addTextViewInfo(intent, getString(R.string.transition_name_description), description);

        final TextView highTemp = view.findViewById(R.id.high_temperature);
        TransitionUtils.addTextViewInfo(intent, getString(R.string.transition_name_high_temp), highTemp);

        final TextView lowTemp = view.findViewById(R.id.low_temperature);
        TransitionUtils.addTextViewInfo(intent, getString(R.string.transition_name_low_temp), lowTemp);
    }

    private ActivityOptionsCompat getStartDetailActivityOptions(View view) {
        return ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                Pair.create(view.findViewById(R.id.date), getString(R.string.transition_name_date)),
                Pair.create(view.findViewById(R.id.weather_icon), getString(R.string.transition_name_weather_icon)),
                Pair.create(view.findViewById(R.id.weather_description), getString(R.string.transition_name_description)),
                Pair.create(view.findViewById(R.id.high_temperature), getString(R.string.transition_name_high_temp)),
                Pair.create(view.findViewById(R.id.low_temperature), getString(R.string.transition_name_low_temp)));
    }

    /**
     * This method will make the View for the weather data visible and hide the error message and
     * loading indicator.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't need to check whether
     * each view is currently visible or invisible.
     */
    private void showWeatherDataView() {
        /* First, hide the loading indicator */
//        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mBinding.pbLoadingIndicator.setVisibility(View.INVISIBLE);
        /* Finally, make sure the weather data is visible */
//        mRecyclerView.setVisibility(View.VISIBLE);
        mBinding.recyclerviewForecast.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the loading indicator visible and hide the weather View and error
     * message.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't need to check whether
     * each view is currently visible or invisible.
     */
    private void showLoading() {
        /* Then, hide the weather data */
//        mRecyclerView.setVisibility(View.INVISIBLE);
        mBinding.recyclerviewForecast.setVisibility(View.INVISIBLE);
        /* Finally, show the loading indicator */
//        mLoadingIndicator.setVisibility(View.VISIBLE);
        mBinding.pbLoadingIndicator.setVisibility(View.VISIBLE);
    }

    /**
     * This is where we inflate and set up the menu for this Activity.
     *
     * @param menu The options menu in which you place your items.
     *
     * @return You must return true for the menu to be displayed;
     *         if you return false it will not be shown.
     *
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.forecast, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    private void refresh() {
        if (WeatherAppPreferences.isUseCurrentLocation(this)) {
            if (PermissionUtils.checkAccessFineLocationPermission(this)) {
                UpdateCurrentLocation.updateCurrentLocation(this);
                SyncUtils.startImmediateSync(this);
            } else {
                WeatherAppPreferences.setUseCurrentLocation(this, false);
            }
        } else {
            SyncUtils.startImmediateSync(this);
        }
    }

    /**
     * Callback invoked when a menu item was selected from this Activity's menu.
     *
     * @param item The menu item that was selected by the user
     *
     * @return true if you handle the menu click here, false otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_map) {
            openPreferredLocationInMap();
            return true;
        }
        if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        if (id == R.id.action_refresh) {
            refresh();
        }

        return super.onOptionsItemSelected(item);
    }
}
