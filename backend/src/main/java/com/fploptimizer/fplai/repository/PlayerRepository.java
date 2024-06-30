package com.fploptimizer.fplai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.fploptimizer.fplai.model.Player;

/**
 * Repository interface for Player entities.
 */
public interface PlayerRepository extends JpaRepository<Player, Long> {
}

