package tw.edu.tku.csie.weatherforecast;

import android.content.Intent;

/**
 * Holding intent extra names and utility methods for intent handling.
 */
public class IntentUtil {

    /*
     * for transition
     */
    public static final String TRANSITION_NAME_ICON = "icon";
    public static final String TRANSITION_NAME_DATE_TIME = "date_time";
    public static final String TRANSITION_NAME_DESCRIPTION = "description";
    public static final String TRANSITION_NAME_HIGH_TEMP = "high_temp";
    public static final String TRANSITION_NAME_LOW_TEMP = "low_temp";

    public static final String EXTRA_KEY_NAME_LOW_TEMP_SIZE = "low_temp_size";
    public static final String EXTRA_KEY_NAME_LOW_TEMP_PADDING = "low_temp_padding";
    public static final String EXTRA_KEY_NAME_LOW_TEMP_COLOR = "low_temp_padding";

    public static final String EXTRA_KEY_NAME_HIGH_TEMP_SIZE = "high_temp_size";
    public static final String EXTRA_KEY_NAME_HIGH_TEMP_PADDING = "high_temp_padding";
    public static final String EXTRA_KEY_NAME_HIGH_TEMP_COLOR = "high_temp_padding";

    public static final String EXTRA_KEY_NAME_DATE_TIME_SIZE = "date_time_size";
    public static final String EXTRA_KEY_NAME_DATE_TIME_PADDING = "date_time_padding";
    public static final String EXTRA_KEY_NAME_DATE_TIME_COLOR = "date_time_padding";

    public static final String EXTRA_KEY_DESCRIPTION_TIME_SIZE = "description_size";
    public static final String EXTRA_KEY_DESCRIPTION_TIME_PADDING = "description_padding";
    public static final String EXTRA_KEY_DESCRIPTION_TIME_COLOR = "description_padding";


    /**
     * Checks if all extras are present in an intent.
     *
     * @param intent The intent to check.
     * @param extras The extras to check for.
     * @return <code>true</code> if all extras are present, else <code>false</code>.
     */
    public static boolean hasAll(Intent intent, String... extras) {
        for (String extra : extras) {
            if (!intent.hasExtra(extra)) {
                return false;
            }
        }
        return true;
    }

}
