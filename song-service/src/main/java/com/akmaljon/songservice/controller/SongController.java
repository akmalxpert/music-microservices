package com.akmaljon.songservice.controller;

import com.akmaljon.songservice.dto.SongDto;
import com.akmaljon.songservice.service.SongService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/songs")
public class SongController {

    private final SongService songService;

    public SongController(SongService songService) {
        this.songService = songService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Long>> createSong(@Valid @RequestBody SongDto songDto) {
        Map<String, Long> id = songService.createSong(songDto);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongDto> getSong(@PathVariable String id) {
        SongDto songDto = songService.getSong(id);
        return ResponseEntity.ok(songDto);
    }

    @DeleteMapping
    public ResponseEntity<Map<String, List<Long>>> deleteSongs(@RequestParam("id") String csvIds) {
        Map<String, List<Long>> deletedIds = songService.deleteSongs(csvIds);
        return ResponseEntity.ok(deletedIds);
    }
}

