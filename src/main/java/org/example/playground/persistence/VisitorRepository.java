package org.example.playground.persistence;

import org.example.playground.model.VisitorEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface VisitorRepository extends JpaRepository<VisitorEntry, Long> {
    @Query("SELECT COUNT(DISTINCT v.ticketNumber) FROM VisitorEntry v WHERE v.entryDate = :date")
    long countUniqueVisitorsByDate(@Param("date") LocalDate date);
}
