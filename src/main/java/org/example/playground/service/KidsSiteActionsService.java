package org.example.playground.service;

import lombok.RequiredArgsConstructor;
import org.example.playground.model.Kid;
import org.example.playground.model.PlaySite;
import org.example.playground.persistence.KidRepository;
import org.example.playground.persistence.PlaySiteRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KidsSiteActionsService {

    private final PlaySiteRepository playSiteRepository;
    private final KidRepository kidRepository;

    public void addKidToPlaySite(Long siteId, Long kidId) {
        PlaySite site = playSiteRepository.findById(siteId).orElseThrow();
        Kid kid = kidRepository.findById(kidId).orElseThrow();

        site.getKidsOnSite().add(kid);
        playSiteRepository.save(site);
    }

    public void removeKidFromPlaySite(Long siteId, Long kidId) {
        PlaySite site = playSiteRepository.findById(siteId).orElseThrow();
        site.getKidsOnSite().removeIf(k -> k.getId().equals(kidId));
        playSiteRepository.save(site);
    }
}
