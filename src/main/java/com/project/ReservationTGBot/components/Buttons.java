package com.project.ReservationTGBot.components;

import com.project.ReservationTGBot.database.Service;
import com.project.ReservationTGBot.database.ServiceRepository;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class Buttons {
    private static final InlineKeyboardButton START_BUTTON = new InlineKeyboardButton("Новая запись ➕");
    private static final InlineKeyboardButton HELP_BUTTON = new InlineKeyboardButton("Подсказка \uD83D\uDCD5");
    private static final InlineKeyboardButton ALL_RESERVATIONS_BUTTON = new InlineKeyboardButton("Все записи \uD83D\uDCC6");
    private static final InlineKeyboardButton CLOSEST_BUTTON = new InlineKeyboardButton("Ближайшая запись ⏰");
    private static final InlineKeyboardButton DENY_BUTTON = new InlineKeyboardButton("Отменить запись ❌");
    

    private static final InlineKeyboardButton NO_DESCRIPTION_BUTTON = new InlineKeyboardButton("Без описания");

    public static InlineKeyboardMarkup inlineMarkupMenu() {
        List<List<InlineKeyboardButton>> rowsInLine = menuBuilder();
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.setKeyboard(rowsInLine);

        return markupInline;
    }

    public static InlineKeyboardMarkup inlineMarkupServices(List<Service> services, boolean addMenu) {
        List<List<InlineKeyboardButton>> rowsInLineService = new ArrayList<>();
        System.out.println(services);
        for (Service option : services) {
            InlineKeyboardButton button = new InlineKeyboardButton(option.getName() + option.getEmoji());
            button.setCallbackData("service:" + option.getCode());
            List<InlineKeyboardButton> rowInline = List.of(button);
            rowsInLineService.add(rowInline);
        }
        return getInlineKeyboardMarkup(rowsInLineService, addMenu);
    }

    private static InlineKeyboardMarkup getInlineKeyboardMarkup(List<List<InlineKeyboardButton>> rowsInLineService, boolean addMenu) {
        List<List<InlineKeyboardButton>> resultRows = new ArrayList<>(rowsInLineService);
        if (addMenu) resultRows.addAll(menuBuilder());
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        assert false;
        markupInline.setKeyboard(resultRows);
        return markupInline;
    }

    public static InlineKeyboardMarkup inlineKeyboardReservationOptions(long reservationID) {
        DENY_BUTTON.setCallbackData("deny_reservation:" + reservationID);
        List<InlineKeyboardButton> rowInLine = List.of(DENY_BUTTON);
        List<List<InlineKeyboardButton>> rowsInLine = List.of(rowInLine);
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.setKeyboard(rowsInLine);
        return markupInline;
    }

    public static InlineKeyboardMarkup hashMapToInlineKeyboard(HashMap<String, String> options, boolean addMenu) {
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        options.forEach((command, message) -> {
            InlineKeyboardButton button = new InlineKeyboardButton(message);
            button.setCallbackData(command);
            List<InlineKeyboardButton> rowInline = List.of(button);
            rowsInLine.add(rowInline);
        });
        return getInlineKeyboardMarkup(rowsInLine, addMenu);
    }

    private static List<List<InlineKeyboardButton>> menuBuilder() {
        START_BUTTON.setCallbackData("/again");
        HELP_BUTTON.setCallbackData("/help");
        ALL_RESERVATIONS_BUTTON.setCallbackData("/all_reservations");
        CLOSEST_BUTTON.setCallbackData("/closest");
        List<InlineKeyboardButton> rowInlineMenu = List.of(START_BUTTON, HELP_BUTTON);
        List<InlineKeyboardButton> rowInlineSchedule = List.of(ALL_RESERVATIONS_BUTTON, CLOSEST_BUTTON);
        return List.of(rowInlineMenu, rowInlineSchedule);
    }
}
