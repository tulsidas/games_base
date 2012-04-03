package client;

import pulpcore.Stage;
import pulpcore.image.Colors;
import pulpcore.image.CoreFont;
import pulpcore.image.CoreGraphics;
import pulpcore.sprite.Sprite;

public class PopUp extends Sprite {

    private String str;

    private static final int HGAP = 10;

    private static final int VGAP = 10;

    private long timestamp;

    public PopUp(CoreFont font, String str) {
        // bogus
        super(0, 0, 0, 0);

        this.str = str;

        if (font == null) {
            font = CoreFont.getSystemFont();
        }

        width.set(font.getStringWidth(str) + VGAP * 2);
        height.set(font.getHeight() + HGAP * 2);
        x.set((Stage.getWidth() - width.getAsInt()) / 2);
        y.set((Stage.getHeight() - height.getAsInt()) / 2);

        timestamp = System.currentTimeMillis();
    }

    @Override
    protected void drawSprite(CoreGraphics g) {
        // fondo
        g.setColor(Colors.BLACK);
        g.fillRect(0, 0, width.getAsInt(), height.getAsInt());

        // relieve
        g.setColor(Colors.DARKGRAY);
        g.fillRect(2, 2, width.getAsInt() - 4, height.getAsInt() - 4);

        // borde
        // g.setColor(Colors.RED);
        // g.drawRect(0, 0, width.getAsInt(), height.getAsInt());

        // texto
        g.setColor(Colors.WHITE);
        g.drawString(str, HGAP, VGAP);
    }

    @Override
    public void update(int elapsedTime) {
        super.update(elapsedTime);

        if (isMousePressed()
                || System.currentTimeMillis() - timestamp > 5 * 1000) {
            getParent().remove(this);
        }
    }
}
