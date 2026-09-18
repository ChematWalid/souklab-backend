package com.project.souklab.dto.common;

import org.junit.jupiter.api.Test;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PatchFieldDeserializerTest {

    @Test
    void exposesThreePatchStatesAndHandlesUncontextualizedValues() throws Exception {
        PatchFieldDeserializer deserializer = new PatchFieldDeserializer();
        DeserializationContext context = mock(DeserializationContext.class);

        assertThat(deserializer.getAbsentValue(context).isDefined()).isFalse();
        assertThat(deserializer.getNullValue(context).isNull()).isTrue();
        assertThat(deserializer.deserialize(mock(JsonParser.class), context).isNull()).isTrue();
    }
}
