package notifier.NotificationManager.database;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends CrudRepository<Reservation, Long> {

    @Transactional
    @Modifying
    @Query("update reservation_data r set r.rejected = true where r.ID is not null and r.ID = :ID")
    void rejectReservationById(@Param("ID") long ID);

    @Transactional
    @Modifying
    @Query("update reservation_data r set r.accepted = true where r.ID is not null and r.ID = :ID")
    void acceptReservationById(@Param("ID") long ID);

    @Transactional
    @Modifying
    @Query("update reservation_data r set r.denied = true where r.ID is not null and r.ID = :ID")
    void denyReservationById(@Param("ID") long ID);

    @Transactional
    @Modifying
    @Query("update reservation_data r set r.notified1 = true where r.ID = :ID")
    void setNotified1(@Param("ID") long ID);

    @Transactional
    @Modifying
    @Query("update reservation_data r set r.notified2 = true where r.ID = :ID")
    void setNotified2(@Param("ID") long ID);

    @Transactional
    @Modifying
    @Query("update reservation_data r set r.notified3 = true where r.ID = :ID")
    void setNotified3(@Param("ID") long ID);

    @Transactional
    @Modifying
    @Query("update reservation_data r set r.notified1 = true, r.notified2 = true, r.notified3 = true where r.ID = :ID")
    void setNotifiedAll(@Param("ID") long ID);

    Reservation findByID(int ID);
    Reservation findFirstByChatIDAndDateTimeIsAfterAndDeniedIsFalseAndRejectedIsFalseOrderByDateTime(Long chatID, LocalDateTime dateTime);

    boolean existsReservationByDateTimeAndServiceCodeAndDeniedIsFalseAndRejectedIsFalse(LocalDateTime checkDateTime, String serviceCode);
    boolean existsReservationByChatIDAndDateTimeAndDeniedIsFalseAndRejectedIsFalse(long chatID, LocalDateTime checkDateTime);

    String actualityQuery = "r.denied = false "
            + "and r.rejected = false ";
    @Query(value = "SELECT r FROM reservation_data r WHERE r.dateTime > :now and r.chatID = :chatID and " + actualityQuery + "ORDER BY r.dateTime")
    List<Reservation> findAllReservationsByChatId(@Param("chatID") Long chatID, @Param("now") LocalDateTime dateTime);

    @Query("SELECT COUNT(r) FROM reservation_data r WHERE r.dateTime > :now and r.dateTime < :dateEnd and r.serviceCode = :serviceCode and " + actualityQuery)
    long countReservationsByPeriod(@Param("now") LocalDateTime dateStart, @Param("dateEnd") LocalDateTime dateEnd, @Param("serviceCode") String serviceCode);

    @Query(value = "SELECT r FROM reservation_data r WHERE " +
            "r.notified1 = false " +
            "and r.dateTime >= :now " +
            "and r.dateTime <= :dateCheck " +
            "and " + actualityQuery)
    List<Reservation> findReservationsForNotification1(@Param("now") LocalDateTime now, @Param("dateCheck") LocalDateTime dateCheck);

    @Query(value = "SELECT r FROM reservation_data r WHERE " +
            "r.notified2 = false " +
            "and r.dateTime >= :now " +
            "and r.dateTime <= :dateCheck " +
            "and " + actualityQuery)
    List<Reservation> findReservationsForNotification2(@Param("now") LocalDateTime now, @Param("dateCheck") LocalDateTime dateCheck);

    @Query(value = "SELECT r FROM reservation_data r WHERE " +
            "r.notified3 = false " +
            "and r.dateTime >= :now " +
            "and r.dateTime <= :dateCheck " +
            "and " + actualityQuery)
    List<Reservation> findReservationsForNotification3(@Param("now") LocalDateTime now, @Param("dateCheck") LocalDateTime dateCheck);

}
