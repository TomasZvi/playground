package org.example.playground.service;

import lombok.RequiredArgsConstructor;
import org.example.playground.model.Kid;
import org.example.playground.persistence.KidRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KidService {

    private final KidRepository kidRepository;

    public Kid getKid(int id) {
        return kidRepository.findById(id);
    }

    public Kid createKid(Kid kid) {
        return kidRepository.save(kid);
    }

    public Kid updateKid(Kid updatedKid) {
        return kidRepository.update(updatedKid);
    }

    public void deleteKidById(int id) {
        kidRepository.deleteById(id);
    }
}
