package notifier.NotificationManager.database;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


public interface UserRepository extends CrudRepository<User, Long> {
    @Transactional
    @Modifying
    @Query("update tg_data t set t.msg_numb = t.msg_numb + 1 where t.id is not null and t.id = :id")
    void updateMsgNumberByUserId(@Param("id") long id);

    @Transactional
    @Modifying
    @Query("update tg_data t set t.msg_numb = t.msg_numb + 1 where t.id is not null and t.id = :id")
    void updateReservationSetByUserId(@Param("id") long id);

    // User findById(long chatID);

    
}
