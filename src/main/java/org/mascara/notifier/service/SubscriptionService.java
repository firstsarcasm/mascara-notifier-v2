package org.mascara.notifier.service;

import org.mascara.notifier.bot.MascaraNotifierBot;

public interface SubscriptionService {
	boolean subscribe(Long telegramChatId, Integer staffId, MascaraNotifierBot mascaraNotifierBot);

	void unsubscribe(Long telegramChatId);
}
