package com.project.souklab.filestorage.scan;

import com.project.souklab.filestorage.config.StorageProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for ClamdInstreamScanner verifying zINSTREAM protocol framing,
 * chunking, timeout behavior, and response parsing against an in-memory mock socket server.
 */
class ClamdInstreamScannerTest {

    /**
     * Builds test VirusScanProperties pointing to the specified port.
     */
    private StorageProperties.VirusScanProperties createProperties(int port, Duration connectTimeout, Duration readTimeout) {
        StorageProperties.VirusScanProperties props = new StorageProperties.VirusScanProperties();
        props.setEnabled(true);
        props.setHost("localhost");
        props.setPort(port);
        props.setConnectionTimeout(connectTimeout);
        props.setReadTimeout(readTimeout);
        props.setFailOpen(false);
        return props;
    }

    /**
     * Verifies clean response returns clean ScanResult and streams exact content.
     */
    @Test
    @DisplayName("scan: returns clean result and streams exact content when clamd responds OK")
    void scan_cleanResponse_returnsCleanResult() throws Exception {
        byte[] testData = "Clean document content for scanning".getBytes(StandardCharsets.UTF_8);

        try (MockClamServer server = new MockClamServer("stream: OK")) {
            server.awaitReady();
            StorageProperties.VirusScanProperties props = createProperties(server.getPort(), Duration.ofSeconds(2), Duration.ofSeconds(5));
            ClamdInstreamScanner scanner = new ClamdInstreamScanner(props);

            ScanResult result = scanner.scan(new ByteArrayInputStream(testData));

            assertThat(result.isClean()).isTrue();
            assertThat(result.isInfected()).isFalse();
            assertThat(result.isError()).isFalse();
            assertThat(result.virusName()).isNull();
            assertThat(server.getReceivedPayload()).isEqualTo(testData);
        }
    }

    /**
     * Verifies FOUND response returns infected ScanResult with parsed virus name.
     */
    @Test
    @DisplayName("scan: returns infected result with extracted virus name when clamd responds FOUND")
    void scan_virusFoundResponse_returnsInfectedResultWithVirusName() throws Exception {
        byte[] eicarData = "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*".getBytes(StandardCharsets.US_ASCII);

        try (MockClamServer server = new MockClamServer("stream: Eicar-Signature FOUND")) {
            server.awaitReady();
            StorageProperties.VirusScanProperties props = createProperties(server.getPort(), Duration.ofSeconds(2), Duration.ofSeconds(5));
            ClamdInstreamScanner scanner = new ClamdInstreamScanner(props);

            ScanResult result = scanner.scan(new ByteArrayInputStream(eicarData));

            assertThat(result.isInfected()).isTrue();
            assertThat(result.isClean()).isFalse();
            assertThat(result.isError()).isFalse();
            assertThat(result.virusName()).isEqualTo("Eicar-Signature");
            assertThat(result.message()).contains("Eicar-Signature");
        }
    }

    /**
     * Verifies alternative ClamAV malware signature names are parsed correctly.
     */
    @Test
    @DisplayName("scan: parses composite signature names like Win.Test.EICAR_HDB-1 correctly")
    void scan_compositeVirusName_extractsCorrectName() throws Exception {
        try (MockClamServer server = new MockClamServer("stream: Win.Test.EICAR_HDB-1 FOUND")) {
            server.awaitReady();
            StorageProperties.VirusScanProperties props = createProperties(server.getPort(), Duration.ofSeconds(2), Duration.ofSeconds(5));
            ClamdInstreamScanner scanner = new ClamdInstreamScanner(props);

            ScanResult result = scanner.scan(new ByteArrayInputStream(new byte[]{1, 2, 3}));

            assertThat(result.isInfected()).isTrue();
            assertThat(result.virusName()).isEqualTo("Win.Test.EICAR_HDB-1");
        }
    }

