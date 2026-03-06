package org.example.playground.service;

import lombok.RequiredArgsConstructor;
import org.example.playground.model.PlaySite;
import org.example.playground.persistence.PlaySiteRepository;
import org.springframework.stereotype.Service;

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
}
