package com.akmaljon.resourceservice.client;

import com.akmaljon.resourceservice.dto.SongMetadataDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class SongServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(SongServiceClient.class);

    private final RestTemplate restTemplate;
    private final String songServiceUrl;

    public SongServiceClient(RestTemplate restTemplate,
                             @Value("${song.service.url}") String songServiceUrl) {
        this.restTemplate = restTemplate;
        this.songServiceUrl = songServiceUrl;
    }

    public void saveSongMetadata(SongMetadataDto metadata) {
        String url = songServiceUrl + "/songs";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SongMetadataDto> request = new HttpEntity<>(metadata, headers);

        try {
            restTemplate.postForEntity(url, request, Void.class);
            logger.info("Successfully saved song metadata for resource ID: {}", metadata.getId());
        } catch (Exception e) {
            // Log the error but don't fail the resource upload
            // The song metadata can be added later if needed
            logger.error("Failed to save song metadata to Song Service for resource ID {}: {}",
                    metadata.getId(), e.getMessage());
        }
    }

    public void deleteSongMetadata(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        String idsParam = String.join(",", ids.stream().map(String::valueOf).toList());
        String url = songServiceUrl + "/songs?id=" + idsParam;

        try {
            restTemplate.delete(url);
            logger.info("Successfully deleted song metadata for resource IDs: {}", ids);
        } catch (Exception e) {
            // Log the error but don't fail the resource deletion
            logger.error("Failed to delete song metadata from Song Service for IDs {}: {}",
                    ids, e.getMessage());
        }
    }
}


