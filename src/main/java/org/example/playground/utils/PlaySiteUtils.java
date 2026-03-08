package org.example.playground.utils;

import org.example.playground.model.PlaySite;

public class PlaySiteUtils {

    private PlaySiteUtils() {
        // Utility class
    }

    public static int totalCapacity(PlaySite site) {
        if (site == null || site.getAttractions() == null) {
            return 0;
        }
        return site.getAttractions().stream()
                .mapToInt(a -> a.getAttractionType().getCapacity() * a.getQuantity())
                .sum();
    }

    public static int freeSpace(PlaySite site) {
        if (site == null) {
            return 0;
        }
        int kidsOnSite = site.getKidsOnSite() != null ? site.getKidsOnSite().size() : 0;
        return totalCapacity(site) - kidsOnSite;
    }

    public static boolean hasFreeSpace(PlaySite site) {
        return freeSpace(site) > 0;
    }
}
