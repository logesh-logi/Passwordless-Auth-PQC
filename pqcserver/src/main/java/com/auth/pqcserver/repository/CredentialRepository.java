package com.auth.pqcserver.repository;

import com.auth.pqcserver.entity.Credential;
import com.auth.pqcserver.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository for managing Credential entities.
 */
public interface CredentialRepository extends JpaRepository<Credential, Long> {

    /**
     * Finds a credential associated with a given user.
     *
     * @param user The user whose credential is being searched.
     * @return An Optional containing the credential if found, otherwise empty.
     */
    Optional<Credential> findByUser(User user);

    /**
     * Checks if a credential exists for a given user.
     *
     * @param user The user to check for an existing credential.
     * @return True if a credential exists, false otherwise.
     */
    boolean existsByUser(User user);
}
