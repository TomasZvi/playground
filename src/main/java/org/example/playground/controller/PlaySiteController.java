package org.example.playground.controller;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.example.playground.model.PlaySite;
import org.example.playground.service.SiteService;
import org.springframework.web.bind.annotation.*;

@RestController( "/playSites")
@RequiredArgsConstructor
public class PlaySiteController {

    private final SiteService siteService;

    @PostMapping
    public PlaySite createPlaySite(@Nullable PlaySite playSite) {
        if (playSite == null) {
            return siteService.createDefaultPlaySite();
        }
        return siteService.createPlaySite(playSite);
    }

    @GetMapping( "/{id}")
    public PlaySite getPlaySite(@PathVariable int id) {
        return siteService.getPlaySite(id);
    }

    @PutMapping
    public PlaySite updatePlaySite(@Nonnull PlaySite playSite) {
        return siteService.updatePlaySite(playSite);
    }

    @DeleteMapping( "/{id}")
    public void deletePlaySite(@PathVariable int id) {
        siteService.deletePlaySite(id);
    }

}
