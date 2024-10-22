package com.imansdev.ackownt.repository;

import com.imansdev.ackownt.model.Transaction;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.transactionName = 'DEDUCTION' AND t.transactionDate = :date")
    Long findTotalDailyDeductions(Long userId, LocalDate date);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Transaction t WHERE t.user.id = :userId")
    void deleteByUserId(Long userId);
}
