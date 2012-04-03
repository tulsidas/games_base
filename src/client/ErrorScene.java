package client;

import pulpcore.CoreSystem;
import pulpcore.Stage;
import pulpcore.image.Colors;
import pulpcore.image.CoreFont;
import pulpcore.scene.Scene2D;
import pulpcore.sprite.FilledSprite;
import pulpcore.sprite.Group;
import pulpcore.sprite.Label;

public class ErrorScene extends Scene2D {

    // Only send once per browser session to avoid talkback spam
    static boolean uploadedThisSession = false;

    @Override
    public void load() {
        add(new FilledSprite(Colors.rgb(0, 0, 170)));

        // Send the talkback fields via POST
        if (!uploadedThisSession
                && "true".equals(CoreSystem.getAppProperty("talkback"))) {
            uploadedThisSession = true;
            CoreSystem.uploadTalkBackFields("/talkback.py");
        }

        CoreFont font = CoreFont.getSystemFont().tint(Colors.WHITE);
        Group message = Label.createMultilineLabel(font,
                "Oops! Se rompió... recargá la página", Stage.getWidth() / 2,
                150, Stage.getWidth() - 20);
        message.setAnchor(0.5, 0.5);
        add(message);
    }
}
