package notifier.NotificationManager;

import notifier.NotificationManager.config.NManagerConfig;
import notifier.NotificationManager.database.Reservation;
import notifier.NotificationManager.database.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class NotificationManager {
    final NManagerConfig config;

    @Autowired
    private ReservationRepository reservationRepository;
    private static final Logger log = LoggerFactory.getLogger(NotificationManager.class);

    public NotificationManager(NManagerConfig config) {
        this.config = config;
    }
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(fixedRate = 50000)
    public void reportCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();
        if (now.isAfter(today.atTime(7, 0)) && now.isBefore(today.atTime(22,0))) {
            List<Reservation> reservationsNotification1 = new ArrayList<>();
            List<Reservation> reservationsNotification2 = new ArrayList<>();
            List<Reservation> reservationsNotification3 = new ArrayList<>();

            try {
                reservationsNotification1 = reservationRepository.findReservationsForNotification1(now, now.plusHours(48));
                reservationsNotification2 = reservationRepository.findReservationsForNotification2(now, now.plusHours(1));
                reservationsNotification3 = reservationRepository.findReservationsForNotification3(now, now.plusHours(3));
            } catch (Exception e) {
                log.error(e.toString());
            }
            log.info("\uD83D\uDD0D Reservations found: " + reservationsNotification1.size() + ", " + reservationsNotification2.size() + ", " + reservationsNotification3.size());
            for (Reservation reservation : reservationsNotification1) {
                sendToTelegram(reservation);
                reservationRepository.setNotified1(reservation.getID());
            }
            for (Reservation reservation : reservationsNotification2) {
                sendToTelegram(reservation);
                reservationRepository.setNotified2(reservation.getID());
            }
            for (Reservation reservation : reservationsNotification3) {
                sendToTelegram(reservation, String.valueOf(config.getMastersChatId()));
                reservationRepository.setNotified3(reservation.getID());
            }
        }
    }

    public static void sendToTelegram(Reservation reservation, String chatID) {
        log.info("\uD83D\uDCE8 Sending scheduled notification---------------------------------------+" + chatID);

        String urlString = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s";

        //Add Telegram token (given Token is fake)
        String apiToken = "6275988545:AAGw1xaOVyIaM17YkV2teXC5BBNlZswSuas";


        urlString = String.format(urlString, apiToken, chatID, URLEncoder.encode("Напоминание о записи!\n" + reservation, StandardCharsets.UTF_8));

        try {
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            InputStream is = new BufferedInputStream(conn.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendToTelegram(Reservation reservation) {
        sendToTelegram(reservation, String.valueOf(reservation.getChatID()));
    }

}
