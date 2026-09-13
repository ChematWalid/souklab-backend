package com.project.souklab.dto.common;

import tools.jackson.databind.annotation.JsonDeserialize;

import java.util.function.Consumer;

/**
 * A three-state generic container for JSON Merge Patch fields.
 *
 * <p>Distinguishes between three states for a PATCH request field:
 * <ul>
 *   <li><strong>Undefined</strong> — the field was omitted from the JSON payload; no change should be applied.</li>
 *   <li><strong>Null</strong> — the field was present in the JSON payload with an explicit {@code null} value; the target field should be cleared.</li>
 *   <li><strong>Present</strong> — the field was present in the JSON payload with a non-null value; the target field should be set to that value.</li>
 * </ul>
 *
 * <p>Instances are created via the static factory methods {@link #undefined()} and {@link #of(Object)}.
 * Jackson 3 deserialization is handled by {@link PatchFieldDeserializer}, which correctly maps
 * absent fields to {@code undefined()} and explicit JSON {@code null} to {@code of(null)}.
 *
 * @param <T> the type of the wrapped value
 */
@JsonDeserialize(using = PatchFieldDeserializer.class)
public final class PatchField<T> {

    private final boolean defined;
    private final T value;

    private PatchField(boolean defined, T value) {
        this.defined = defined;
        this.value = value;
    }

    /**
     * Creates a {@code PatchField} representing an absent (omitted) field.
     * {@link #isDefined()} returns {@code false} for instances created by this method.
     *
     * @param <T> the phantom type parameter
     * @return a new undefined {@code PatchField}
     */
    public static <T> PatchField<T> undefined() {
        return new PatchField<>(false, null);
    }

    /**
     * Creates a {@code PatchField} representing a field that was present in the JSON payload.
     * The wrapped {@code value} may be {@code null}, which signals an intentional clear operation.
     *
     * @param value the deserialized field value, or {@code null} to represent an explicit JSON {@code null}
     * @param <T>   the type of the wrapped value
     * @return a new defined {@code PatchField} wrapping {@code value}
     */
    public static <T> PatchField<T> of(T value) {
        return new PatchField<>(true, value);
    }

    /**
     * Returns {@code true} if this field was present in the JSON payload (including explicit {@code null}).
     *
     * @return {@code true} if the field was defined in the payload
     */
    public boolean isDefined() {
        return defined;
    }

    /**
     * Returns the wrapped value, which may be {@code null} when the field was set to JSON {@code null}.
     *
     * @return the deserialized value, or {@code null}
     */
    public T getValue() {
        return value;
    }

    /**
     * Returns {@code true} if the field was present in the payload and its value is JSON {@code null}.
     * This condition signals that the corresponding entity field should be cleared.
     *
     * @return {@code true} if defined and the value is {@code null}
     */
    public boolean isNull() {
        return defined && value == null;
    }

    /**
     * Returns {@code true} if the field was present in the payload and has a non-null value.
     *
     * @return {@code true} if defined and the value is non-null
     */
    public boolean hasValue() {
        return defined && value != null;
    }

    /**
     * Invokes the given {@code action} with the wrapped value if and only if this field is defined.
     * The action is also invoked when the value is {@code null} (explicit clear signal).
     *
     * @param action the consumer to invoke; receives the wrapped value (possibly {@code null})
     */
    public void ifDefined(Consumer<T> action) {
        if (defined) {
            action.accept(value);
        }
    }
}
