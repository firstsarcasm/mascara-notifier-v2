package org.mascara.notifier.bot;

import org.mascara.notifier.constant.RelativeDay;

import java.util.List;
import java.util.Map;

public interface MessageSender {
    void onScheduleChanged(Long chatId, String newValue);

    void onDayChanged(List<Long> chatIds, Map<RelativeDay, String> dayToSchedule);
}