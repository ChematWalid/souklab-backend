package com.project.souklab.dto.common;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;

/**
 * Jackson 3 {@link ValueDeserializer} for {@link PatchField}{@code <T>}.
 *
 * <p>Implements contextual deserialization to resolve the generic type parameter {@code T}
 * at wire-up time using {@link #createContextual(DeserializationContext, BeanProperty)}.
 * This allows a single registered deserializer to serve all {@code PatchField<T>} fields
 * regardless of the concrete type of {@code T}.
 *
 * <p>The three-state mapping is:
 * <ul>
 *   <li>Field <em>absent</em> from JSON — {@link #getAbsentValue(DeserializationContext)} returns {@link PatchField#undefined()}.</li>
 *   <li>Field present with explicit JSON {@code null} — {@link #getNullValue(DeserializationContext)} returns {@link PatchField#of(Object) PatchField.of(null)}.</li>
 *   <li>Field present with a non-null value — {@link #deserialize(JsonParser, DeserializationContext)} delegates to the inner type deserializer and wraps the result in {@link PatchField#of(Object)}.</li>
 * </ul>
 */
public class PatchFieldDeserializer extends ValueDeserializer<PatchField<?>> {

    private final ValueDeserializer<Object> innerDeserializer;

    /**
     * No-argument constructor used by Jackson during initial registration via
     * {@link tools.jackson.databind.annotation.JsonDeserialize @JsonDeserialize}.
     * The real contextual instance is built in {@link #createContextual(DeserializationContext, BeanProperty)}.
     */
    public PatchFieldDeserializer() {
        this.innerDeserializer = null;
    }

    /**
     * Private constructor used when creating a contextually resolved instance that
     * already knows the inner deserializer for the concrete type {@code T}.
     *
     * @param innerDeserializer the resolved {@link ValueDeserializer} for the wrapped type {@code T}
     */
    private PatchFieldDeserializer(ValueDeserializer<Object> innerDeserializer) {
        this.innerDeserializer = innerDeserializer;
    }

    /**
     * Creates a contextually resolved deserializer instance by inspecting the declared type
     * of the annotated {@link BeanProperty} to extract the generic type parameter {@code T}.
     * The inner deserializer for {@code T} is resolved from the {@link DeserializationContext}.
     *
     * @param ctxt     the current deserialization context
     * @param property the bean property being deserialized (provides the declared generic type)
     * @return a contextual {@code PatchFieldDeserializer} bound to the resolved inner deserializer
     * @throws JacksonException if the inner deserializer cannot be resolved
     */
    @Override
    @SuppressWarnings("unchecked")
    public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
            throws JacksonException {
        JavaType wrapperType = property != null ? property.getType() : ctxt.getContextualType();
        JavaType innerType = wrapperType != null && wrapperType.containedTypeCount() > 0
                ? wrapperType.containedType(0)
                : ctxt.constructType(Object.class);
        ValueDeserializer<Object> inner =
                (ValueDeserializer<Object>) ctxt.findContextualValueDeserializer(innerType, property);
        return new PatchFieldDeserializer(inner);
    }

    /**
     * Deserializes a non-null JSON value by delegating to the inner type deserializer
     * and wrapping the result in {@link PatchField#of(Object)}.
     *
     * @param p    the JSON parser positioned at the current token
     * @param ctxt the current deserialization context
     * @return a defined {@link PatchField} wrapping the deserialized inner value
     * @throws JacksonException if the inner value cannot be deserialized
     */
    @Override
    public PatchField<?> deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        Object value = innerDeserializer != null ? innerDeserializer.deserialize(p, ctxt) : null;
        return PatchField.of(value);
    }

    /**
     * Returns {@link PatchField#of(Object) PatchField.of(null)} when the JSON field is
     * explicitly set to {@code null}. This signals that the corresponding entity field
     * should be cleared.
     *
     * @param ctxt the current deserialization context (unused)
     * @return a defined {@link PatchField} wrapping {@code null}
     */
    @Override
    public PatchField<?> getNullValue(DeserializationContext ctxt) {
        return PatchField.of(null);
    }

    /**
     * Returns {@link PatchField#undefined()} when the JSON field is absent entirely from
     * the payload. This signals that the corresponding entity field should not be changed.
     *
     * @param ctxt the current deserialization context (unused)
     * @return an undefined {@link PatchField}
     */
    @Override
    public PatchField<?> getAbsentValue(DeserializationContext ctxt) {
        return PatchField.undefined();
    }
}
