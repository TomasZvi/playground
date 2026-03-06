package org.example.playground.service;

import lombok.RequiredArgsConstructor;
import org.example.playground.model.Kid;
import org.example.playground.persistence.KidRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KidService {

    private final KidRepository kidRepository;

    public Kid getKid(String ticketNumber) {
        return kidRepository.findById(ticketNumber).orElse(null);
    }

    public Kid createKid(Kid kid) {
        if (kid.getTicketNumber() == null) {
            kid.setTicketNumber(UUID.randomUUID().toString());
        } else if (kidRepository.existsById(kid.getTicketNumber())) {
            throw new RuntimeException("Ticket number already exists");
        }
        return kidRepository.save(kid);
    }

    public Kid updateKid(Kid updatedKid) {
        if (updatedKid.getTicketNumber() == null || !kidRepository.existsById(updatedKid.getTicketNumber())) {
            throw new RuntimeException("Kid not found");
        }
        return kidRepository.save(updatedKid);
    }

    public void deleteKid(String ticketNumber) {
        kidRepository.deleteById(ticketNumber);
    }
}
