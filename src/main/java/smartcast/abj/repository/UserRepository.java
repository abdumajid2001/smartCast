package smartcast.abj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import smartcast.abj.entity.User;
import smartcast.abj.projection.UserDetailsProjection;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = "select new smartcast.abj.configuration.security.MyUserDetails(t.id,t.username,t.password,t.permissions) from User t where t.username = ?1")
    Optional<UserDetailsProjection> findByUsername(String username);

    boolean existsByUsername(String username);

}
