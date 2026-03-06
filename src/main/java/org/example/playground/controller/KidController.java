package org.example.playground.controller;

import lombok.RequiredArgsConstructor;
import org.example.playground.model.Kid;
import org.example.playground.service.KidService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/kids")
@RequiredArgsConstructor
public class KidController {

    private final KidService kidService;

    @PostMapping
    public Kid createKid(@RequestBody Kid kid) {
        return kidService.createKid(kid);
    }

    @GetMapping( "/{ticketNumber}")
    public Kid getKid(@PathVariable String ticketNumber) {
        return kidService.getKid(ticketNumber);
    }

    @PutMapping
    public Kid updateKid(@RequestBody Kid updatedKid) {
        return kidService.updateKid(updatedKid);
    }

    @DeleteMapping("/{ticketNumber}")
    public void deleteKid(@PathVariable String ticketNumber) {
        kidService.deleteKid(ticketNumber);
    }

}
