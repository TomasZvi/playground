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

    public int calculateTotalCapacity(PlaySite site) {
        if (site == null || site.getAttractions() == null) {
            return 0;
        }
        return site.getAttractions().stream()
                .mapToInt(a -> a.getAttractionType().getCapacity() * a.getQuantity())
                .sum();
    }

    public int calculateFreeSpace(PlaySite site) {
        if (site == null) {
            return 0;
        }
        int kidsOnSite = site.getKidsOnSite() != null ? site.getKidsOnSite().size() : 0;
        return calculateTotalCapacity(site) - kidsOnSite;
    }

    public boolean hasFreeSpace(PlaySite site) {
        return calculateFreeSpace(site) > 0;
    }

    public double getUtilization(Long id) {
        PlaySite site = getPlaySite(id);
        if (site == null) {
            throw new RuntimeException("PlaySite not found");
        }
        int capacity = calculateTotalCapacity(site);
        if (capacity == 0) {
            return 0.0;
        }
        int kidsOnSite = site.getKidsOnSite() != null ? site.getKidsOnSite().size() : 0;
        return (double) kidsOnSite / capacity * 100.0;
    }
}
