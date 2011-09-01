/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trafficownage.util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Gerrit
 */
public class StringFormatter {
    
    private static final DecimalFormat DECIMALFORMAT = new DecimalFormat("#.##");
    
    public static String getTwoDecimalDoubleString(double value) {
        return DECIMALFORMAT.format(value);
    }
    
    public static String getTimeString(double simulatedTime) {
        long seconds = (long)simulatedTime;
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        long hours = TimeUnit.MINUTES.toHours(minutes);

        seconds -= TimeUnit.MINUTES.toSeconds(minutes);
        minutes -= TimeUnit.HOURS.toMinutes(hours);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy'_'MM'_'dd'_'HH'_'mm'_'ss");
    
    public static String getDateTimeFileString() {
        Calendar currentDate = Calendar.getInstance();
        return DATE_FORMATTER.format(currentDate.getTime());
    }
}
