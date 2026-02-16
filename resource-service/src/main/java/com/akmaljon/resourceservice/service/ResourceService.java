package com.akmaljon.resourceservice.service;

import com.akmaljon.resourceservice.client.SongServiceClient;
import com.akmaljon.resourceservice.dto.SongMetadataDto;
import com.akmaljon.resourceservice.entity.Resource;
import com.akmaljon.resourceservice.exception.InvalidAudioDataException;
import com.akmaljon.resourceservice.exception.InvalidCsvFormatException;
import com.akmaljon.resourceservice.exception.ResourceNotFoundException;
import com.akmaljon.resourceservice.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceService.class);
    private static final int MAX_CSV_LENGTH = 200;

    private final ResourceRepository resourceRepository;
    private final MetadataExtractionService metadataExtractionService;
    private final SongServiceClient songServiceClient;

    @Transactional
    public Map<String, Long> uploadResource(byte[] audioData) {
        validateAudioData(audioData);

        Resource resource = new Resource();
        resource.setData(audioData);
        Resource savedResource = resourceRepository.save(resource);

        try {
            SongMetadataDto metadata = metadataExtractionService.extractMetadata(savedResource.getId(), audioData);
            songServiceClient.saveSongMetadata(metadata);
        } catch (Exception e) {
            // Metadata extraction or saving to song service failed, but resource is saved
            logger.warn("Failed to process metadata for resource {}: {}", savedResource.getId(), e.getMessage());
        }

        var id = savedResource.getId();
        return Map.of("id", id);
    }

    public byte[] getResource(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
        return resource.getData();
    }

    @Transactional
    public Map<String, List<Long>> deleteResources(String csvIds) {
        List<Long> ids = parseCsvIds(csvIds);
        List<Long> deletedIds = new ArrayList<>();

        for (Long id : ids) {
            if (!resourceRepository.existsById(id)) {
                continue;
            }
            resourceRepository.deleteById(id);
            deletedIds.add(id);
        }

        if (!deletedIds.isEmpty()) {
            songServiceClient.deleteSongMetadata(deletedIds);
        }

        return Map.of("ids", deletedIds);
    }

    private void validateAudioData(byte[] audioData) {
        if (audioData == null || audioData.length == 0) {
            throw new InvalidAudioDataException("Audio data cannot be empty");
        }

        // Basic MP3 validation - check for MP3 header
        if (audioData.length < 3) {
            throw new InvalidAudioDataException("Invalid MP3 file format");
        }

        // Check for ID3v2 tag (starts with "ID3") or MPEG frame sync (0xFF 0xFB, 0xFF 0xFA, etc.)
        boolean hasId3Tag = audioData[0] == 'I' && audioData[1] == 'D' && audioData[2] == '3';
        boolean hasMpegSync = (audioData[0] & 0xFF) == 0xFF && (audioData[1] & 0xE0) == 0xE0;

        if (!hasId3Tag && !hasMpegSync) {
            throw new InvalidAudioDataException("Invalid MP3 file format");
        }
    }

    private List<Long> parseCsvIds(String csvIds) {
        if (csvIds == null || csvIds.isBlank()) {
            throw new InvalidCsvFormatException("ID parameter cannot be empty");
        }

        if (csvIds.length() > MAX_CSV_LENGTH) {
            throw new InvalidCsvFormatException("CSV string length must be less than 200 characters");
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
                    throw new InvalidCsvFormatException("Invalid CSV format: ID must be a positive number");
                }
                ids.add(id);
            } catch (NumberFormatException e) {
                throw new InvalidCsvFormatException("Invalid CSV format: ID must be a valid number");
            }
        }

        return ids;
    }
}



