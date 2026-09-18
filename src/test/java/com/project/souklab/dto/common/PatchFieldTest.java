package com.project.souklab.dto.common;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class PatchFieldTest {

    @Test
    void distinguishesUndefinedField() {
        PatchField<String> field = PatchField.undefined();
        AtomicReference<String> calledWith = new AtomicReference<>("not-called");

        field.ifDefined(calledWith::set);

        assertThat(field.isDefined()).isFalse();
        assertThat(field.isNull()).isFalse();
        assertThat(field.hasValue()).isFalse();
        assertThat(field.getValue()).isNull();
        assertThat(calledWith).hasValue("not-called");
    }

    @Test
    void distinguishesExplicitNullAndInvokesConsumer() {
        PatchField<String> field = PatchField.of(null);
        AtomicReference<String> called = new AtomicReference<>("not-called");

        field.ifDefined(called::set);

        assertThat(field.isDefined()).isTrue();
        assertThat(field.isNull()).isTrue();
        assertThat(field.hasValue()).isFalse();
        assertThat(called).hasValue(null);
    }

    @Test
    void distinguishesPresentValueAndInvokesConsumer() {
        PatchField<String> field = PatchField.of("updated");
        AtomicReference<String> called = new AtomicReference<>();

        field.ifDefined(called::set);

        assertThat(field.isDefined()).isTrue();
        assertThat(field.isNull()).isFalse();
        assertThat(field.hasValue()).isTrue();
        assertThat(field.getValue()).isEqualTo("updated");
        assertThat(called).hasValue("updated");
    }
}
