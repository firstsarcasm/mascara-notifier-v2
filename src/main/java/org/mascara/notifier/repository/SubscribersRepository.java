package org.mascara.notifier.repository;

import org.mascara.notifier.entity.Subscriber;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscribersRepository extends JpaRepository<Subscriber, Long> {
	/**
	 * @return true if subscriptions was performed successfully
	 */
	default boolean subscribe(Long telegramChatId, Integer staffId) {
		if (hasAlreadySubscribed(telegramChatId)) {
			return false;
		}
		saveSubscriber(telegramChatId, staffId);
		return true;
	}

	private boolean hasAlreadySubscribed(Long telegramChatId) {
		return this.findById(telegramChatId).isPresent();
	}

	default void saveSubscriber(Long telegramChatId, Integer staffId) {
		Subscriber subscriber = new Subscriber(telegramChatId, staffId);
		this.save(subscriber);
	}

	@Override
	List<Subscriber> findAll();
}
