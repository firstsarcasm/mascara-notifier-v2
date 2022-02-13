package org.mascara.notifier.repository;

import org.mascara.notifier.entity.BookedTimeCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface BookedTimeCacheRepository extends JpaRepository<BookedTimeCache, Long> {

	BookedTimeCache findByStaffIdAndDate(Integer staffId, LocalDate date);
}
