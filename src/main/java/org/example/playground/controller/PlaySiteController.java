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

    @PostMapping("/{siteId}/kids/{kidId}")
    public void addKidToPlaySite(@PathVariable Long siteId, @PathVariable Long kidId) {
        kidsSiteActionsService.addKidToPlaySite(siteId, kidId);
    }

    @DeleteMapping("/{siteId}/kids/{kidId}")
    public void removeKidFromPlaySite(@PathVariable Long siteId, @PathVariable Long kidId) {
        kidsSiteActionsService.removeKidFromPlaySite(siteId, kidId);
    }

}
