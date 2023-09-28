package com.project.ReservationTGBot;


import com.project.ReservationTGBot.components.Buttons;
import com.project.ReservationTGBot.config.BotConfig;
import com.project.ReservationTGBot.database.*;
import com.project.ReservationTGBot.utils.Utils;

import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.project.ReservationTGBot.components.BotCommands.*;
import static com.project.ReservationTGBot.utils.Utils.reformatDateString;
import static java.lang.Math.toIntExact;
/**
 * ReservationBot - отвечает за обработку приходящий от Telegram update-ов
 */
@Slf4j
@Component
public class ReservationBot extends TelegramLongPollingBot {
    final BotConfig config;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Map<Long, Reservation> reservationsInProgress = new HashMap<>();

    /**
     * Checks if user already exists in database and saves is if not
     * @param userId - User id
     * @param userName - User name in his Telegram account
     */
    private void updateDB(long userId, String userName) {
        if(userRepository.findById(userId).isEmpty()){
            User user = new User();
            user.setId(userId);
            user.setName(userName);
            //сразу добавляем в столбец каунтера 1 сообщение
            user.setMsg_numb(2);

            userRepository.save(user);
            log.info("Added to DB: " + user);
        } else {
            userRepository.updateMsgNumberByUserId(userId);
        }
    }

