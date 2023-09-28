package com.project.ReservationTGBot;

import com.project.ReservationTGBot.utils.Browser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@SpringBootApplication
public class ReservationTgBotApplication {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(ReservationTgBotApplication.class, args);
		// Browser.openURL("https://web.telegram.org/k/#-4073409748");
		// Browser.openURL("https://web.telegram.org/k/#@SalonReservationBot");
	}

}
