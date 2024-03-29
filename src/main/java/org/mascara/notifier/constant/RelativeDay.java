package org.mascara.notifier.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.mascara.notifier.util.TimeUtils;

import java.time.LocalDate;
import java.util.function.Supplier;

@Getter
@RequiredArgsConstructor
public enum RelativeDay {
	TODAY(1, "Сегодня", TimeUtils::getToday),
	TOMORROW(2, "Завтра", TimeUtils::getTomorrow),
	AFTER_TWO_DAYS(3, "Послезавтра", TimeUtils::getDayAfterTomorrow),
	;

	private final Integer dayId;
	private final String description;
	private final Supplier<LocalDate> day;
}
