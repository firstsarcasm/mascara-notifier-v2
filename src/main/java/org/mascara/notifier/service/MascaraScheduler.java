package org.mascara.notifier.service;

import org.springframework.scheduling.annotation.Scheduled;

public interface MascaraScheduler {
	@Scheduled(fixedRate = 120_000)
	void checkSubscribersSchedule();
}
