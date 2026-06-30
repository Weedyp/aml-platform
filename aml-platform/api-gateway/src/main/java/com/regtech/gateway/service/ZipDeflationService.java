package com.regtech.gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ZipDeflationService {

    private static final Logger log = LoggerFactory.getLogger(ZipDeflationService.class);

    // Enterprise Safety Limits
    private static final int MAX_ENTRIES = 10;
    private static final long MAX_UNCOMPRESSED_BYTES = 100 * 1024 * 1024; // 100 MB

    private final CsvValidationService csvValidationService;

    public ZipDeflationService(CsvValidationService csvValidationService) {
        this.csvValidationService = csvValidationService;
    }

    @Async
    public void securelyExtract(Path zipFilePath, Path targetDirectory) {
        log.info("Starting secure async extraction for: {}", zipFilePath.getFileName());

        int entryCount = 0;
        long totalBytesExtracted = 0;
        Path extractedCsvPath = null;

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath.toFile()))) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                entryCount++;
                if (entryCount > MAX_ENTRIES) {
                    throw new SecurityException("Zip bomb detected: Exceeded maximum file entries (" + MAX_ENTRIES + ")");
                }

                // Prevent directory traversal attacks (e.g., file named "../../windows/system32/bad.txt")
                Path resolvedPath = targetDirectory.resolve(entry.getName()).normalize();
                if (!resolvedPath.startsWith(targetDirectory.normalize())) {
                    throw new SecurityException("Path traversal attempt detected in zip entry: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(resolvedPath);
                } else {
                    Files.createDirectories(resolvedPath.getParent());
                    totalBytesExtracted += extractFileWithLimit(zis, resolvedPath, totalBytesExtracted);
                    if (entry.getName().toLowerCase().endsWith(".csv")) {
                        extractedCsvPath = resolvedPath;
                    }
                }
                zis.closeEntry();
            }
            log.info("Extraction complete. Safely extracted {} files. Total size: {} bytes", entryCount, totalBytesExtracted);
            if (extractedCsvPath != null) {
                // Trigger the validation engine for the extracted CSV file
                csvValidationService.processAndValidate(extractedCsvPath, "bank_alpha_customer_v1.json");
            } else {
                log.warn("No CSV file found in extracted ZIP archive: {}", zipFilePath.getFileName());
            }
        } catch (Exception e) {
            log.error("Halting extraction! Security or IO exception encountered: {}", e.getMessage());
            // In a real system, you would update the database here to mark this batch as 'FAILED_SECURITY_CHECK'
        }
    }

    private long extractFileWithLimit(ZipInputStream zis, Path targetFile, long currentTotalBytes) throws IOException {
        long bytesReadForThisFile = 0;
        byte[] buffer = new byte[8192];

        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(targetFile.toFile()))) {
            int read;
            while ((read = zis.read(buffer)) != -1) {
                bos.write(buffer, 0, read);
                bytesReadForThisFile += read;

                if ((currentTotalBytes + bytesReadForThisFile) > MAX_UNCOMPRESSED_BYTES) {
                    throw new SecurityException("Zip bomb detected: Exceeded maximum uncompressed size of 100MB");
                }
            }
        }
        return bytesReadForThisFile;
    }
}
