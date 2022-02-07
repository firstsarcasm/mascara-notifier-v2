package org.mascara.notifier.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mascara.notifier.constant.RelativeDay;
import org.mascara.notifier.service.MascaraService;
import org.mascara.notifier.service.SubscriptionService;
import org.mascara.notifier.util.TimeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

import static org.mascara.notifier.util.TelegramUtils.extractChatId;
import static org.mascara.notifier.util.TelegramUtils.hasTextMessage;

//todo if caching work
//todo сделать scheduler
//todo сделать разные подключения к базе в зависимости от профиля(heroku,dev,etc)
@Slf4j
@Service
@RequiredArgsConstructor
public class MascaraNotifierBot extends AbstractMascaraNotifierBot {

	private final KeyboardMaker keyboardMaker;
	private final MascaraService mascaraService;
	private final SubscriptionService subscriptionService;

	@Value("${telegram.bot.name}")
	private String botName;

	@Value("${telegram.bot.token}")
	private String botToken;

	@Override
	public String getBotUsername() {
		return botName;
	}

	@Override
	public String getBotToken() {
		return botToken;
	}

	//todo add integration retries ?
	//todo проверить порядок отправки сообщений
	//todo logs
	//todo добавить на начальном экране выбор салона и сотрудника
	@Override
	public void onUpdateReceived(Update update) {
		var chatId = extractChatId(update);
		sendMainKeyboard(chatId);

//		Integer staffId = service.getStaffId("Даша С");
		Integer staffId = mascaraService.getStaffId("Татьяна М");

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
	public void onScheduleChanged(Long chatId, String newValue) {
		//todo add//		"------------------"
		sendTextMessage(chatId, "Расписание изменилось, теперь оно такое\n" + newValue);
	}

	@Override
	public void onDayChanged(Long chatId, Map<RelativeDay, String> dayToSchedule) {
		//todo add//		"------------------"
		sendTextMessage(chatId, "Начался новый день, расписание:\n");
		dayToSchedule.forEach((relativeDay, schedule) -> sendTextMessage(chatId, schedule));
	}

	private void sendScheduleForThreeDays(Long chatId, Integer staffId) {
		//todo make with stream from days enum? Arrays.stream(RelativeDay.values()).forEach();
		String todaySchedule = mascaraService.getScheduleFormatted(staffId, "Сегодня:", TimeUtils.getToday());
		String tomorrowSchedule = mascaraService.getScheduleFormatted(staffId, "Завтра:", TimeUtils.getTomorrow());
		String dayAfterTomorrowSchedule = mascaraService.getScheduleFormatted(staffId, "Послезавтра:", TimeUtils.getDayAfterTomorrow());

		sendTextMessage(chatId, todaySchedule);
		sendTextMessage(chatId, tomorrowSchedule);
		sendTextMessage(chatId, dayAfterTomorrowSchedule);
	}

	private void sendMainKeyboard(Long chatId) {
		tryExecute(keyboardMaker.getKeyboardMessage(chatId));
	}
}