    /* allows to create an always accessible list of commands in chat */
    public ReservationBot(BotConfig config) {
        this.config = config;
        try {
            this.execute(new SetMyCommands(LIST_OF_COMMANDS, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e){
            log.error(e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    /**
     * Method that first interacts with update from telegram
     * and spreads tasks for other methods
     */
    @Override
    public void onUpdateReceived(@NotNull Update update) {
        long chatId = 0;
        long userId = 0;
        String userName = null;
        String receivedMessage;
        Integer messageId = null;

        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
            userId = update.getMessage().getFrom().getId();
            userName = update.getMessage().getFrom().getFirstName();
            messageId = update.getMessage().getMessageId();

            if (update.getMessage().hasText()) {
                receivedMessage = update.getMessage().getText();
                if (chatId == (config.getMastersChatId())) {
                    botAnswerUtilsMasters(receivedMessage, messageId);
                } else
                    botAnswerUtils(receivedMessage, chatId, userName, messageId);
            }
            if (update.getMessage().hasText() || update.getMessage().hasPhoto()) {
                Reservation currentRes = reservationsInProgress.get(chatId);
                if (currentRes == null) return;
                if (currentRes.getServiceType() != null) {
                    log.info("cashed not completed reservations: " + reservationsInProgress.size());
                    sendDatesMessage(chatId, "weeks", currentRes.getServiceCode());
                    currentRes.setDescriptionMessageID(messageId);
                    reservationsInProgress.put(chatId, currentRes);
                }
            }
        }
        else if (update.hasCallbackQuery()) {
            String replaceText = "";
            chatId = update.getCallbackQuery().getMessage().getChatId();
            userId = update.getCallbackQuery().getFrom().getId();
            userName = update.getCallbackQuery().getFrom().getFirstName();
            receivedMessage = update.getCallbackQuery().getData();
            if (chatId == (config.getMastersChatId())) {
                replaceText = botAnswerUtilsMasters(receivedMessage, messageId);
            }
            else {
                replaceText = botAnswerUtils(receivedMessage, chatId, userName, messageId);
            }
            hideKeyboard(update.getCallbackQuery().getMessage(), replaceText);
        }

        if (chatId != (config.getMastersChatId())){
            updateDB(userId, userName);
        }
    }

    private String botAnswerUtilsMasters(String receivedMessage, Integer messageId) {
        String[] param = receivedMessage.split(":");
        String replaceText = "";
        String param2 = "";
        if (param.length > 1)
            param2 = param[1];
        switch (param[0]) {
            case "accept", "reject", "/accept", "/reject" -> {
                int reservationId;
                try {
                    reservationId = Integer.parseInt(param2);
                } catch (NumberFormatException e) {
                    log.error("В сообщении c опциями для мастеров, не валидный ID записи: {}", param2);
                    return "‼️Что-то пошло не так, запись клиента осталась без изменений, зовите программиста";
                }
                Reservation clientReservation = reservationRepository.findByID(reservationId);
                clientReservation.setAccepted(param[0].equals("accept") || param[0].equals("/accept"));
                clientReservation.setRejected(param[0].equals("reject") || param[0].equals("/reject"));
                String answer = "Ваша запись " + (param[0].equals("accept") || param[0].equals("/accept") ? "просмотрена и принята мастером ✅" : "отклонена ❌") + "\n";
                sendOptionsMessage(clientReservation.getChatID(), new HashMap<>(), answer + clientReservation, false);
                replaceText = "Эта запись " + (param[0].equals("accept") || param[0].equals("/accept") ? "уже просмотрена и принята вами✅" : "была вами отклонена❌") + "\n" + clientReservation;
                //todo catch proper exeptions to send replaceText
                if (clientReservation.isAccepted())
                    reservationRepository.acceptReservationById(reservationId);
                else 
                    reservationRepository.rejectReservationById(reservationId); 

            }
            case "/all_reservations" -> {
                List<Reservation> reservations = reservationRepository.findAllByDateTimeIsAfterAndDeniedIsFalseAndRejectedIsFalseOrderByDateTime(LocalDateTime.now());
                replaceText = reservations.size() < 1 ? "Пока записей нет" : "Все записи: ";
                sendReservationsTable(reservations);
            }
            case "/closest" -> {
                sendMasterNotification("Ближайшая запись\n", reservationRepository.findFirstByDateTimeIsAfterAndDeniedIsFalseAndRejectedIsFalseOrderByDateTime(LocalDateTime.now()));
            }
            case "/help" -> {
                sendHelpText(config.getMastersChatId(), MASTERS_HELP_TEXT);
            }
        }
        return replaceText;
    }

    public void sendReservationsTable(List<Reservation> reservations) {
        SendMessage message = new SendMessage();
        message.setChatId(config.getMastersChatId());
        message.enableMarkdown(true);
        // message.enableHtml(true);
        StringBuilder table = new StringBuilder();
        
        for (Reservation reservation : reservations) {
            table.append("Дата: ").append(Utils.reformatDateTimeString(reservation.getDateTime())).append("\n");
            table.append("ID: `").append(reservation.getID()).append("`, ");
            table.append("Услуга: ").append(reservation.getServiceType()).append(", ");
            table.append("Цена: ").append(reservation.getCost()).append("Р, ");
            table.append("Принята: ").append(reservation.isAccepted() ? "Да" : "Нет").append("\n\n");
        }
        message.setText(table.toString());
        try {
            execute(message);
            log.info("Reply sent");
        } catch (TelegramApiException e){
            log.error(e.getMessage());
        }
    }

    private String botAnswerUtils(String receivedMessage, long chatId, String userName, Integer messageId) {
        String[] param = receivedMessage.split(":");
        String replaceText = "";
        String param2 = "";
        if (param.length > 1)
            param2 = param[1];
        switch (param[0]) {
            case "/start" -> {
                sendLocalPhoto(chatId, "src/main/java/com/project/ReservationTGBot/images/drew-beamer-3SIXZisims4-unsplash.jpg", "Добро пожаловать в наш сервис для записи!");
                sendServiceMessage(chatId, userName, "Буду рад вам помочь, " + userName + "! Какой вид услуги вас интересует?", "0");
                updateDB(chatId, userName);
                
                //todo send photo https://www.youtube.com/watch?v=z8ClLrofvI8&themeRefresh=1
                // caption is for text, buttons can be added too
                reservationsInProgress.remove(chatId);
            }
            case "/again", "again" -> {
                sendServiceMessage(chatId, userName, "Какой вид услуги вас интересует?", "0");
                reservationsInProgress.remove(chatId);
                replaceText = "Новая запись";
            }
            case "service" -> {
                Optional<Service> query = serviceRepository.findById(param2);
                log.info(String.valueOf(query));
                if (query.isEmpty()) {
                    sendMenuMessage(chatId, "Упс, что-то пошло не так, попробуйте выбрать другую услугу");
                    break;
                }
                Service service = query.get();
                if (service.isHasChildType()) {
                    sendServiceMessage(chatId, userName, "Выберите подтип", service.getCode());
                    break;
                }
                reservationsInProgress.put(chatId, new Reservation(chatId, service.getName(), service.getCost(), service.getCode()));
                replaceText = "Вы выбрали: " + service.getName() + service.getEmoji();
                HashMap<String, String> options = new HashMap<>();
                options.put("empty", "Без описания");
                sendOptionsMessage(chatId, options, "По желанию оставьте описание услуги, можете прикрепить фотографию пример \uD83D\uDCF8", true);
            }
            case "empty", "weeks-again" -> {
                Reservation currentRes = reservationsInProgress.get(chatId);
                sendDatesMessage(chatId, "weeks", currentRes.getServiceCode());
                if (param[0].equals("empty")) {
                    currentRes.setDescriptionMessageID(-1);
                    reservationsInProgress.put(chatId, currentRes);
                    replaceText = "Без описания";
                } else {
                    replaceText = "◀️ К выбору недель";
                }
                //todo
            }
            case "this-week" -> {

                sendDatesMessage(chatId, "days-this", reservationsInProgress.get(chatId).getServiceCode());
                replaceText = "На ближайшие семь дней";
            }
            case "next-week" -> {
                sendDatesMessage(chatId, "days-next", reservationsInProgress.get(chatId).getServiceCode());
                replaceText = "Через неделю";
            }
            case "date", "date-today" -> {
                Reservation currentRes = reservationsInProgress.get(chatId);
                currentRes.setDate(LocalDate.parse(param2));
                if (param[0].equals("date-today")) {
                    sendTimeMessage(chatId, "time-today", currentRes.getServiceCode(), currentRes.getDate());
                    replaceText = "Вы выбрали сегодняшний день";
                }
                else {
                    sendTimeMessage(chatId, "time", currentRes.getServiceCode(), currentRes.getDate());
                    replaceText = "Вы выбрали дату: " + reformatDateString(currentRes.getDate());
                }
                replaceText = "Вы выбрали дату: " + reformatDateString(currentRes.getDate());
            }
            case "time" -> {
                HashMap<String, String> options = new HashMap<>();
                options.put("save", "Записаться");
                options.put("again", "Отменить запись");
                Reservation currentRes = reservationsInProgress.get(chatId);
                currentRes.setDateTime(currentRes.getDate().atTime(Integer.parseInt(param2), 0));
                reservationsInProgress.put(chatId, currentRes);
                if (reservationRepository.existsReservationByChatIDAndDateTimeAndDeniedIsFalseAndRejectedIsFalse(chatId, currentRes.getDateTime())) {
                    replaceText = "⚠️Внимание, у вас уже есть запись на эту дату и время,\n Вы уверены в своем выборе?\n";
                }
                sendOptionsMessage(chatId, options, "Подтвердите запись\n" + currentRes.toString(), true);
                replaceText += "Время записи: " + param2 + ":00";
            }
            case "save" -> {
                Reservation reservation = reservationsInProgress.get(chatId);
                reservation.setCreationDateTime(LocalDateTime.now());
                reservation.setUser(userRepository.findById(chatId).orElse(new User()));
                reservationRepository.save(reservation);
                sendMenuMessage(chatId, "Запись сохранена ✅");
                sendLocalPhoto(chatId, "src/main/java/com/project/ReservationTGBot/images/morvanic-lee-GiUJ02Yj_io-unsplash.jpg");
                sendMasterNotification("У вас новая запись!\n", reservation);
                reservationsInProgress.remove(chatId);
            }
            case "/all_reservations" -> {
                List<Reservation> allReservations = reservationRepository.findAllReservationsByChatId(chatId, LocalDateTime.now());
//                List<Reservation> allReservations = reservationRepository.findAllByChatID(chatId);
                for (Reservation reservation : allReservations) {
                    sendReservation(reservation);
                }
                String answer = allReservations.size() < 1 ? "У вас пока нет записей, вы можете создать ее прямо сейчас, выберите дальнейшее действие"
                        : "Вот все ваши записи, чем ещё я могу помочь?";
                sendMenuMessage(chatId, answer);

            }
            case "/closest" -> {
                Reservation closestReservation = reservationRepository.findFirstByChatIDAndDateTimeIsAfterAndDeniedIsFalseAndRejectedIsFalseOrderByDateTime(chatId, LocalDateTime.now());
                log.info(String.valueOf(closestReservation));
                if (closestReservation != null) {
                    sendReservation(closestReservation);
                }
                sendMenuMessage(chatId, closestReservation == null ? "У вас пока нет записей, вы можете создать ее " +
                        "прямо сейчас, выберите дальнейшее действие" : reservationsInProgress.containsKey(chatId) ?
                        "У вас есть незавершённая запись, завершите её или создайте новую" : "Готовы сделать новую запись?" );
            }
            case "/help" -> {
                sendHelpText(chatId, HELP_TEXT);
                log.info("This chat needs help: " + chatId);
            }
            case "deny_reservation" -> {
                int reservationId;
                try {
                    reservationId = Integer.parseInt(param2);
                } catch (NumberFormatException e) {
                    log.error("В сообщении c отменой записи, не валидный ID записи: {}", param2);
                    return "Сбой соединения, запись не удалось отменить, попробуйте еще раз";

                }
                Reservation deniedReservation = reservationRepository.findByID(reservationId);
                sendMasterNotification("Клиент отменил запись ❌\n", deniedReservation);
                replaceText = "Запись отменена ❌";
                reservationRepository.denyReservationById(reservationId);
                log.info("reservation denied");
            }
            default -> {
            }
        }
        return replaceText;
    }

    private void sendOptionsMessage(long chatId, HashMap<String, String> options, String answer, boolean addMenu) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(answer);
        message.setReplyMarkup(Buttons.hashMapToInlineKeyboard(options, addMenu));
        try {
            execute(message);
            log.info("Reply sent");
        } catch (TelegramApiException e){
            log.error(e.getMessage());
        }
    }

    private void sendMasterNotification(String answer, Reservation reservation) {
        if (reservation.isDenied() || reservation.isRejected()) {
            SendMessage message = new SendMessage();
            message.setChatId(config.getMastersChatId());
            message.setText(answer);
            try {
                execute(message);
                log.info("Reply sent");
            } catch (TelegramApiException e){
                log.error(e.getMessage());
            }
            return;
        }
        if (reservation.getDescriptionMessageID() != -1) {
            try {
                execute(
                        ForwardMessage.builder()
                                .chatId(config.getMastersChatId())
                                .fromChatId(reservation.getChatID())
                                .messageId(reservation.getDescriptionMessageID())
                                .build()
                );
            } catch (TelegramApiException e){
                log.error(e.getMessage());
            }
        }
        HashMap<String, String> masterOptions;
        {
            masterOptions = new HashMap<>();
            masterOptions.put("reject:" + reservation.getID(), "Отклонить запись");
            masterOptions.put("accept:" + reservation.getID(), "Принять запись");
        }
        sendOptionsMessage(config.getMastersChatId(), masterOptions, answer + reservation, false);
    }

    private void sendMenuMessage(long chatID, String answer) {
        SendMessage message = new SendMessage();
        message.setChatId(chatID);
        message.setText(answer);
        message.setReplyMarkup(Buttons.inlineMarkupMenu());
        try {
            execute(message);
            log.info("Reply sent");
        } catch (TelegramApiException e){
            log.error(e.getMessage());
        }
    }

    private void sendServiceMessage(long chatId, String userName, String answer, String stage) {
        log.info("Stage:" + stage);
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(answer);
        message.setReplyMarkup(Buttons.inlineMarkupServices(serviceRepository.findAllServicesByStage(stage), true));

        try {
            execute(message);
            log.info("Reply sent");
        } catch (TelegramApiException e){
            log.error(e.getMessage());
        }
    }

    public void sendReservation(Reservation reservation) {
        SendMessage message = new SendMessage();
        message.setChatId(reservation.getChatID());
        message.setText(reservation.toString());
        message.setReplyMarkup(Buttons.inlineKeyboardReservationOptions(reservation.getID()));
        message.setDisableNotification(true);

        try {
            execute(message);
            log.info("Reservation " + reservation.getID() + " sent");
        } catch (TelegramApiException e){
            log.error(e.getMessage());
        }
    }

    private void sendDatesMessage(Long chatId, String type, String serviceCode) {
        HashMap<String, String> options = new LinkedHashMap<>();
        if (type.equals("weeks")) {
            options.put("this-week", "Записаться в ближайшие 7 дней");
            options.put("next-week", "Записаться через неделю");
//            options.put("__description_again", "На предыдущий этап");
            sendOptionsMessage(chatId, options,"На какой период вы хотите записаться?", true);
            return;
        }
        if (type.equals("days-this") || type.equals("days-next")) {
            LocalDate date = LocalDate.now();
            LocalDateTime dateTime = LocalDateTime.now();
            if (type.equals("days-next")) {
                date = date.plusDays(7);
                long reservationsCount =
                        reservationRepository.countReservationsByPeriod(
                                date.atStartOfDay(),
                                date.atTime(19, 0),
                                serviceCode
                        );
                log.warn(date.atStartOfDay() + " - " + date.atTime(19, 0) + " Reservations count at " + String.valueOf(reservationsCount));
                if (reservationsCount < 8) {
                    options.put("date:" + date, reformatDateString(date));
                }
            }
//            try {
            if (type.equals("days-this")) {
                int hoursLeft = 19 - dateTime.getHour();
                long reservationsCount =
                        reservationRepository.countReservationsByPeriod(
                                dateTime,
                                date.atTime(19, 0),
                                serviceCode
                        );
                log.warn(dateTime + " - " + date.atTime(19, 0) + " Reservations count for " + serviceCode + ": " + String.valueOf(reservationsCount));
                if (hoursLeft > 1 && reservationsCount < hoursLeft) 
                    options.put("date-today:" + date, reformatDateString(date));
            }
            for (int i = 1; i < 7; i++) {
                long reservationsCount =
                        reservationRepository.countReservationsByPeriod(
                                date.plusDays(i).atStartOfDay(),
                                date.plusDays(i).atTime(19, 0),
                                serviceCode
                        );
                log.info(date.plusDays(i).atStartOfDay() + " - " + date.plusDays(i).atTime(19, 0) +" Reservations count for " + serviceCode + ": " + reservationsCount);
                if (reservationsCount < 8) {
                    options.put("date:" + date.plusDays(i), reformatDateString(date.plusDays(i)));
                }
            }
            options.put("weeks-again", "◀️ Назад к выбору недель");
            //todo make no days for the week and send other week
            sendOptionsMessage(chatId, options, "Выберите день", true);
            return;
        }

    }

    private void sendTimeMessage(Long chatId, String type, String serviceCode, LocalDate reservationDate) {
        HashMap<String, String> options = new LinkedHashMap<>();
        int starter = 8;
        if (type.equals("time-today")) {
            int hour = LocalDateTime.now().getHour();
            if (hour > starter) starter = hour + 1;
        }
        for (int i = starter; i < 19; i++) {
            if (!reservationRepository.existsReservationByDateTimeAndServiceCodeAndDeniedIsFalseAndRejectedIsFalse(reservationDate.atTime(i, 0), serviceCode))
                options.put("time:" + i, i + ":00");
        }
        options.put("weeks-again", "◀️ Назад к выбору недель");
        //todo no time for today
        sendOptionsMessage(chatId, options, "Выберите желаемое время", true);
    }

    private void sendHelpText(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);

        try {
            execute(message);
            log.info("Reply sent");
        } catch (TelegramApiException e){
            log.error(e.getMessage());
        }
    }

    private void sendLocalPhoto(long chatId, String path) {
        sendLocalPhoto(chatId, path, "");
    }
    private void sendLocalPhoto(long chatId, String path, String text) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setCaption(text);
        sendPhoto.setDisableNotification(true);
        try {
            sendPhoto.setPhoto(Utils.getInputFile(path));
        } catch(Exception e) {
            log.error(e.getMessage());
        }
        try {
            execute(sendPhoto);
            log.info("Photo sent");
        } catch (TelegramApiException e){
            log.error(e.getMessage());
        }
    }

    private void hideKeyboard(Message message, String replaceText) {
        String text = message.getText();
        if (!replaceText.equals("")) {
            text = replaceText;
        }
        try {
            execute(EditMessageText.builder()
                    .chatId(message.getChatId())
                    .text(text)
                    .messageId(toIntExact(message.getMessageId()))
                    .build()
            );
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