    /**
     * Verifies clamd size limit exceeded ERROR response returns error ScanResult.
     */
    @Test
    @DisplayName("scan: returns error result when clamd responds with ERROR")
    void scan_clamdErrorResponse_returnsErrorResult() throws Exception {
        try (MockClamServer server = new MockClamServer("stream: size limit exceeded ERROR")) {
            server.awaitReady();
            StorageProperties.VirusScanProperties props = createProperties(server.getPort(), Duration.ofSeconds(2), Duration.ofSeconds(5));
            ClamdInstreamScanner scanner = new ClamdInstreamScanner(props);

            ScanResult result = scanner.scan(new ByteArrayInputStream(new byte[]{1, 2, 3}));

            assertThat(result.isError()).isTrue();
            assertThat(result.isClean()).isFalse();
            assertThat(result.isInfected()).isFalse();
            assertThat(result.message()).contains("size limit exceeded");
        }
    }

    /**
     * Verifies unexpected or unparseable responses return error ScanResult.
     */
    @Test
    @DisplayName("scan: returns error result on unexpected or malformed response")
    void scan_malformedResponse_returnsErrorResult() throws Exception {
        try (MockClamServer server = new MockClamServer("UNRECOGNIZED_DAEMON_OUTPUT")) {
            server.awaitReady();
            StorageProperties.VirusScanProperties props = createProperties(server.getPort(), Duration.ofSeconds(2), Duration.ofSeconds(5));
            ClamdInstreamScanner scanner = new ClamdInstreamScanner(props);

            ScanResult result = scanner.scan(new ByteArrayInputStream(new byte[]{1, 2, 3}));

            assertThat(result.isError()).isTrue();
            assertThat(result.message()).contains("Unexpected ClamAV response");
        }
    }

    /**
     * Verifies empty response from daemon returns error ScanResult.
     */
    @Test
    @DisplayName("scan: returns error result when daemon returns empty response")
    void scan_emptyResponse_returnsErrorResult() throws Exception {
        try (MockClamServer server = new MockClamServer("")) {
            server.awaitReady();
            StorageProperties.VirusScanProperties props = createProperties(server.getPort(), Duration.ofSeconds(2), Duration.ofSeconds(5));
            ClamdInstreamScanner scanner = new ClamdInstreamScanner(props);

            ScanResult result = scanner.scan(new ByteArrayInputStream(new byte[]{1, 2, 3}));

            assertThat(result.isError()).isTrue();
            assertThat(result.message()).contains("Empty response");
        }
    }

    /**
     * Verifies socket read timeout returns error ScanResult.
     */
    @Test
    @DisplayName("scan: returns error result when socket read times out")
    void scan_socketTimeout_returnsErrorResult() throws Exception {
        try (MockClamServer server = new MockClamServer("stream: OK", 800)) {
            server.awaitReady();
            StorageProperties.VirusScanProperties props = createProperties(
                    server.getPort(),
                    Duration.ofMillis(500),
                    Duration.ofMillis(100)
            );
            ClamdInstreamScanner scanner = new ClamdInstreamScanner(props);

            ScanResult result = scanner.scan(new ByteArrayInputStream(new byte[]{1, 2, 3}));

            assertThat(result.isError()).isTrue();
            assertThat(result.message()).contains("timeout");
        }
    }

    /**
     * Verifies connection refused returns error ScanResult.
     */
    @Test
    @DisplayName("scan: returns error result when connection is refused")
    void scan_connectionRefused_returnsErrorResult() throws Exception {
        int closedPort;
        try (ServerSocket tempSocket = new ServerSocket(0)) {
            closedPort = tempSocket.getLocalPort();
        }

        StorageProperties.VirusScanProperties props = createProperties(
                closedPort,
                Duration.ofMillis(200),
                Duration.ofMillis(200)
        );
        ClamdInstreamScanner scanner = new ClamdInstreamScanner(props);

        ScanResult result = scanner.scan(new ByteArrayInputStream(new byte[]{1, 2, 3}));

        assertThat(result.isError()).isTrue();
        assertThat(result.message()).contains("I/O failure");
    }

