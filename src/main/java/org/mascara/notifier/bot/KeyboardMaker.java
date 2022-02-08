package org.mascara.notifier.bot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

@Component
public class KeyboardMaker {

	//todo to separate class or enum?
	public static final String SCHEDULE_BUTTON_TITLE = "Расписание";
	public static final String SUBSCRIBE_BUTTON_TITLE = "Подписаться";
	public static final String UNSUBSCRIBE_BUTTON_TITLE = "Отписаться";
	public static final String WORKING_DAYS_BUTTON_TITLE = "Рабочие дни";

	private static final String HEADER_TEXT = "------------------";

	private final SendMessage.SendMessageBuilder keyboardMessage = SendMessage.builder()
			.replyMarkup(createKeyboard())
			.text(HEADER_TEXT);

	public SendMessage getKeyboardMessage(Long chatId) {
		return keyboardMessage
				.chatId(chatId.toString())
				.build();
	}

	private ReplyKeyboardMarkup createKeyboard() {
		var row = new KeyboardRow();
		var row2 = new KeyboardRow();
		var row3 = new KeyboardRow();

		KeyboardButton scheduleButton = new KeyboardButton(SCHEDULE_BUTTON_TITLE);
		KeyboardButton subscribeButton = new KeyboardButton(SUBSCRIBE_BUTTON_TITLE);
		KeyboardButton unsubscribeButton = new KeyboardButton(UNSUBSCRIBE_BUTTON_TITLE);
		KeyboardButton workingDaysButton = new KeyboardButton(WORKING_DAYS_BUTTON_TITLE);

		row.add(scheduleButton);
		row2.add(subscribeButton);
		row2.add(unsubscribeButton);
		row3.add(workingDaysButton);

		List<KeyboardRow> keyboardRows = List.of(row, row2, row3);
		ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup(keyboardRows);
		keyboardMarkup.setResizeKeyboard(true);

		return keyboardMarkup;
	}

}
