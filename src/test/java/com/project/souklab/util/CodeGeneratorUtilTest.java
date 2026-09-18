package com.project.souklab.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CodeGeneratorUtilTest {
    @Test void generatesNumericCodesOfRequestedLength() {
        assertThat(CodeGeneratorUtil.generateNumericCode(1)).matches("\\d");
        assertThat(CodeGeneratorUtil.generateNumericCode(6)).matches("\\d{6}");
        assertThat(CodeGeneratorUtil.generateNumericCode(10)).matches("\\d{10}");
    }

    @Test void rejectsInvalidDigitCounts() {
        assertThatThrownBy(() -> CodeGeneratorUtil.generateNumericCode(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CodeGeneratorUtil.generateNumericCode(11)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void padsCodesWhenRandomValueHasFewerDigits() {
        boolean observedPadding = false;
        for (int i = 0; i < 200 && !observedPadding; i++) {
            observedPadding = CodeGeneratorUtil.generateNumericCode(1).equals("0");
        }
        assertThat(observedPadding).isTrue();
    }
}
