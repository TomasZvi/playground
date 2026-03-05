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
    public Kid createKid(Kid kid) {
        return kidService.createKid(kid);
    }

    @GetMapping( "/{id}")
    public Kid getKid(@PathVariable Long id) {
        return kidService.getKid(id);
    }

    @PutMapping
    public Kid updateKid(@RequestBody Kid updatedKid) {
        return kidService.updateKid(updatedKid);
    }

    @DeleteMapping("/{id}")
    public void deleteKid(@PathVariable Long id) {
        kidService.deleteKidById(id);
    }

}
