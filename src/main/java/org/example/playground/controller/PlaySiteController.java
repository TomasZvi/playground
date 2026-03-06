package org.example.playground.controller;

import lombok.RequiredArgsConstructor;
import org.example.playground.model.PlaySite;
import org.example.playground.service.KidsSiteActionsService;
import org.example.playground.service.SiteService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/playSites")
@RequiredArgsConstructor
public class PlaySiteController {

    private final SiteService siteService;
    private final KidsSiteActionsService kidsSiteActionsService;

    @PostMapping
    public PlaySite createPlaySite(@RequestBody(required = false) PlaySite playSite) {
        if (playSite == null) {
            return siteService.createDefaultPlaySite();
        }
        return siteService.createPlaySite(playSite);
    }

    @GetMapping( "/{id}")
    public PlaySite getPlaySite(@PathVariable Long id) {
        return siteService.getPlaySite(id);
    }

    @PutMapping
    public PlaySite updatePlaySite(@RequestBody PlaySite playSite) {
        return siteService.updatePlaySite(playSite);
    }

    @DeleteMapping( "/{id}")
    public void deletePlaySite(@PathVariable Long id) {
        siteService.deletePlaySite(id);
    }

    @PostMapping("/{siteId}/kids/{ticketNumber}")
    public void addKidToPlaySite(@PathVariable Long siteId, @PathVariable String ticketNumber) {
        kidsSiteActionsService.addKidToPlaySite(siteId, ticketNumber);
    }

    @DeleteMapping("/{siteId}/kids/{ticketNumber}")
    public void removeKidFromPlaySite(@PathVariable Long siteId, @PathVariable String ticketNumber) {
        kidsSiteActionsService.removeKidFromPlaySite(siteId, ticketNumber);
    }

    @GetMapping("/{id}/utilization")
    public double getUtilization(@PathVariable Long id) {
        return siteService.getUtilization(id);
    }

}
