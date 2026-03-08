package org.example.playground.service;

import lombok.RequiredArgsConstructor;
import org.example.playground.event.KidEnteredEvent;
import org.example.playground.error.exception.ResourceNotFoundException;
import org.example.playground.model.Kid;
import org.example.playground.model.PlaySite;
import org.example.playground.persistence.PlaySiteRepository;
import org.example.playground.utils.PlaySiteUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
        return playSiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PlaySite with id " + id + " not found"));
    }

    @Transactional
    public PlaySite updatePlaySite(PlaySite playSite) {
        PlaySite existingSite = getPlaySite(playSite.getId());

        if (playSite.getAttractions() == null) {
            existingSite.getAttractions().clear();
        } else {
            existingSite.setAttractions(playSite.getAttractions());
        }

        processQueue(existingSite);
        return playSiteRepository.save(existingSite);
    }

    public void processQueue(PlaySite site) {
        int kidsToMove = PlaySiteUtils.totalCapacity(site) - site.getKidsOnSite().size();
        if (kidsToMove > 0) {
            moveKidsFromQueToSite(site, kidsToMove);
        } else if (kidsToMove < 0) {
            moveKidsFromSiteToQue(site, kidsToMove);
        }
    }

    private static void moveKidsFromSiteToQue(PlaySite site, int kidsToMove) {
        int toRemove = -kidsToMove;
        List<Kid> toQueue = new ArrayList<>();
        for (int i = 0; i < toRemove; i++) {
            Kid kid = site.getKidsOnSite().removeFirst();
            if (kid.isAcceptWaiting()) {
                toQueue.add(kid);
            }
        }
        if (!toQueue.isEmpty()) {
            site.getKidsQueue().addAll(0, toQueue);
        }
    }

    private void moveKidsFromQueToSite(PlaySite site, int kidsToMove) {
        for (int i = 0; i < kidsToMove; i++) {
            addKidToSite(site, site.getKidsQueue().removeFirst());
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
        int capacity = PlaySiteUtils.totalCapacity(site);
        if (capacity == 0) {
            return 0.0;
        }
        int kidsOnSite = site.getKidsOnSite() != null ? site.getKidsOnSite().size() : 0;
        return (double) kidsOnSite / capacity * 100.0;
    }
}
