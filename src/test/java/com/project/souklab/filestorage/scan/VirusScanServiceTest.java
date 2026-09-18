package com.project.souklab.filestorage.scan;

import com.project.souklab.filestorage.config.StorageProperties;
import com.project.souklab.filestorage.exception.VirusDetectedException;
import com.project.souklab.filestorage.exception.VirusScanException;
import com.project.souklab.filestorage.validation.ValidatedFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for VirusScanService verifying fail-open and fail-closed branching logic,
 * malware rejection, stream rebuffering, and bypass behavior when disabled.
 */
class VirusScanServiceTest {

    private FakeVirusScanner fakeScanner;
    private StorageProperties properties;
    private StorageProperties.VirusScanProperties scanProperties;
    private VirusScanService virusScanService;

    @BeforeEach
    void setUp() {
        properties = new StorageProperties();
        scanProperties = new StorageProperties.VirusScanProperties();
        scanProperties.setEnabled(true);
        scanProperties.setHost("localhost");
        scanProperties.setPort(3310);
        properties.setVirusScan(scanProperties);

        fakeScanner = new FakeVirusScanner();
        virusScanService = new VirusScanService(properties, fakeScanner);
    }

    /**
     * Creates a ValidatedFile container for testing.
     */
    private ValidatedFile createValidatedFile(byte[] data, String filename) {
        return new ValidatedFile(
                new ByteArrayInputStream(data),
                filename,
                "image/jpeg",
                data.length
        );
    }

    /**
     * Verifies clean scan returns refreshed ValidatedFile with identical content.
     */
    @Test
    @DisplayName("scan: returns refreshed ValidatedFile with unconsumed stream when scan is clean")
    void scan_whenClean_returnsRefreshedValidatedFile() throws IOException {
        byte[] payload = "Valid non-infected file content".getBytes(StandardCharsets.UTF_8);
        ValidatedFile input = createValidatedFile(payload, "photo.jpg");

        fakeScanner.setCannedResult(ScanResult.clean());

        ValidatedFile result = virusScanService.scan(input);

        assertThat(result).isNotNull();
        assertThat(result.sanitizedFilename()).isEqualTo("photo.jpg");
        assertThat(result.detectedMimeType()).isEqualTo("image/jpeg");
        assertThat(result.size()).isEqualTo(payload.length);
        assertThat(result.content().readAllBytes()).isEqualTo(payload);
        assertThat(fakeScanner.getScanCount()).isEqualTo(1);
    }

    /**
     * Verifies malware detection always throws VirusDetectedException even when fail-open is enabled.
     */
    @Test
    @DisplayName("scan: throws VirusDetectedException when virus found, even if failOpen is true")
    void scan_whenVirusFound_throwsVirusDetectedException_evenIfFailOpenTrue() {
        scanProperties.setFailOpen(true);
        byte[] payload = "malware payload".getBytes(StandardCharsets.UTF_8);
        ValidatedFile input = createValidatedFile(payload, "infected.jpg");

        fakeScanner.setCannedResult(ScanResult.infected("Eicar-Signature"));

        assertThatThrownBy(() -> virusScanService.scan(input))
                .isInstanceOf(VirusDetectedException.class)
                .hasMessageContaining("Eicar-Signature")
                .satisfies(ex -> assertThat(((VirusDetectedException) ex).getVirusName()).isEqualTo("Eicar-Signature"));
    }

    /**
     * Verifies malware detection throws VirusDetectedException when fail-open is false.
     */
    @Test
    @DisplayName("scan: throws VirusDetectedException when virus found and failOpen is false")
    void scan_whenVirusFound_throwsVirusDetectedException_whenFailOpenFalse() {
        scanProperties.setFailOpen(false);
        byte[] payload = "malware payload".getBytes(StandardCharsets.UTF_8);
        ValidatedFile input = createValidatedFile(payload, "trojan.pdf");

        fakeScanner.setCannedResult(ScanResult.infected("Win.Trojan.Generic"));

        assertThatThrownBy(() -> virusScanService.scan(input))
                .isInstanceOf(VirusDetectedException.class)
                .hasMessageContaining("Win.Trojan.Generic")
                .satisfies(ex -> assertThat(((VirusDetectedException) ex).getVirusName()).isEqualTo("Win.Trojan.Generic"));
    }

    /**
     * Verifies scanner error throws VirusScanException when failOpen is false (fail closed).
     */
    @Test
    @DisplayName("scan: throws VirusScanException on scanner error when failOpen is false (fail-closed)")
    void scan_whenScannerError_andFailClosed_throwsVirusScanException() {
        scanProperties.setFailOpen(false);
        byte[] payload = "some content".getBytes(StandardCharsets.UTF_8);
        ValidatedFile input = createValidatedFile(payload, "doc.pdf");

        fakeScanner.setCannedResult(ScanResult.error("Connection refused"));

        assertThatThrownBy(() -> virusScanService.scan(input))
                .isInstanceOf(VirusScanException.class)
                .hasMessageContaining("Connection refused");
    }

