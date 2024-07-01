package com.fploptimizer.fplai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.fploptimizer.fplai.model.Team;

/**
 * Repository interface for Team entities.
 */
public interface TeamRepository extends JpaRepository<Team, Long> {
}
