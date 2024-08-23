package smartcast.abj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import smartcast.abj.entity.Card;

@Repository
public interface CardRepository extends JpaRepository<Card, String> {

    @Query(value = "select count(*) from cards c where is_deleted = false and c.user_id = ? and c.status != 'CLOSED'", nativeQuery = true)
    Long getExistCardCount(Long userId);

}
