package com.project.souklab.filestorage.scan;

import com.project.souklab.filestorage.config.StorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Hand-rolled ClamAV daemon client implementing the zINSTREAM protocol over a TCP socket.
 * Establishes a dedicated single-transaction socket per scan operation, streams chunked
 * data prefixed by 4-byte big-endian integer lengths, sends a 0-length termination chunk,
 * and parses ClamAV's null-delimited response.
 */
public class ClamdInstreamScanner implements VirusScanner {

    private static final Logger log = LoggerFactory.getLogger(ClamdInstreamScanner.class);

    private static final int CHUNK_SIZE = 8192;
    private static final int LENGTH_HEADER_BYTES = 4;
    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 2000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 10000;
    private static final byte[] INSTREAM_COMMAND = "zINSTREAM\0".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] TERMINATION_CHUNK = new byte[]{0, 0, 0, 0};
    private static final String RESPONSE_OK_SUFFIX = "OK";
    private static final String RESPONSE_FOUND_SUFFIX = "FOUND";
    private static final String RESPONSE_ERROR_SUFFIX = "ERROR";
    private static final String STREAM_PREFIX = "stream:";

    private final StorageProperties.VirusScanProperties properties;

    public ClamdInstreamScanner(StorageProperties.VirusScanProperties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("VirusScanProperties cannot be null");
        }
        this.properties = properties;
    }

    /**
     * Scans the provided input stream using ClamAV's INSTREAM protocol over TCP.
     *
     * @param content stream containing the payload to scan
     * @return ScanResult indicating clean, infected (with signature), or error
     */
    @Override
    public ScanResult scan(InputStream content) {
        if (content == null) {
            throw new IllegalArgumentException("Content stream cannot be null");
        }

        String host = properties.getHost();
        int port = properties.getPort();
        int connectTimeout = properties.getConnectionTimeout() != null
                ? (int) properties.getConnectionTimeout().toMillis()
                : DEFAULT_CONNECT_TIMEOUT_MS;
        int readTimeout = properties.getReadTimeout() != null
                ? (int) properties.getReadTimeout().toMillis()
                : DEFAULT_READ_TIMEOUT_MS;

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), connectTimeout);
            socket.setSoTimeout(readTimeout);

            try (OutputStream out = socket.getOutputStream();
                 InputStream in = socket.getInputStream()) {

                out.write(INSTREAM_COMMAND);
                out.flush();

                byte[] buffer = new byte[CHUNK_SIZE];
                int bytesRead;
                while ((bytesRead = content.read(buffer)) != -1) {
                    if (bytesRead > 0) {
                        ByteBuffer lengthBuffer = ByteBuffer.allocate(LENGTH_HEADER_BYTES).putInt(bytesRead);
                        out.write(lengthBuffer.array());
                        out.write(buffer, 0, bytesRead);
                    }
                }

                out.write(TERMINATION_CHUNK);
                out.flush();

                String response = readResponse(in);
                return parseResponse(response);
            }
        } catch (SocketTimeoutException e) {
            log.warn("ClamAV socket timeout connecting to or reading from {}:{}: {}", host, port, e.getMessage());
            return ScanResult.error("ClamAV socket timeout: " + e.getMessage());
        } catch (IOException e) {
            log.warn("ClamAV communication error with {}:{}: {}", host, port, e.getMessage());
            return ScanResult.error("ClamAV I/O failure: " + e.getMessage());
        }
    }

    /**
     * Reads the response bytes from the socket until a null byte delimiter, newline, or EOF.
     *
     * @param in socket input stream
     * @return trimmed ASCII response string
     * @throws IOException on socket read failure
     */
    private String readResponse(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1) {
            if (b == 0 || b == '\n') {
                break;
            }
            baos.write(b);
        }
        return baos.toString(StandardCharsets.US_ASCII).trim();
    }

    /**
     * Parses the response string returned by the ClamAV daemon.
     *
     * @param response the raw trimmed response string
     * @return ScanResult reflecting clean, infected, or error
     */
    private ScanResult parseResponse(String response) {
        if (response.isBlank()) {
            return ScanResult.error("Empty response from ClamAV daemon");
        }

        if (response.endsWith(RESPONSE_OK_SUFFIX)) {
            return ScanResult.clean();
        }

        if (response.endsWith(RESPONSE_FOUND_SUFFIX)) {
            String candidate = response.substring(0, response.length() - RESPONSE_FOUND_SUFFIX.length()).trim();
            if (candidate.startsWith(STREAM_PREFIX)) {
                candidate = candidate.substring(STREAM_PREFIX.length()).trim();
            }
            String virusName = candidate.isEmpty() ? "UNKNOWN_VIRUS" : candidate;
            return ScanResult.infected(virusName);
        }

        if (response.endsWith(RESPONSE_ERROR_SUFFIX)) {
            return ScanResult.error("ClamAV reported error: " + response);
        }

        return ScanResult.error("Unexpected ClamAV response: " + response);
    }
}
