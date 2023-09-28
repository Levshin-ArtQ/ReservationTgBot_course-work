package com.project.ReservationTGBot.config;

import com.project.ReservationTGBot.ReservationBot;
import com.project.ReservationTGBot.database.ReservationRepository;
import com.project.ReservationTGBot.database.Service;
import com.project.ReservationTGBot.database.ServiceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.LongPollingBot;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class Initializer {
    @Autowired
    ReservationBot bot;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ReservationRepository reservationRepository;


    Map<String, String> serviceNames;
    {
        serviceNames = new HashMap<>();
        serviceNames.put("haircut", "Парикмахерские услуги");
        serviceNames.put("nails", "Ногтевой сервис");
        serviceNames.put("manicure", "Маникюр");
        serviceNames.put("pedicure", "Педикюр");
        serviceNames.put("makeup", "Макияж");
        serviceNames.put("tattooing", "Татуаж");
        serviceNames.put("extensions", "Наращивание");;
        serviceNames.put("extensions_nails", "Наращивание ногтей");
        serviceNames.put("extensions_hair", "Наращивание волос");
        serviceNames.put("extensions_eyelashes", "Наращивание ресниц");
    }
    Map<String, Integer> prices;
    {
        prices = new HashMap<>();
        prices.put("haircut", 600);
        prices.put("manicure", 750);
        prices.put("pedicure", 750);
        prices.put("makeup", 750);
        prices.put("tattooing", 750);
        prices.put("extensions_nails", 900);
        prices.put("extensions_hair", 900);
        prices.put("extensions_eyelashes", 900);
    }
    Map<String, String> emojis;
    {
        emojis = new HashMap<>();
        emojis.put("haircut", " \uD83D\uDC87");
        emojis.put("manicure", " \uD83D\uDC85");
        emojis.put("pedicure", " \uD83E\uDDB6");
        emojis.put("makeup", " \uD83D\uDC44 \uD83D\uDC84");
        emojis.put("tattooing", " \uD83D\uDC41");
        emojis.put("nails", " \uD83D\uDC85");
        emojis.put("extensions", " \uD83D\uDCCF");
        emojis.put("extensions_nails", " \uD83D\uDC85 \uD83D\uDCCF");
        emojis.put("extensions_hair", " \uD83D\uDC69 \uD83D\uDCCF");
        emojis.put("extensions_eyelashes", " (⺣◡⺣) \uD83D\uDCCF");
        emojis.put("all_reservations", " \uD83D\uDCC6");
        emojis.put("reject", "❌");
        emojis.put("accept", "✅");
    }

    Map<String, String> subtypeToType;
    {
        subtypeToType = new HashMap<>();
        subtypeToType.put("manicure", "nails");
        subtypeToType.put("pedicure", "nails");
        subtypeToType.put("extensions_hair", "extensions");
        subtypeToType.put("extensions_nails", "extensions");
        subtypeToType.put("extensions_eyelashes", "extensions");
    }


    public void startServices() {
//        serviceRepository.deleteAll();
        if (serviceRepository.count() < 1) {
            serviceNames.forEach((key, value) -> {
                Service service = new Service(key);
                service.setName(value);
                service.setParentType(subtypeToType.getOrDefault(key, "0"));
                service.setCost(prices.getOrDefault(key, 1));
                service.setEmoji(emojis.getOrDefault(key, ""));
//                String emoji = emojis.get(key);
//                service.setEmoji(emoji != null ? emoji : "");
                serviceRepository.save(service);
            });
        }
    }
    public void markServices() {
        Set<Service> serviceSet = serviceRepository.findAll();
        for (Service service : serviceSet) {
            if (service.hasParentType()) {
                serviceRepository.makeParentType(service.getParentType());
            }
        }
    }

    @EventListener({ContextRefreshedEvent.class})

    public void init() {
        log.info("init called");
        try {
            startServices();
            markServices();
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot((LongPollingBot) bot);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }
}
