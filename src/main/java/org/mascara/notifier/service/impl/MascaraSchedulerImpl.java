package org.mascara.notifier.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mascara.notifier.bot.MessageSender;
import org.mascara.notifier.constant.RelativeDay;
import org.mascara.notifier.entity.Schedule;
import org.mascara.notifier.repository.ScheduleRepository;
import org.mascara.notifier.repository.SubscribersRepository;
import org.mascara.notifier.service.MascaraScheduler;
import org.mascara.notifier.service.MascaraService;
import org.mascara.notifier.util.TimeUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class MascaraSchedulerImpl implements MascaraScheduler {

	private static final String DAY_DESCRIPTION_DELIMITER = ":";

	private final MascaraService service;
	private final MessageSender messageSender;
	private final ScheduleRepository scheduleRepository;
	private final SubscribersRepository subscribersRepository;

	private volatile LocalDate schedulerToday = TimeUtils.getToday();
//	private volatile LocalDate schedulerToday = TimeUtils.getToday().minusDays(1); // test value

	@Override
//	@Scheduled(fixedRate = 120_000)
	@Scheduled(fixedRate = 120_00) // test value
	public void checkSubscribersSchedule() {
		subscribersRepository.findAll().forEach(subscriber -> {
			Integer staffId = subscriber.getStaffId();
			Long chatId = subscriber.getChatId();
			LocalDate now = TimeUtils.getToday();

			Map<RelativeDay, String> dayToSchedule = getScheduleForTargetDays(staffId);

			List<Schedule> schedules = scheduleRepository.findAllByStaffId(staffId);
			if (isEmpty(schedules)) {
				initSchedules(staffId, chatId, dayToSchedule);
				return;
			}

			if (isDayChanged(now)) {
				changeDay(chatId, now, dayToSchedule, schedules);
				return;
			}

			schedules.stream()
					.map(dbSchedule -> updateIfScheduleChanged(dayToSchedule, dbSchedule))
					.filter(Objects::nonNull)
					.forEach(actualSchedule -> notifyThatShecduleChaned(chatId, actualSchedule));
		});
	}

	private boolean isDayChanged(LocalDate now) {
		return schedulerToday.isBefore(now);
	}

	private LinkedHashMap<RelativeDay, String> getScheduleForTargetDays(Integer staffId) {
		return Arrays.stream(RelativeDay.values()).collect(Collectors.toMap(
				Function.identity(),
				relativeDay -> getScheduleForTargetDay(staffId, relativeDay),
				(o1, o2) -> o1, LinkedHashMap::new
		));
	}

	private void changeDay(Long chatId, LocalDate now, Map<RelativeDay, String> dayToSchedule, List<Schedule> schedules) {
		messageSender.onDayChanged(chatId, dayToSchedule);
		schedulerToday = now;

		schedules.forEach(dbSchedule -> updateIfScheduleChanged(dayToSchedule, dbSchedule));
	}

	private void initSchedules(Integer staffId, Long chatId, Map<RelativeDay, String> dayToSchedule) {
		dayToSchedule.forEach((relativeDay, schedule) -> {
			saveSchedule(staffId, schedule, relativeDay);
			notifyThatShecduleChaned(chatId, schedule);
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

	private void notifyThatShecduleChaned(Long chatId, String actualSchedule) {
		messageSender.onScheduleChanged(chatId, actualSchedule);
	}

	private String getScheduleForTargetDay(Integer staffId, RelativeDay relativeDay) {
		String prefix = makePrefix(relativeDay);
		LocalDate targetDay = relativeDay.getDay().get();
		return service.getScheduleFormatted(staffId, prefix, targetDay);
	}

	private String makePrefix(RelativeDay relativeDay) {
		return relativeDay.getDescription() + DAY_DESCRIPTION_DELIMITER;
	}

	private void saveSchedule(Integer staffId, String todaySchedule, RelativeDay day) {
		scheduleRepository.save(Schedule.builder()
				.day(day)
				.staffId(staffId)
				.schedule(todaySchedule)
				.build());
	}
}
