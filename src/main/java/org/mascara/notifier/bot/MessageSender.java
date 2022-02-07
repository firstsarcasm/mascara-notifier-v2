package org.mascara.notifier.bot;

import org.mascara.notifier.constant.RelativeDay;

import java.util.Map;

public interface MessageSender {
    // todo dayDescription  default = "сегодня"
    void onScheduleChanged(Long chatId, String newValue);

    //todo custom schedule holder?
    void onDayChanged(Long chatId, Map<RelativeDay, String> dayToSchedule);
}