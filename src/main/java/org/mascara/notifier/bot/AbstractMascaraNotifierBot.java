package org.mascara.notifier.bot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public abstract class AbstractMascaraNotifierBot extends TelegramLongPollingBot implements MessageSender {

	protected void tryExecute(SendMessage message) {
		try {
			execute(message);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	//todo @LogEntryAndExit
	protected void sendTextMessage(Long chatId, String text) {
		var message = SendMessage.builder()
				.text(text)
				.chatId(chatId.toString())
				.build();
		tryExecute(message);
	}

}
