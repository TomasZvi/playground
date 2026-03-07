package org.example.playground.service;

import lombok.RequiredArgsConstructor;
import org.example.playground.event.KidEnteredEvent;
import org.example.playground.model.VisitorEntry;
import org.example.playground.persistence.VisitorRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class VisitorService {

    private final VisitorRepository visitorRepository;

    @EventListener(KidEnteredEvent.class)
    public void onKidEntered(KidEnteredEvent event) {
        VisitorEntry entry = VisitorEntry.builder()
                .ticketNumber(event.ticketNumber())
                .siteId(event.siteId())
                .entryDate(LocalDate.now())
                .build();
        visitorRepository.save(entry);
    }

    public long getTotalVisitorsToday() {
        return visitorRepository.countUniqueVisitorsByDate(LocalDate.now());
    }
}
