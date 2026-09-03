package io.github.nahuel92.pit4u.highlighter;

import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Objects;

public final class LineIcon implements Icon {
    static final LineIcon GRAY = new LineIcon(JBColor.GRAY);
    static final LineIcon GREEN = new LineIcon(
            new JBColor(
                    new Color(46, 139, 87),
                    new Color(60, 179, 113)
            )
    );
    static final LineIcon RED = new LineIcon(
            new JBColor(
                    new Color(178, 34, 34),
                    new Color(220, 20, 60)
            )
    );

    private final Color color;
    private final int size = 12;

    private LineIcon(@NotNull final Color color) {
        this.color = Objects.requireNonNull(color);
    }

    @Override
    public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
        final var g2d = (Graphics2D) g.create();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.fillRoundRect(x, y, size, size, 4, 4);
        } finally {
            g2d.dispose();
        }
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }
}