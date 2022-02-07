package org.mascara.notifier.service;

import org.mascara.notifier.bot.MascaraNotifierBot;
import org.springframework.scheduling.annotation.Scheduled;

public interface MascaraScheduler {
	@Scheduled(fixedRate = 120_000)
	void checkSubscribersSchedule();
//	void subscribe(Long telegramChatId, Integer staffId, MascaraNotifierBot mascaraNotifierBot);

//	void unsubscribe(Long telegramChatId);
}
