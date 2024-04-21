package com.example.demo.controller;

import com.example.demo.entity.Confirm;
import com.example.demo.service.ObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/confirms")
public class ConfirmController {
    @Autowired
    private ObjectService service;

    @GetMapping("/{id}")
    public ResponseEntity<Confirm> getConfirm(@PathVariable Integer id) {
        Confirm confirm = service.getObjectById(id);
        return confirm != null ? ResponseEntity.ok(confirm) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<Confirm> listConfirms() {
        return service.listAllObjects();
    }

    @PostMapping
    public Confirm createConfirm(@RequestBody Confirm confirm) {
        return service.createObject(confirm);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Optional<Optional<?>>> updateConfirm(@PathVariable Integer id, @RequestBody Confirm confirm, @RequestHeader("username") String username) {
        Optional<Optional<Optional<?>>> updatedConfirm = Optional.ofNullable(service.updateObject(id, (Map<String, String>) confirm, username));
        return updatedConfirm.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(403).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConfirm(@PathVariable Integer id, @RequestHeader("username") String username) {
        boolean deleted = service.deleteObject(id, username);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.status(403).build();
    }

    @PutMapping("/replace/{id}")
    public ResponseEntity<Optional<Confirm>> replaceConfirm(@PathVariable Integer id, @RequestBody Confirm confirm, @RequestHeader("username") String username) {
        Optional<Optional<Confirm>> replacedConfirm = Optional.ofNullable(service.replaceObject(id, confirm, username));
        return replacedConfirm.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(403).build());
    }
}
