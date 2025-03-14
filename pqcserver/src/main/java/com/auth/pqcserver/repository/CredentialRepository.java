package com.auth.pqcserver.repository;

import com.auth.pqcserver.entity.Credential;
import com.auth.pqcserver.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CredentialRepository extends JpaRepository<Credential, Long> {
    Optional<Credential> findByUser(User user);
    boolean existsByUser(User user);
}
