package io.github.nahuel92.pit4u.highlighter;

import com.intellij.openapi.diagnostic.Logger;
import io.github.nahuel92.pit4u.highlighter.dto.Mutations;
import org.apache.commons.lang3.exception.UncheckedException;
import org.jetbrains.annotations.NotNull;
import tools.jackson.core.JacksonException;
import tools.jackson.dataformat.xml.XmlMapper;

import java.nio.file.Path;

public final class XMLDataParser {
    private static final Logger LOG = Logger.getInstance(XMLDataParser.class);
    private static final XmlMapper XML_MAPPER = new XmlMapper();

    public static Mutations parse(@NotNull final Path path) {
        try {
            return XML_MAPPER.readValue(path.toFile(), Mutations.class);
        } catch (final JacksonException e) {
            LOG.warn("Failed to parse PIT XML file", e);
            throw new UncheckedException(e);
        }
    }
}
