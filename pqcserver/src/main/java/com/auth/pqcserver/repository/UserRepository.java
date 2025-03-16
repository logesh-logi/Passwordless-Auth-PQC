package com.auth.pqcserver.repository;

import com.auth.pqcserver.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository for managing User entities.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their username.
     *
     * @param username The username of the user.
     * @return An Optional containing the user if found, otherwise empty.
     */
    Optional<User> findByUsername(String username);
}
