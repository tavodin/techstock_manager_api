package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.entities.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    @Query("""
            SELECT u.name
            FROM User u
            WHERE u.username = :username
            """)
    Optional<String> getNameByUsername(@Param("username") String username);
}
