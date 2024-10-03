package com.imansdev.ackownt.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.imansdev.ackownt.model.Accounts;

public interface AccountsRepository extends JpaRepository<Accounts, Long> {
    Optional<Accounts> findByUserId(Long userId);
}
