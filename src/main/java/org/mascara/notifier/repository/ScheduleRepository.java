package org.mascara.notifier.repository;

import org.mascara.notifier.entity.Schedule;
import org.mascara.notifier.entity.Subscriber;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
	@Cacheable("schedule")
	List<Schedule> findAllByStaffId(Integer staffId);

}
