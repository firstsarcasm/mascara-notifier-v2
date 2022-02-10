package org.mascara.notifier.service.impl;

import lombok.RequiredArgsConstructor;
import org.mascara.notifier.bot.MascaraNotifierBot;
import org.mascara.notifier.logging.LogEntryAndExit;
import org.mascara.notifier.repository.SubscribersRepository;
import org.mascara.notifier.service.SubscriptionService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

	private final SubscribersRepository subscribersRepository;

	@Override
	@LogEntryAndExit
	public boolean subscribe(Long telegramChatId, Integer staffId, MascaraNotifierBot mascaraNotifierBot) {
		return subscribersRepository.subscribe(telegramChatId, staffId);
	}

	@Override
	@LogEntryAndExit
	public void unsubscribe(Long telegramChatId) {
		subscribersRepository.deleteById(telegramChatId);
	}
}
