package org.example.playground.service;

import lombok.RequiredArgsConstructor;
import org.example.playground.model.Kid;
import org.example.playground.model.PlaySite;
import org.example.playground.persistence.PlaySiteRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@RequiredArgsConstructor
@Service
public class SiteService {

    private final PlaySiteRepository playSiteRepository;

    public PlaySite createDefaultPlaySite() {
        PlaySite defaultPlaySite = PlaySite.builder().build();
        return playSiteRepository.save(defaultPlaySite);
    }

    public PlaySite createPlaySite(PlaySite playSite) {
        return playSiteRepository.save(playSite);
    }

    public PlaySite getPlaySite(Long id) {
        return playSiteRepository.findById(id).orElse(null);
    }

    public PlaySite updatePlaySite(PlaySite playSite) {
        return playSiteRepository.save(playSite);
    }

    public void deletePlaySite(Long id) {
        playSiteRepository.deleteById(id);
    }

    public void addKidToPlaySite(Long siteId, Long kidId) {
        PlaySite site = playSiteRepository.findById(siteId).orElseThrow();
        Kid kid = new Kid();
        kid.setId(kidId);
        site.getKidsOnSite().add(kid);
        playSiteRepository.save(site);
    }

    public void removeKidFromPlaySite(Long siteId, Long kidId) {
        PlaySite site = playSiteRepository.findById(siteId).orElseThrow();
        site.getKidsOnSite().removeIf(k -> k.getId().equals(kidId));
        playSiteRepository.save(site);
    }
}
