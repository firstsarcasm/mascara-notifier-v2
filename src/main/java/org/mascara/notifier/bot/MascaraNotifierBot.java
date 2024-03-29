package org.mascara.notifier.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mascara.notifier.constant.RelativeDay;
import org.mascara.notifier.logging.LogEntryAndExit;
import org.mascara.notifier.service.MascaraService;
import org.mascara.notifier.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Map;

import static org.mascara.notifier.util.TelegramUtils.extractChatId;
import static org.mascara.notifier.util.TelegramUtils.hasTextMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class MascaraNotifierBot extends AbstractMascaraNotifierBot {

	private static final String MESSAGE_PREFIX = "------------------\n";

	private final KeyboardMaker keyboardMaker;
	private final MascaraService mascaraService;
	private final SubscriptionService subscriptionService;

	@Value("${telegram.bot.name}")
	private String botName;

	@Value("${telegram.bot.token}")
	private String botToken;

	@Value("${mascara.employee-name}")
	private String employeeName;

	@Override
	public String getBotUsername() {
		return botName;
	}

	@Override
	public String getBotToken() {
		return botToken;
	}

	@Override
	public void onUpdateReceived(Update update) {
		var chatId = extractChatId(update);
		sendMainKeyboard(chatId);

		Integer staffId = mascaraService.getStaffId(employeeName);

		if (hasTextMessage(update)) {
			String messageText = update.getMessage().getText();
			if (messageText.startsWith(KeyboardMaker.SUBSCRIBE_BUTTON_TITLE)) {
				boolean subscribed = subscriptionService.subscribe(chatId, staffId, this);
				if (subscribed) {
					sendTextMessage(chatId, "Вы подписаны на обновления");
				} else {
					sendTextMessage(chatId, "Подписка уже оформлена");
				}
				return;
			}

			if (messageText.startsWith(KeyboardMaker.UNSUBSCRIBE_BUTTON_TITLE)) {
				subscriptionService.unsubscribe(chatId);
				sendTextMessage(chatId, "Вы отписаны от обновлений");
				return;
			}

			if (messageText.startsWith(KeyboardMaker.WORKING_DAYS_BUTTON_TITLE)) {
				String dates = mascaraService.getDatesFormatted(staffId);
				sendTextMessage(chatId, dates);
				return;
			}

			sendScheduleForThreeDays(chatId, staffId);
		}
	}

	@Override
	@LogEntryAndExit
	public void onScheduleChanged(Long chatId, String newValue) {
		sendTextMessage(chatId, MESSAGE_PREFIX + "Расписание изменилось, теперь оно такое\n" + newValue);
	}

	@Override
	@LogEntryAndExit
	public void onDayChanged(List<Long> chatIds, Map<RelativeDay, String> dayToSchedule) {
		chatIds.forEach(chatId -> {
			sendTextMessage(chatId, MESSAGE_PREFIX + "Начался новый день, расписание:\n");
			dayToSchedule.forEach((relativeDay, schedule) -> sendTextMessage(chatId, schedule));
		});
	}

	private void sendScheduleForThreeDays(Long chatId, Integer staffId) {
		mascaraService.getScheduleForTargetDays(staffId)
				.forEach((relativeDay, schedule) -> sendTextMessage(chatId, schedule));
	}

	private void sendMainKeyboard(Long chatId) {
		tryExecute(keyboardMaker.getKeyboardMessage(chatId));
	}
}
