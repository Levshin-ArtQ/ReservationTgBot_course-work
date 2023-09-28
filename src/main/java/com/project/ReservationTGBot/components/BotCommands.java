package com.project.ReservationTGBot.components;

import org.telegram.telegrambots.meta.api.objects.commands.BotCommand; // есть варианты импорта

import java.util.List;

public interface BotCommands {
    List<BotCommand> LIST_OF_COMMANDS = List.of(
            new BotCommand("/again ", " Новая запись"),
            new BotCommand("/help ", " Подсказка"),
            new BotCommand("/all_reservations ", " Все записи"),
            new BotCommand("/closest ", " Ближайшая запись")
    );
    String MASTERS_HELP_TEXT = "вы находитесь в чате мастеров бота по записи \nСюда приходят уведомления о новых записях, вы можете отклонить или принять данную запись нажав на кнопку под ней";

    String HELP_TEXT = "Данный бот поможет вам быстро и удобно записаться " +
            "в салон красоты в удобное для вас время. Просто выберите услугу из предложенных, " +
            "и бот подскажет, что делать дальше.\n" +
            "Если вы не можете найти сообщение с перечислением услуг салона," +
            "выберите /again или отправьте сообщение с этой командой. \n" +
            "В меню вам доступны следующие опции:\n\n" +
            "/again - Новая запись\n" +
            "/help - чтобы еще раз просмотреть эту инструкцию\n" +
            "/all_reservations - чтобы увидеть все ваши актуальные записи\n" +
            "/closest - чтобы посмотреть ближайшую запись";


}
