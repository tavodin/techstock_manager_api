package io.github.tavodin.techstock_manager.repositories;

import io.github.tavodin.techstock_manager.dto.MenuProjection;
import io.github.tavodin.techstock_manager.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    @Query("""
            SELECT u.name
            FROM User u
            WHERE u.username = :username
            """)
    Optional<String> getNameByUsername(@Param("username") String username);

    @Query("""
            SELECT new io.github.tavodin.techstock_manager.dto.MenuProjection(
                mi.name, mi.link, m.name
            )
            FROM Permission p
            JOIN p.menuItem mi
            JOIN mi.menu m
            WHERE p.name IN :permissions
            """)
    List<MenuProjection> getMenuItemByPermissions(@Param("permissions") Collection<String> permissions);
}
