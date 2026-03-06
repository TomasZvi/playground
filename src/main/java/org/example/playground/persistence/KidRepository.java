package org.example.playground.persistence;

import org.example.playground.model.Kid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KidRepository extends JpaRepository<Kid, Long> {

    Kid findByTicketNumber(String ticketNumber);

    Boolean existsByTicketNumber(String ticketNumber);

}