    /**
     * Verifies null content stream throws IllegalArgumentException.
     */
    @Test
    @DisplayName("scan: throws IllegalArgumentException on null content stream")
    void scan_nullContent_throwsIllegalArgumentException() {
        StorageProperties.VirusScanProperties props = createProperties(3310, Duration.ofSeconds(1), Duration.ofSeconds(1));
        ClamdInstreamScanner scanner = new ClamdInstreamScanner(props);

        assertThatThrownBy(() -> scanner.scan((InputStream) null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructorRejectsNullPropertiesAndUnknownVirusNamesUseStableFallback() throws Exception {
        assertThatThrownBy(() -> new ClamdInstreamScanner(null))
                .isInstanceOf(IllegalArgumentException.class);

        try (MockClamServer server = new MockClamServer("stream: FOUND")) {
            server.awaitReady();
            ClamdInstreamScanner scanner = new ClamdInstreamScanner(
                    createProperties(server.getPort(), Duration.ofSeconds(2), Duration.ofSeconds(5)));
            assertThat(scanner.scan(new ByteArrayInputStream(new byte[]{1})).virusName())
                    .isEqualTo("UNKNOWN_VIRUS");
        }
    }

    @Test
    void usesDefaultTimeoutsAndHandlesZeroLengthReadsAndNewlineResponses() throws Exception {
        try (MockClamServer server = new MockClamServer("stream: OK\n")) {
            server.awaitReady();
            StorageProperties.VirusScanProperties props = createProperties(server.getPort(), null, null);
            InputStream zeroThenData = new InputStream() {
                private int calls;
                @Override public int read(byte[] buffer, int offset, int length) {
                    if (calls++ == 0) return 0;
                    if (calls == 2) { buffer[offset] = 1; return 1; }
                    return -1;
                }
                @Override public int read() { return -1; }
            };
            assertThat(new ClamdInstreamScanner(props).scan(zeroThenData).isClean()).isTrue();
        }
    }

    @Test
    void parsesFoundResponseWithoutStreamPrefix() throws Exception {
        try (MockClamServer server = new MockClamServer("Virus.Name FOUND")) {
            server.awaitReady();
            StorageProperties.VirusScanProperties props = createProperties(server.getPort(), null, null);
            assertThat(new ClamdInstreamScanner(props).scan(new ByteArrayInputStream(new byte[]{1})).virusName())
                    .isEqualTo("Virus.Name");
        }
    }

    /**
     * In-memory mock TCP server implementing ClamAV's zINSTREAM protocol.
     */
    private static class MockClamServer implements AutoCloseable {
        private final ServerSocket serverSocket;
        private final ExecutorService executor;
        private final CountDownLatch readyLatch = new CountDownLatch(1);
        private final AtomicReference<byte[]> receivedPayload = new AtomicReference<>();

        MockClamServer(String response) throws IOException {
            this(response, 0);
        }

        MockClamServer(String response, long delayMillis) throws IOException {
            this.serverSocket = new ServerSocket(0);
            this.executor = Executors.newSingleThreadExecutor();
            this.executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    try (Socket clientSocket = serverSocket.accept();
                         InputStream in = clientSocket.getInputStream();
                         OutputStream out = clientSocket.getOutputStream()) {

                        if (delayMillis > 0) {
                            Thread.sleep(delayMillis);
                        }

                        ByteArrayOutputStream cmdBaos = new ByteArrayOutputStream();
                        int b;
                        while ((b = in.read()) != -1) {
                            if (b == 0) {
                                break;
                            }
                            cmdBaos.write(b);
                        }

                        ByteArrayOutputStream payloadBaos = new ByteArrayOutputStream();
                        byte[] lenBytes = new byte[4];
                        while (true) {
                            int read = in.readNBytes(lenBytes, 0, 4);
                            if (read < 4) {
                                break;
                            }
                            int chunkLen = ByteBuffer.wrap(lenBytes).getInt();
                            if (chunkLen == 0) {
                                break;
                            }
                            byte[] chunk = in.readNBytes(chunkLen);
                            payloadBaos.write(chunk);
                        }
                        receivedPayload.set(payloadBaos.toByteArray());

                        if (response != null && !response.isEmpty()) {
                            out.write(response.getBytes(StandardCharsets.US_ASCII));
                            out.write(0);
                            out.flush();
                        }
                    }
                } catch (Exception ignored) {
                }
            });
        }

        int getPort() {
            return serverSocket.getLocalPort();
        }

        byte[] getReceivedPayload() {
            return receivedPayload.get();
        }

        void awaitReady() throws InterruptedException {
            readyLatch.await(5, TimeUnit.SECONDS);
        }

        @Override
        public void close() throws IOException {
            serverSocket.close();
            executor.shutdownNow();
        }
    }
}
