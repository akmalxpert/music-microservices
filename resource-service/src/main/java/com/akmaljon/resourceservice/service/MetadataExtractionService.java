package com.akmaljon.resourceservice.service;

import com.akmaljon.resourceservice.dto.SongMetadataDto;
import com.akmaljon.resourceservice.exception.InvalidAudioDataException;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
public class MetadataExtractionService {

    public SongMetadataDto extractMetadata(Long resourceId, byte[] audioData) {
        Metadata metadata = new Metadata();
        Mp3Parser parser = new Mp3Parser();
        ParseContext parseContext = new ParseContext();

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(audioData)) {
            parser.parse(inputStream, new DefaultHandler(), metadata, parseContext);

            // Extract metadata values without modification (as per requirements)
            String name = getMetadataValue(metadata, "dc:title", "title");
            String artist = getMetadataValue(metadata, "xmpDM:artist", "dc:creator");
            String album = getMetadataValue(metadata, "xmpDM:album");
            String year = extractYear(metadata);
            String durationSeconds = metadata.get("xmpDM:duration");

            // Only convert duration format from seconds to mm:ss (allowed modification)
            String duration = convertDurationToMinutesSeconds(durationSeconds);

            // Use extracted values as-is, provide defaults only if extraction fails
            return new SongMetadataDto(
                    resourceId,
                    name != null && !name.isEmpty() ? name : "Unknown",
                    artist != null && !artist.isEmpty() ? artist : "Unknown",
                    album != null && !album.isEmpty() ? album : "Unknown",
                    duration,
                    year != null && !year.isEmpty() ? year : "1900"
            );

        } catch (IOException | SAXException | TikaException e) {
            throw new InvalidAudioDataException("Failed to parse MP3 file", e);
        }
    }

    private String getMetadataValue(Metadata metadata, String... keys) {
        for (String key : keys) {
            String value = metadata.get(key);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private String extractYear(Metadata metadata) {
        // Try multiple year-related fields
        String year = getMetadataValue(metadata, "xmpDM:releaseDate", "xmpDM:year", "year");

        if (year != null && !year.isBlank()) {
            // Extract just the year if it's a full date format
            if (year.length() >= 4) {
                String yearPart = year.substring(0, 4);
                // Validate it's a number
                try {
                    Integer.parseInt(yearPart);
                    return yearPart;
                } catch (NumberFormatException e) {
                    return year; // Return original if parsing fails
                }
            }
            return year;
        }
        return null;
    }

    private String convertDurationToMinutesSeconds(String durationSeconds) {
        if (durationSeconds == null || durationSeconds.isBlank()) {
            return "00:00";
        }

        try {
            double seconds = Double.parseDouble(durationSeconds);
            int totalSeconds = (int) Math.round(seconds);
            int minutes = totalSeconds / 60;
            int remainingSeconds = totalSeconds % 60;
            return String.format("%02d:%02d", minutes, remainingSeconds);
        } catch (NumberFormatException e) {
            return "00:00";
        }
    }
}



