package org.example.playground.service;

import lombok.RequiredArgsConstructor;
import org.example.playground.event.KidEnteredEvent;
import org.example.playground.exception.ResourceNotFoundException;
import org.example.playground.model.Kid;
import org.example.playground.model.PlaySite;
import org.example.playground.persistence.PlaySiteRepository;
import org.example.playground.utils.PlaySiteUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SiteService {

    private final PlaySiteRepository playSiteRepository;
    private final ApplicationEventPublisher eventPublisher;

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
        processQueue(playSite);
        return playSiteRepository.save(playSite);
    }

    public void processQueue(PlaySite site) {
        while (!site.getKidsQueue().isEmpty() && PlaySiteUtils.hasFreeSpace(site)) {
            Kid kidFromQueue = site.getKidsQueue().removeFirst();
            addKidToSite(site, kidFromQueue);
        }
    }

    public void addKidToSite(PlaySite site, Kid kid) {
        site.getKidsOnSite().add(kid);
        if (site.getId() != null) {
            eventPublisher.publishEvent(new KidEnteredEvent(kid.getTicketNumber(), site.getId()));
        }
    }

    public void deletePlaySite(Long id) {
        playSiteRepository.deleteById(id);
    }

    public double getUtilization(Long id) {
        PlaySite site = getPlaySite(id);
        if (site == null) {
            throw new ResourceNotFoundException("PlaySite not found");
        }
        int capacity = PlaySiteUtils.calculateTotalCapacity(site);
        if (capacity == 0) {
            return 0.0;
        }
        int kidsOnSite = site.getKidsOnSite() != null ? site.getKidsOnSite().size() : 0;
        return (double) kidsOnSite / capacity * 100.0;
    }
}
