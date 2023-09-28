package notifier.NotificationManager.config;

import notifier.NotificationManager.database.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Initializer {

    @Autowired
    private ReservationRepository reservationRepository;

}
