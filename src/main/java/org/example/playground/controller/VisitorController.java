package org.example.playground.controller;

import lombok.RequiredArgsConstructor;
import org.example.playground.service.VisitorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/visitors")
@RequiredArgsConstructor
public class VisitorController {

    private final VisitorService visitorService;

    @GetMapping("/count")
    public long getVisitorCount() {
        return visitorService.getTotalVisitorsToday();
    }
}
