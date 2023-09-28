package notifier.NotificationManager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NotificationManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationManagerApplication.class, args);
	}

}
