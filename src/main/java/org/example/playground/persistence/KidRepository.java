package org.example.playground.persistence;

import org.example.playground.model.Kid;
import org.springframework.stereotype.Repository;

@Repository
public interface KidRepository {

    Kid save(Kid kid);

    Kid findById(int id);

    Kid update(Kid updatedKid);

    void deleteById(int id);

}
