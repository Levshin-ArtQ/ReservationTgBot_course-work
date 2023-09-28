package notifier.NotificationManager;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

public class Utils {
    static String format = "dd MMM, E";

    static String formatWithTime = "HH:mm(z), dd MMMM Y, EE";

    static Locale locale = new Locale("ru");
    static SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
    static SimpleDateFormat sdf_t = new SimpleDateFormat(formatWithTime, locale);
    public static String reformatDateString(LocalDate date) {
        String result;
        try {
            result = sdf.format(asDate(date));
        } catch (Exception e) {
            System.out.println("parsing problem");
            throw new RuntimeException(e);
        }
        return result;
    }
    public static Date asDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }
    public static Date asDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
    public static String reformatDateTimeString(LocalDateTime date) {
        String result;
        try {
            result = sdf_t.format(asDate(date));
        } catch (Exception e) {
            System.out.println("parsing problem");
            throw new RuntimeException(e);
        }
        return result;
    }
}
