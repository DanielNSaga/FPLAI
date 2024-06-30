package com.fploptimizer.fplai.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import com.fploptimizer.fplai.model.Transfer;

/**
 * Repository interface for Transfer entities.
 */
public interface TransferRepository extends JpaRepository<Transfer, Long> {
}

