package org.example.playground.service;

import lombok.RequiredArgsConstructor;
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
    public void addKidToPlaySite(Long siteId, Long kidId) {
        PlaySite site = playSiteRepository.findById(siteId).orElseThrow();
        Kid kid = kidRepository.findById(kidId).orElseThrow();

        if (site.getKidsOnSite().stream().anyMatch(k -> k.getId().equals(kidId)) ||
            site.getKidsQueue().stream().anyMatch(k -> k.getId().equals(kidId))) {
            return;
        }

        if (PlaySiteUtils.hasFreeSpace(site)) {
            site.getKidsOnSite().add(kid);
        } else if (kid.isAcceptWaiting()) {
            site.getKidsQueue().add(kid);
        } else {
            throw new RuntimeException("Site is full and kid does not accept waiting");
        }
        playSiteRepository.save(site);
    }

    @Transactional
    public void removeKidFromPlaySite(Long siteId, Long kidId) {
        PlaySite site = playSiteRepository.findById(siteId).orElseThrow();
        site.getKidsOnSite().removeIf(k -> k.getId().equals(kidId));
        site.getKidsQueue().removeIf(k -> k.getId().equals(kidId));

        while (!site.getKidsQueue().isEmpty() && PlaySiteUtils.hasFreeSpace(site)) {
            Kid kidFromQueue = site.getKidsQueue().removeFirst();
            site.getKidsOnSite().add(kidFromQueue);
        }

        playSiteRepository.save(site);
    }
}
