package com.regtech.gateway.service;

import com.regtech.gateway.dto.IngestResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
public class IngestService {

    private static final Logger log = LoggerFactory.getLogger(IngestService.class);

    @Value("${aml.staging.directory:/tmp/aml-staging}")
    private String stagingDirectory;

    public IngestResponseDto stageFile(MultipartFile file, String tenantId) {
        if (file.isEmpty()) {
            throw new IllegalStateException("Cannot ingest an empty file.");
        }

        try {
            // 1. Calculate SHA-256 Checksum for Idempotency
            String checksum = calculateChecksum(file);
            log.info("Calculated checksum {} for tenant {}", checksum, tenantId);

            // 2. Setup isolated tenant directory
            Path tenantPath = Paths.get(stagingDirectory, tenantId);
            if (!Files.exists(tenantPath)) {
                Files.createDirectories(tenantPath);
            }

            // 3. Use the checksum as the filename to inherently prevent duplicates
            // If the file already exists, it means this exact payload was already uploaded.
            Path targetLocation = tenantPath.resolve(checksum + ".zip");
            if (Files.exists(targetLocation)) {
                log.warn("Idempotency trigger: File with checksum {} already exists for tenant {}", checksum, tenantId);
                return new IngestResponseDto(checksum, checksum, "DUPLICATE_IGNORED", file.getSize());
            }

            // 4. Stream bytes to disk (StandardCopyOption.REPLACE_EXISTING is safe here)
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            String trackingId = UUID.randomUUID().toString();
            log.info("Successfully staged file {} for tenant {}", trackingId, tenantId);

            return new IngestResponseDto(trackingId, checksum, "STAGED_SUCCESSFULLY", file.getSize());

        } catch (IOException | NoSuchAlgorithmException e) {
            log.error("Failed to stage file for tenant {}", tenantId, e);
            throw new IllegalStateException("System encountered an error while staging the file stream.");
        }
    }

    private String calculateChecksum(MultipartFile file) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream fis = file.getInputStream()) {
            byte[] byteArray = new byte[1024];
            int bytesCount;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        }

        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
