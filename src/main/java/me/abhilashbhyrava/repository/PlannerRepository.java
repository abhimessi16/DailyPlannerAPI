package me.abhilashbhyrava.repository;

import me.abhilashbhyrava.model.Planner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PlannerRepository extends JpaRepository<Planner, Long> {
    Optional<Planner> findByUsername(String username);
}
