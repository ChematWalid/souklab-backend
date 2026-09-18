package com.project.souklab.filestorage.scan;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScanResultTest {

    @Test
    void exposesCleanResult() {
        ScanResult result = ScanResult.clean();

        assertThat(result.status()).isEqualTo(ScanResult.Status.CLEAN);
        assertThat(result.isClean()).isTrue();
        assertThat(result.isInfected()).isFalse();
        assertThat(result.isError()).isFalse();
    }

    @Test
    void exposesInfectedResult() {
        ScanResult result = ScanResult.infected("EICAR");

        assertThat(result.status()).isEqualTo(ScanResult.Status.INFECTED);
        assertThat(result.virusName()).isEqualTo("EICAR");
        assertThat(result.message()).isEqualTo("Malware signature detected: EICAR");
        assertThat(result.isInfected()).isTrue();
    }

    @Test
    void exposesErrorResult() {
        ScanResult result = ScanResult.error("daemon unavailable");

        assertThat(result.status()).isEqualTo(ScanResult.Status.ERROR);
        assertThat(result.message()).isEqualTo("daemon unavailable");
        assertThat(result.isError()).isTrue();
    }
}
