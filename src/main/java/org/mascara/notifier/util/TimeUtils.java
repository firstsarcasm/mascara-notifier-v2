package org.mascara.notifier.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TimeUtils {

	public static LocalDate getToday() {
		val clock = Clock.system(ZoneId.of("Europe/Moscow"));
		return LocalDate.now(clock);
	}

	public static LocalDate getTomorrow() {
		return getToday().plusDays(1);
	}

	public static LocalDate getDayAfterTomorrow() {
		return getTomorrow().plusDays(1);
	}

}
