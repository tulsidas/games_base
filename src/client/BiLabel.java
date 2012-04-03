package client;

import pulpcore.image.Colors;
import pulpcore.image.CoreFont;
import pulpcore.image.CoreGraphics;
import pulpcore.sprite.Label;

public class BiLabel extends Label {

    protected CoreFont tintedFont;

    protected char separator;

    private boolean highlighted;

    public BiLabel(CoreFont tintedFont, CoreFont font, String text,
            char separator, int x, int y) {
        this(tintedFont, font, text, separator, x, y, false);
    }

    public BiLabel(CoreFont tintedFont, CoreFont font, String text,
            char separator, int x, int y, boolean highlighted) {
        super(font, text, x, y);

        this.highlighted = highlighted;
        this.tintedFont = tintedFont;
        this.separator = separator;
    }

    @Override
    protected void drawText(CoreGraphics g, String text) {
        if (highlighted) {
            g.setColor(Colors.rgba(0x2222FF, 64));
            g.fillRect(0, 0, width.getAsInt(), height.getAsInt());
        }

        int index = text.indexOf(separator);
        if (index > 0) {
            g.setFont(tintedFont);
            g.drawString(text.substring(0, index));

            g.setFont(getFont());
            g.drawString(text.substring(index, text.length()), tintedFont
                    .getStringWidth(text.substring(0, index)), 0);
        }
        else {
            g.setFont(getFont());
            g.drawString(text);
        }
    }

    @Override
    public String toString() {
        return "BiLabel - " + getText();
    }
}