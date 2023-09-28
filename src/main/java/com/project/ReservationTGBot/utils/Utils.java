package com.project.ReservationTGBot.utils;

//import org.checkerframework.checker.units.qual.A;
import com.project.ReservationTGBot.database.ReservationRepository;
import com.project.ReservationTGBot.database.Service;
import org.springframework.data.repository.CrudRepository;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.yaml.snakeyaml.events.Event;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.*;
import java.util.Locale;
import java.util.Date;
import java.text.SimpleDateFormat;

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

    public static String getNotificationQuery(int number) {
        return "SELECT r FROM reservation_data r WHERE " +
                "r.notified" + number + " = false " +
                "and r.dateTime >= :now " +
                "and r.dateTime >= :dateCheck " +
                "and " + ReservationRepository.actualityQuery;
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

    public static Date asDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }
    public static Date asDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDateTime asLocalDateTime(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    // public static Map<String, String> personify(Map<String, String> options, String chatId) {
    //     Map<String, String> personOptions = new HashMap<>();
    //     options.forEach((command, message) -> {
    //         personOptions.put(command+":"+chatId, message);
    //     });
    //     return personOptions;
    // }

    public static <T> HashMap<String, String> manageOptions(CrudRepository<T, Event.ID> optionsObjects) {

        Collection<T> services = new ArrayList<>();
        optionsObjects.findAll().forEach(services::add);
        HashMap<String, String> result = new HashMap<>();
        for (Object object : services) {
//            result.put(object,)
        }
        return result;
    }
    public static InputFile getInputFile(String path) throws IOException {
        File initialFile = new File(path);
        InputFile result = new InputFile(initialFile, "Salon_photo");
        return result;
    }

}

