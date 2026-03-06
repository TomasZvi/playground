package org.example.playground.service;

import lombok.RequiredArgsConstructor;
import org.example.playground.model.Kid;
import org.example.playground.persistence.KidRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KidService {

    private final KidRepository kidRepository;

    public Kid getKid(Long id) {
        return kidRepository.findById(id).orElse(null);
    }

    public Kid createKid(Kid kid) {
        if (kidRepository.existsByTicketNumber(kid.getTicketNumber())) {
            throw new RuntimeException("Ticket number already exists");
        }
        return kidRepository.save(kid);
    }

    public Kid updateKid(Kid updatedKid) {
        Kid existingKid = kidRepository.findByTicketNumber(updatedKid.getTicketNumber());
        if (existingKid != null && !existingKid.getId().equals(updatedKid.getId())) {
            throw new RuntimeException("Ticket number already exists");
        }
        return kidRepository.save(updatedKid);
    }

    public void deleteKidById(Long id) {
        kidRepository.deleteById(id);
    }
}
