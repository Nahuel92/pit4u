package io.github.nahuel92.pit4u.highlighter;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import io.github.nahuel92.pit4u.highlighter.dto.Mutations;
import org.jetbrains.annotations.NotNull;
import tools.jackson.core.JacksonException;
import tools.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.util.List;

public final class XMLDataParser {
    private static final Logger LOG = Logger.getInstance(XMLDataParser.class);
    private static final XmlMapper XML_MAPPER = new XmlMapper();

    private XMLDataParser() {
    }

    @NotNull
    public static Mutations parse(@NotNull final VirtualFile virtualFile) {
        virtualFile.refresh(false, false);
        if (virtualFile.getLength() == 0) {
            LOG.warn("Mutations file is empty");
            return new Mutations(List.of());
        }
        try (final var inputStream = virtualFile.getInputStream()) {
            return XML_MAPPER.readValue(inputStream, Mutations.class);
        } catch (final JacksonException | IOException e) {
            LOG.warn("Failed to parse PIT report file from VirtualFile: " + virtualFile.getPath(), e);
            return new Mutations(List.of());
        }
    }
}
