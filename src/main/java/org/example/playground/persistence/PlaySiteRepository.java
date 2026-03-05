package org.example.playground.persistence;

import org.example.playground.model.PlaySite;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaySiteRepository {

    PlaySite save(PlaySite playSite);
    PlaySite findById(int id);
    PlaySite update(PlaySite playSite);
    void deleteById(int id);

}
