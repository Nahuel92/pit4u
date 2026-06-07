package io.github.nahuel92.pit4u.highlighter;

import com.intellij.openapi.editor.markup.GutterIconRenderer;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.util.Objects;

public final class PitMutationGutterIconRenderer extends GutterIconRenderer {
    private final String tooltipHtml;
    private final Icon icon;

    public PitMutationGutterIconRenderer(final String tooltipHtml, final Icon icon) {
        this.tooltipHtml = tooltipHtml;
        this.icon = icon;
    }

    @Override
    @NotNull
    public Icon getIcon() {
        return icon;
    }

    @Override
    public String getTooltipText() {
        return tooltipHtml;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tooltipHtml);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PitMutationGutterIconRenderer that)) {
            return false;
        }
        return Objects.equals(tooltipHtml, that.tooltipHtml);
    }
}
