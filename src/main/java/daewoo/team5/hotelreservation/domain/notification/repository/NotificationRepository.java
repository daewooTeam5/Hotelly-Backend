package daewoo.team5.hotelreservation.domain.notification.repository;

import daewoo.team5.hotelreservation.domain.notification.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    @Query("""
            select n
            from Notification n
            left join n.user u
            where (u.id = :userId or u.id is null)
              and n.createdAt > (
                  select u2.createdAt from Users u2 where u2.id = :userId
              )
            order by n.createdAt desc
            """)
    Page<NotificationEntity> findByUserIdOrUserIdIsNullOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("""
            select count(n.id)
            from Notification n
            where (n.user.id = :userId or n.user.id is null)
              and n.createdAt > n.user.createdAt
            """)
    long countByUserId(Long userId);
}
