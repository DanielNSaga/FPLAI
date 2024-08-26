package com.fploptimizer.fplai.repository;

import com.fploptimizer.fplai.model.Fixture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for the `Fixture` entity.
 *
 * <p>This interface provides methods to perform CRUD operations on the `Fixture` entity,
 * leveraging the Spring Data JPA framework.</p>
 */
@Repository
public interface FixtureRepository extends JpaRepository<Fixture, String> {
}
