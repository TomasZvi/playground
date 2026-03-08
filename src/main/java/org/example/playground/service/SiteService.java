package org.example.playground.service;

import lombok.RequiredArgsConstructor;
import org.example.playground.event.KidEnteredEvent;
import org.example.playground.error.exception.KidWaitingException;
import org.example.playground.error.exception.ResourceNotFoundException;
import org.example.playground.model.Kid;
import org.example.playground.model.PlaySite;
import org.example.playground.persistence.KidRepository;
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
    private final KidRepository kidRepository;
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
        int freeSpace = PlaySiteUtils.totalCapacity(site) - site.getKidsOnSite().size();
        if (freeSpace > 0) {
            moveKidsFromQueToSite(site, freeSpace);
        } else if (freeSpace < 0) {
            moveKidsFromSiteToQue(site, -freeSpace);
        }
    }

    private void moveKidsFromSiteToQue(PlaySite site, int kidsToMove) {
        int actuallyMoving = Math.min(kidsToMove, site.getKidsOnSite().size());
        List<Kid> toQueue = new ArrayList<>();
        for (int i = 0; i < actuallyMoving; i++) {
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
        int actuallyMoving = Math.min(kidsToMove, site.getKidsQueue().size());
        for (int i = 0; i < actuallyMoving; i++) {
            addKidToSite(site, site.getKidsQueue().removeFirst());
        }
    }

    public void addKidToSite(PlaySite site, Kid kid) {
        site.getKidsOnSite().add(kid);
        if (site.getId() != null) {
            eventPublisher.publishEvent(new KidEnteredEvent(kid.getTicketNumber(), site.getId()));
        }
    }

    @Transactional
    public void addKidToPlaySite(Long siteId, String ticketNumber) {
        PlaySite site = getPlaySite(siteId);
        Kid kid = kidRepository.findById(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Kid with ticket number " + ticketNumber + " not found"));

        if (isKidAlreadyInAnySite(ticketNumber)) {
            // If already in THIS site, we can just return (idempotent)
            if (site.getKidsOnSite().stream().anyMatch(k -> k.getTicketNumber().equals(ticketNumber)) ||
                    site.getKidsQueue().stream().anyMatch(k -> k.getTicketNumber().equals(ticketNumber))) {
                return;
            }
            throw new KidWaitingException("Kid is already in another site or queue");
        }

        if (PlaySiteUtils.hasFreeSpace(site)) {
            addKidToSite(site, kid);
        } else if (kid.isAcceptWaiting()) {
            site.getKidsQueue().add(kid);
        } else {
            throw new KidWaitingException("Site is full and kid does not accept waiting");
        }
        playSiteRepository.save(site);
    }

    private boolean isKidAlreadyInAnySite(String ticketNumber) {
        return playSiteRepository.existsByKidsOnSiteTicketNumber(ticketNumber) ||
                playSiteRepository.existsByKidsQueueTicketNumber(ticketNumber);
    }

    @Transactional
    public void removeKidFromPlaySite(Long siteId, String ticketNumber) {
        PlaySite site = getPlaySite(siteId);
        site.getKidsOnSite().removeIf(k -> k.getTicketNumber().equals(ticketNumber));
        site.getKidsQueue().removeIf(k -> k.getTicketNumber().equals(ticketNumber));

        processQueue(site);

        playSiteRepository.save(site);
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
