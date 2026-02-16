package com.akmaljon.resourceservice.controller;

import com.akmaljon.resourceservice.service.ResourceService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping(consumes = "audio/mpeg")
    public ResponseEntity<Map<String, Long>> uploadResource(@RequestBody byte[] audioData) {
        var id = resourceService.uploadResource(audioData);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getResource(@PathVariable Long id) {
        byte[] audioData = resourceService.getResource(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("audio/mpeg"));

        return new ResponseEntity<>(audioData, headers, HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<Map<String, List<Long>>> deleteResources(@RequestParam("id") String csvIds) {
        var deletedIds = resourceService.deleteResources(csvIds);
        return ResponseEntity.ok(deletedIds);
    }
}


