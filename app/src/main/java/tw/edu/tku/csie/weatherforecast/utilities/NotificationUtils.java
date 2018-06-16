package tw.edu.tku.csie.weatherforecast.utilities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;

import tw.edu.tku.csie.weatherforecast.DetailActivity;
import tw.edu.tku.csie.weatherforecast.R;
import tw.edu.tku.csie.weatherforecast.data.WeatherAppContract;
import tw.edu.tku.csie.weatherforecast.data.WeatherAppPreferences;

public class NotificationUtils {

    /*
     * The columns of data that we are interested in displaying within our notification to let
     * the user know there is new weather data available.
     */
    public static final String[] WEATHER_NOTIFICATION_PROJECTION = {
            WeatherAppContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherAppContract.WeatherEntry.COLUMN_DATE_TIME,
            WeatherAppContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherAppContract.WeatherEntry.COLUMN_MIN_TEMP,
    };

    /*
     * We store the indices of the values in the array of Strings above to more quickly be able
     * to access the data from our query. If the order of the Strings above changes, these
     * indices must be adjusted to match the order of the Strings.
     */
    public static final int INDEX_WEATHER_ID = 0;
    public static final int INDEX_WEATHER_DATE_TIME = 1;
    public static final int INDEX_MAX_TEMP = 2;
    public static final int INDEX_MIN_TEMP = 3;

    /*
     * This notification ID can be used to access our notification after we've displayed it. This
     * can be handy when we need to cancel the notification, or perhaps update it. This number is
     * arbitrary and can be set to whatever you like. 3004 is in no way significant.
     */
    private static final int WEATHER_NOTIFICATION_ID = 3005;

    private static final String CHANNEL_ID = "channel_id_0";

    private static void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.notification_weather_forecast_channel_name);
            String description = context.getString(R.string.notification_weather_forecast_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Constructs and displays a notification for the newly updated weather for today.
     *
     * @param context Context used to query our ContentProvider and use various Utility methods
     */
    public static void notifyUserOfNewWeather(Context context) {

        /* create notification before post notifications */
        createNotificationChannel(context);

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

        /* Build the URI for today's weather in order to show up to date data in notification */
//        Uri todaysWeatherUri = WeatherAppContract.WeatherEntry
//                .buildWeatherUriWithDate(WeatherAppDateUtils.getNormalizedUtcDateForToday());

        /*
         * The MAIN_FORECAST_PROJECTION array passed in as the second parameter is defined in our WeatherAppContract
         * class and is used to limit the columns returned in our cursor.
         */
//        Cursor todayWeatherCursor = context.getContentResolver().query(
//                todaysWeatherUri,
//                WEATHER_NOTIFICATION_PROJECTION,
//                null,
//                null,
//                null);
        Cursor todayWeatherCursor = context.getContentResolver().query(
                forecastQueryUri,
                WEATHER_NOTIFICATION_PROJECTION,
                selection,
                null,
                sortOrder);

        /*
         * If todayWeatherCursor is empty, moveToFirst will return false. If our cursor is not
         * empty, we want to show the notification.
         */
        if (todayWeatherCursor != null && todayWeatherCursor.moveToFirst()) {

            /* Weather ID as returned by API, used to identify the icon to be used */
            int weatherId = todayWeatherCursor.getInt(INDEX_WEATHER_ID);
            double high = todayWeatherCursor.getDouble(INDEX_MAX_TEMP);
            double low = todayWeatherCursor.getDouble(INDEX_MIN_TEMP);

            Resources resources = context.getResources();
            int largeArtResourceId = WeatherUtils
                    .getLargeArtResourceIdForWeatherCondition(weatherId);

            Bitmap largeIcon = BitmapFactory.decodeResource(
                    resources,
                    largeArtResourceId);

            String notificationTitle = context.getString(R.string.app_name);

            String notificationText = getNotificationText(context, weatherId, high, low);

            /* getSmallArtResourceIdForWeatherCondition returns the proper art to show given an ID */
            int smallArtResourceId = WeatherUtils
                    .getSmallArtResourceIdForWeatherCondition(weatherId);

            /*
             * NotificationCompat Builder is a very convenient way to build backward-compatible
             * notifications. In order to use it, we provide a context and specify a color for the
             * notification, a couple of different icons, the title for the notification, and
             * finally the text of the notification, which in our case in a summary of today's
             * forecast.
             */
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .setSmallIcon(smallArtResourceId)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationText)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_MIN);

            /*
             * This Intent will be triggered when the user clicks the notification. In our case,
             * we want to open Sunshine to the DetailActivity to display the newly updated weather.
             */
            Intent detailIntentForToday = new Intent(context, DetailActivity.class);
            long dateTime = todayWeatherCursor.getLong(INDEX_WEATHER_DATE_TIME);
            Uri uriForDateTime = WeatherAppContract.WeatherEntry.buildWeatherUriWithDate(dateTime);
            detailIntentForToday.setData(uriForDateTime);

            TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
            taskStackBuilder.addNextIntentWithParentStack(detailIntentForToday);
            PendingIntent resultPendingIntent = taskStackBuilder
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            notificationBuilder.setContentIntent(resultPendingIntent);

            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);

            /* WEATHER_NOTIFICATION_ID allows you to update or cancel the notification later on */
//            notificationManager.notify(WEATHER_NOTIFICATION_ID, notificationBuilder.build());
            if (notificationManager != null) {
                notificationManager.notify(WEATHER_NOTIFICATION_ID, notificationBuilder.build());
            }

            /*
             * Since we just showed a notification, save the current time. That way, we can check
             * next time the weather is refreshed if we should show another notification.
             */
            WeatherAppPreferences.saveLastNotificationTime(context, System.currentTimeMillis());

            /* Always close your cursor when you're done with it to avoid wasting resources. */
            todayWeatherCursor.close();
        }

    }

    /**
     * Constructs and returns the summary of a particular day's forecast using various utility
     * methods and resources for formatting. This method is only used to create the text for the
     * notification that appears when the weather is refreshed.
     * <p>
     * The String returned from this method will look something like this:
     * <p>
     * Forecast: Sunny - High: 14°C Low 7°C
     *
     * @param context   Used to access utility methods and resources
     * @param weatherId ID as determined by Open Weather Map
     * @param high      High temperature (either celsius or fahrenheit depending on preferences)
     * @param low       Low temperature (either celsius or fahrenheit depending on preferences)
     * @return Summary of a particular day's forecast
     */
    private static String getNotificationText(Context context, int weatherId, double high, double low) {

        /*
         * Short description of the weather, as provided by the API.
         * e.g "clear" vs "sky is clear".
         */
        String shortDescription = WeatherUtils
                .getStringForWeatherCondition(context, weatherId);

        String notificationFormat = context.getString(R.string.format_notification);

        /* Using String's format method, we create the forecast summary */
//        String notificationText = String.format(notificationFormat,
//                shortDescription,
//                WeatherUtils.formatTemperature(context, high),
//                WeatherUtils.formatTemperature(context, low));
//
//        return notificationText;
        return String.format(notificationFormat,
                shortDescription,
                WeatherUtils.formatTemperature(context, high),
                WeatherUtils.formatTemperature(context, low));
    }
}
