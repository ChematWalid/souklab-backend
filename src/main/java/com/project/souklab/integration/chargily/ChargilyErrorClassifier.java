package com.project.souklab.integration.chargily;

public class ChargilyErrorClassifier {
    public boolean isRetryable(int status) {
        return status == 429 || status >= 500;
    }
}
