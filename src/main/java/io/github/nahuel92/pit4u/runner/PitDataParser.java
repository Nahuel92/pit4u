package io.github.nahuel92.pit4u.runner;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.github.nahuel92.pit4u.highlighter.Mutations;
import org.apache.commons.lang3.exception.UncheckedException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public final class PitDataParser {
    private static final XmlMapper XML_MAPPER = new XmlMapper();

    public static Mutations parse(@NotNull final Path path) {
        try {
            return XML_MAPPER.readValue(path.toFile(), Mutations.class);
        } catch (final IOException e) {
            throw new UncheckedException(e);
        }
    }
}
