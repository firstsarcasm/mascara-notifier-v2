package org.mascara.notifier.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mascara.notifier.bot.MessageSender;
import org.mascara.notifier.constant.RelativeDay;
import org.mascara.notifier.entity.Schedule;
import org.mascara.notifier.entity.Subscriber;
import org.mascara.notifier.repository.ScheduleRepository;
import org.mascara.notifier.repository.SubscribersRepository;
import org.mascara.notifier.service.MascaraScheduler;
import org.mascara.notifier.service.MascaraService;
import org.mascara.notifier.util.TimeUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class MascaraSchedulerImpl implements MascaraScheduler {

	private final MascaraService service;
	private final MessageSender messageSender;
	private final ScheduleRepository scheduleRepository;
	private final SubscribersRepository subscribersRepository;

	private volatile LocalDate schedulerToday = TimeUtils.getToday();

	@Override
	@Scheduled(fixedRate = 120_000)
	public void checkSubscribersSchedule() {
		List<Subscriber> allSubscribers = subscribersRepository.findAll();
		groupByStaffId(allSubscribers).forEach((staffId, subscribers) -> {
			List<Long> chatIds = subscribers.stream().map(Subscriber::getChatId).collect(Collectors.toList());
			LocalDate now = TimeUtils.getToday();

			Map<RelativeDay, String> dayToSchedule = service.getScheduleForTargetDays(staffId);

			List<Schedule> schedules = scheduleRepository.findAllByStaffId(staffId);
			if (isEmpty(schedules)) {
				initSchedules(staffId, chatIds, dayToSchedule);
				return;
			}

			if (isDayChanged(now)) {
				changeDay(chatIds, now, dayToSchedule, schedules);
				return;
			}

			schedules.stream()
					.map(dbSchedule -> updateIfScheduleChanged(dayToSchedule, dbSchedule))
					.filter(Objects::nonNull)
					.forEach(actualSchedule -> notifyThatShceduleChaned(chatIds, actualSchedule));
		});
	}

	private Map<Integer, List<Subscriber>> groupByStaffId(List<Subscriber> allSubscribers) {
		return allSubscribers.stream().collect(Collectors.groupingBy(Subscriber::getStaffId));
	}

	private boolean isDayChanged(LocalDate now) {
		return schedulerToday.isBefore(now);
	}

	private void changeDay(List<Long> chatIds, LocalDate now, Map<RelativeDay, String> dayToSchedule, List<Schedule> schedules) {
		messageSender.onDayChanged(chatIds, dayToSchedule);
		schedulerToday = now;

		schedules.forEach(dbSchedule -> updateIfScheduleChanged(dayToSchedule, dbSchedule));
	}

	private void initSchedules(Integer staffId, List<Long> chatIds, Map<RelativeDay, String> dayToSchedule) {
		dayToSchedule.forEach((relativeDay, schedule) -> {
			saveSchedule(staffId, schedule, relativeDay);
			notifyThatShceduleChaned(chatIds, schedule);
		});
	}

	/**
	 * @return actualSchedule, if the schedule has changed, or null
	 */
	private String updateIfScheduleChanged(Map<RelativeDay, String> dayToSchedule, Schedule dbSchedule) {
		RelativeDay day = dbSchedule.getDay();
		String actualSchedule = dayToSchedule.get(day);
		if (!actualSchedule.equals(dbSchedule.getSchedule())) {
			log.info("The schedule was changed.\nMascara schedule: {}\n db schedule: {}", actualSchedule, dbSchedule);
			dbSchedule.setSchedule(actualSchedule);
			scheduleRepository.save(dbSchedule);
			return actualSchedule;
		}
		return null;
	}

	private void notifyThatShceduleChaned(List<Long> chatId, String actualSchedule) {
		chatId.forEach(id -> messageSender.onScheduleChanged(id, actualSchedule));
	}

	private void saveSchedule(Integer staffId, String todaySchedule, RelativeDay day) {
		scheduleRepository.save(Schedule.builder()
				.day(day)
				.staffId(staffId)
				.schedule(todaySchedule)
				.build());
	}
}
