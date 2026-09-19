package com.project.souklab.dao.analytics;

import com.project.souklab.analytics.AnalyticsEvent;
import com.project.souklab.model.analytics.ActivityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivityEventRepository extends JpaRepository<ActivityEvent, String> {
    long countByEventTimeBetween(LocalDateTime from, LocalDateTime to);
    long countByEventTypeAndEventTimeBetween(AnalyticsEvent.Type eventType, LocalDateTime from, LocalDateTime to);
    @Query("select count(distinct e.actorId) from ActivityEvent e where e.actorId is not null and e.eventTime >= :from and e.eventTime <= :to")
    long countDistinctActorsByEventTimeBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    @Query("select count(distinct e.actorId) from ActivityEvent e, User u where e.actorId = u.id and u.artisan is not null and e.actorId is not null and e.eventTime >= :from and e.eventTime <= :to")
    long countDistinctArtisanActorsByEventTimeBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    @Query("select count(distinct e.actorId) from ActivityEvent e, User u where e.actorId = u.id and u.artisan is not null and e.actorId is not null and e.eventType = :eventType and e.eventTime >= :from and e.eventTime <= :to")
    long countDistinctArtisanActorsByTypeAndEventTimeBetween(@Param("eventType") AnalyticsEvent.Type eventType,
                                                               @Param("from") LocalDateTime from,
                                                               @Param("to") LocalDateTime to);
    @Query("select count(distinct e.actorId) from ActivityEvent e, User u where e.actorId = u.id and u.client is not null and e.actorId is not null and e.eventTime >= :from and e.eventTime <= :to")
    long countDistinctClientActorsByEventTimeBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    @Query("select count(distinct e.actorId) from ActivityEvent e, User u where e.actorId = u.id and u.client is not null and e.actorId is not null and e.eventType = :eventType and e.eventTime >= :from and e.eventTime <= :to")
    long countDistinctClientActorsByTypeAndEventTimeBetween(@Param("eventType") AnalyticsEvent.Type eventType,
                                                              @Param("from") LocalDateTime from,
                                                              @Param("to") LocalDateTime to);
    @Query("select count(distinct e.actorId) from ActivityEvent e where e.actorId is not null and e.eventType = :eventType and e.eventTime >= :from and e.eventTime <= :to")
    long countDistinctActorsByTypeAndEventTimeBetween(@Param("eventType") AnalyticsEvent.Type eventType,
                                                       @Param("from") LocalDateTime from,
                                                       @Param("to") LocalDateTime to);
    List<ActivityEvent> findByEventTypeAndEventTimeBetweenOrderByEventTimeAsc(AnalyticsEvent.Type eventType,
                                                                                LocalDateTime from,
                                                                                LocalDateTime to);
    List<ActivityEvent> findByEventTimeBeforeOrderByEventTimeAsc(LocalDateTime before, Pageable pageable);
    List<ActivityEvent> findByEventTimeBetweenOrderByEventTimeAsc(LocalDateTime from, LocalDateTime to);
    List<ActivityEvent> findByEventTimeBetweenOrderByEventTimeAsc(LocalDateTime from, LocalDateTime to, Pageable pageable);
}
