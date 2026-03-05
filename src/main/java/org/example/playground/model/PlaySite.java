package org.example.playground.model;

import lombok.Builder;

import java.util.List;
import java.util.Queue;

@Builder
public record PlaySite(
        Integer id,
        List<AttractionConfiguration> attractions,
        List<Kid> kidsOnSite,
        Queue<Kid> kidsQueue
) {
}
