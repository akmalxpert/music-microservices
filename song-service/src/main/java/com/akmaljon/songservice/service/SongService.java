package com.akmaljon.songservice.service;

import com.akmaljon.songservice.dto.SongDto;
import com.akmaljon.songservice.entity.Song;
import com.akmaljon.songservice.exception.InvalidCsvFormatException;
import com.akmaljon.songservice.exception.SongAlreadyExistsException;
import com.akmaljon.songservice.exception.SongNotFoundException;
import com.akmaljon.songservice.repository.SongRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SongService {

    private static final Logger logger = LoggerFactory.getLogger(SongService.class);
    private static final int MAX_CSV_LENGTH = 200;

    private final SongRepository songRepository;

    public SongService(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    @Transactional
    public Map<String, Long> createSong(SongDto songDto) {
        if (songRepository.existsById(songDto.getId())) {
            throw new SongAlreadyExistsException(songDto.getId());
        }

        Song song = new Song(
                songDto.getId(),
                songDto.getName(),
                songDto.getArtist(),
                songDto.getAlbum(),
                songDto.getDuration(),
                songDto.getYear()
        );

        Song savedSong = songRepository.save(song);
        logger.info("Created song with ID: {}", savedSong.getId());
        return Map.of("id", savedSong.getId());
    }

    public SongDto getSong(String id) {
        Long songId = validateAndParseId(id);
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new SongNotFoundException(songId));

        return new SongDto(
                song.getId(),
                song.getName(),
                song.getArtist(),
                song.getAlbum(),
                song.getDuration(),
                song.getYear()
        );
    }

    @Transactional
    public Map<String, List<Long>> deleteSongs(String csvIds) {
        List<Long> ids = parseCsvIds(csvIds);
        List<Long> deletedIds = new ArrayList<>();

        for (Long id : ids) {
            if (!songRepository.existsById(id)) {
                continue;
            }
            songRepository.deleteById(id);
            deletedIds.add(id);
        }

        return Map.of("ids", deletedIds);
    }

    private Long validateAndParseId(String id) {
        try {
            Long parsedId = Long.parseLong(id);
            if (parsedId <= 0) {
                throw new InvalidCsvFormatException("Invalid value '" + id + "' for ID. Must be a positive integer");
            }
            return parsedId;
        } catch (NumberFormatException e) {
            throw new InvalidCsvFormatException("Invalid value '" + id + "' for ID. Must be a positive integer");
        }
    }

    private List<Long> parseCsvIds(String csvIds) {
        if (csvIds == null || csvIds.isBlank()) {
            throw new InvalidCsvFormatException("ID parameter cannot be empty");
        }

        if (csvIds.length() > MAX_CSV_LENGTH) {
            throw new InvalidCsvFormatException("CSV string is too long: received " + csvIds.length() + " characters, maximum allowed is " + MAX_CSV_LENGTH);
        }

        String[] parts = csvIds.split(",");
        List<Long> ids = new ArrayList<>();

        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                throw new InvalidCsvFormatException("Invalid CSV format: empty ID value");
            }

            try {
                Long id = Long.parseLong(trimmed);
                if (id <= 0) {
                    throw new InvalidCsvFormatException("Invalid ID format: '" + trimmed + "'. Only positive integers are allowed");
                }
                ids.add(id);
            } catch (NumberFormatException e) {
                throw new InvalidCsvFormatException("Invalid ID format: '" + trimmed + "'. Only positive integers are allowed");
            }
        }

        return ids;
    }
}