    /**
     * Verifies scanner error allows upload through when failOpen is true (fail open).
     */
    @Test
    @DisplayName("scan: allows upload through on scanner error when failOpen is true (fail-open)")
    void scan_whenScannerError_andFailOpen_proceedsWithoutException() throws IOException {
        scanProperties.setFailOpen(true);
        byte[] payload = "file bytes during outage".getBytes(StandardCharsets.UTF_8);
        ValidatedFile input = createValidatedFile(payload, "profile.jpg");

        fakeScanner.setCannedResult(ScanResult.error("Socket timeout"));

        ValidatedFile result = virusScanService.scan(input);

        assertThat(result).isNotNull();
        assertThat(result.content().readAllBytes()).isEqualTo(payload);
        assertThat(fakeScanner.getScanCount()).isEqualTo(1);
    }

    /**
     * Verifies scanning is completely bypassed when virus scanning is disabled.
     */
    @Test
    @DisplayName("scan: bypasses scanning entirely when storage.virus-scan.enabled is false")
    void scan_whenDisabled_bypassesScanEntirely() {
        scanProperties.setEnabled(false);
        byte[] payload = "test data".getBytes(StandardCharsets.UTF_8);
        ValidatedFile input = createValidatedFile(payload, "bypass.jpg");

        ValidatedFile result = virusScanService.scan(input);

        assertThat(result).isSameAs(input);
        assertThat(fakeScanner.getScanCount()).isEqualTo(0);
    }

    /**
     * Verifies scanBytes delegates to scanner and handles clean results.
     */
    @Test
    @DisplayName("scanBytes: executes scan on byte array")
    void scanBytes_whenClean_executesSuccessfully() {
        byte[] payload = "direct byte scan".getBytes(StandardCharsets.UTF_8);
        fakeScanner.setCannedResult(ScanResult.clean());

        virusScanService.scanBytes(payload, "file.png");

        assertThat(fakeScanner.getScanCount()).isEqualTo(1);
    }

    /**
     * Verifies scanStream delegates to scanner and handles clean results.
     */
    @Test
    @DisplayName("scanStream: executes scan on InputStream")
    void scanStream_whenClean_executesSuccessfully() {
        byte[] payload = "direct stream scan".getBytes(StandardCharsets.UTF_8);
        fakeScanner.setCannedResult(ScanResult.clean());

        virusScanService.scanStream(new ByteArrayInputStream(payload), "stream.png");

        assertThat(fakeScanner.getScanCount()).isEqualTo(1);
    }

    /**
     * Verifies null ValidatedFile argument throws IllegalArgumentException.
     */
    @Test
    @DisplayName("scan: throws IllegalArgumentException when file is null")
    void scan_nullFile_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> virusScanService.scan(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructorRejectsMissingDependencies() {
        assertThatThrownBy(() -> new VirusScanService(null, fakeScanner))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("StorageProperties cannot be null");
        assertThatThrownBy(() -> new VirusScanService(properties, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("VirusScanner cannot be null");
    }

    @Test
    void scanRejectsUnreadableContent() {
        InputStream unreadable = new InputStream() {
            @Override public int read() throws IOException { throw new IOException("read failed"); }
        };
        ValidatedFile input = new ValidatedFile(unreadable, "broken.bin", "application/octet-stream", 1);

        assertThatThrownBy(() -> virusScanService.scan(input))
                .isInstanceOf(VirusScanException.class)
                .hasMessageContaining("read failed");
    }

    @Test
    void nullScanPropertiesBypassesAllScanEntryPoints() {
        properties.setVirusScan(null);
        ValidatedFile input = createValidatedFile(new byte[]{1}, "file.bin");

        assertThat(virusScanService.scan(input)).isSameAs(input);
        virusScanService.scanBytes(new byte[]{1}, "file.bin");
        virusScanService.scanStream(new ByteArrayInputStream(new byte[]{1}), "file.bin");
        assertThat(fakeScanner.getScanCount()).isZero();
    }

    @Test
    void directEntryPointsRejectNullArguments() {
        assertThatThrownBy(() -> virusScanService.scanBytes(null, "file.bin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Byte array content cannot be null");
        assertThatThrownBy(() -> virusScanService.scanStream(null, "file.bin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Content stream cannot be null");
    }

    @Test
    void disabledDirectEntryPointsDoNotInvokeScanner() {
        scanProperties.setEnabled(false);

        virusScanService.scanBytes(new byte[]{1}, "file.bin");
        virusScanService.scanStream(new ByteArrayInputStream(new byte[]{1}), "file.bin");

        assertThat(fakeScanner.getScanCount()).isZero();
    }

    /**
     * Lightweight test double simulating a VirusScanner.
     */
    private static class FakeVirusScanner implements VirusScanner {
        private ScanResult cannedResult = ScanResult.clean();
        private int scanCount = 0;

        void setCannedResult(ScanResult cannedResult) {
            this.cannedResult = cannedResult;
        }

        int getScanCount() {
            return scanCount;
        }

        @Override
        public ScanResult scan(InputStream content) {
            scanCount++;
            return cannedResult;
        }
    }
}
