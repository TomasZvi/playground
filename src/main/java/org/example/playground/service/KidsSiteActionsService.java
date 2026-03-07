package org.example.playground.service;

import lombok.RequiredArgsConstructor;
import org.example.playground.exception.KidWaitingException;
import org.example.playground.exception.ResourceNotFoundException;
import org.example.playground.model.Kid;
import org.example.playground.model.PlaySite;
import org.example.playground.persistence.KidRepository;
import org.example.playground.persistence.PlaySiteRepository;
import org.example.playground.utils.PlaySiteUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KidsSiteActionsService {

    private final PlaySiteRepository playSiteRepository;
    private final KidRepository kidRepository;
    private final SiteService siteService;

    @Transactional
    public void addKidToPlaySite(Long siteId, String ticketNumber) {
        PlaySite site = playSiteRepository.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("PlaySite not found"));
        Kid kid = kidRepository.findById(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Kid not found"));

        if (site.getKidsOnSite().stream().anyMatch(k -> k.getTicketNumber().equals(ticketNumber)) ||
                site.getKidsQueue().stream().anyMatch(k -> k.getTicketNumber().equals(ticketNumber))) {
            return;
        }

        if (PlaySiteUtils.hasFreeSpace(site)) {
            site.getKidsOnSite().add(kid);
        } else if (kid.isAcceptWaiting()) {
            site.getKidsQueue().add(kid);
        } else {
            throw new KidWaitingException("Site is full and kid does not accept waiting");
        }
        playSiteRepository.save(site);
    }

    @Transactional
    public void removeKidFromPlaySite(Long siteId, String ticketNumber) {
        PlaySite site = playSiteRepository.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("PlaySite not found"));
        site.getKidsOnSite().removeIf(k -> k.getTicketNumber().equals(ticketNumber));
        site.getKidsQueue().removeIf(k -> k.getTicketNumber().equals(ticketNumber));

        siteService.processQueue(site);

        playSiteRepository.save(site);
    }
}
