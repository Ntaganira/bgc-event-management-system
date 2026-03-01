package com.bgc.event.repository;

import com.bgc.event.entity.Attendance;
import com.bgc.event.entity.Event;
import com.bgc.event.entity.User;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByUserAndEvent(User user, Event event);

    boolean existsByUserAndEvent(User user, Event event);

    List<Attendance> findByEvent(Event event);

    List<Attendance> findByUser(User user);

    long countByEvent(Event event);

    /** Attendance per event — [eventTitle, count] */
    @Query("SELECT a.event.title, COUNT(a) FROM Attendance a GROUP BY a.event.title ORDER BY COUNT(a) DESC")
    List<Object[]> countPerEvent();

    /** QR vs CODE breakdown — [method, count] */
    @Query("SELECT a.method, COUNT(a) FROM Attendance a GROUP BY a.method")
    List<Object[]> countByMethod();

    /** Daily check-ins for last 7 days — [dayLabel, count] */
    @Query(value = """
            SELECT FORMATDATETIME(a.attendance_time,'EEE') AS day,
                   COUNT(*) AS cnt
            FROM attendance a
            WHERE a.attendance_time >= DATEADD('DAY', -6, CURRENT_DATE)
            GROUP BY FORMATDATETIME(a.attendance_time,'EEE'),
                     CAST(a.attendance_time AS DATE)
            ORDER BY CAST(a.attendance_time AS DATE) ASC
            """, nativeQuery = true)
    List<Object[]> dailyCheckInsLast7Days();

    /** Top 5 most active users — [fullName, count] */
    @Query("SELECT CONCAT(a.user.firstName,' ',a.user.lastName), COUNT(a) FROM Attendance a GROUP BY a.user.id, a.user.firstName, a.user.lastName ORDER BY COUNT(a) DESC LIMIT 5")
    List<Object[]> topActiveUsers();

    /** Recent attendance sorted by time */
    @EntityGraph(attributePaths = "user")
    @Query("SELECT a FROM Attendance a ORDER BY a.attendanceTime DESC LIMIT 5")
    List<Attendance> findTop5ByOrderByAttendanceTimeDesc();
}
