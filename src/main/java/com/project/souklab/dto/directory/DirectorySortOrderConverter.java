package com.project.souklab.dto.directory;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/** Converts directory sort query parameters into grouped sort enums. */
@Component
public class DirectorySortOrderConverter implements Converter<String, DirectorySortOrder.Key> {
    @Override
    public DirectorySortOrder.Key convert(String source) {
        return DirectorySortOrder.fromValue(source);
    }
}
