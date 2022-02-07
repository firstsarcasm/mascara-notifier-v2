package org.mascara.notifier.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WorkTime {
	public static final LocalTime START_OF_WORK = LocalTime.of(10, 0);
	public static final LocalTime END_OF_WORK = LocalTime.of(22, 0);
}
