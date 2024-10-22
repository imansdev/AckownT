package com.imansdev.ackownt.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.imansdev.ackownt.model.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUserId(Long userId);
}
