package com.imansdev.ackownt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.imansdev.ackownt.model.Transactions;

public interface TransactionsRepository extends JpaRepository<Transactions, Long> {

}
