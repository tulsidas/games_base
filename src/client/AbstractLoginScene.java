package client;

import static pulpcore.image.Colors.rgb;
import pulpcore.Assets;
import pulpcore.CoreSystem;
import pulpcore.Stage;
import pulpcore.image.CoreImage;
import pulpcore.scene.Scene;
import pulpcore.scene.Scene2D;
import pulpcore.sprite.FilledSprite;
import pulpcore.sprite.ImageSprite;
import client.DisconnectedScene.Reason;

import common.ifaz.LoginHandler;
import common.model.User;

/**
 * @author Tulsi
 */
public abstract class AbstractLoginScene extends Scene2D implements
        LoginHandler {

    private AbstractGameConnector connection;

    public void load() {
        // error handling
        // XXX Stage.setUncaughtExceptionScene(new ErrorScene());
        
        // fondo
        add(new FilledSprite(rgb(0x0D8EE8)));

        CoreImage img = CoreImage.load("imgs/conectando.png");
        add(new ImageSprite(img, Stage.getWidth() / 2 - img.getWidth() / 2,
                Stage.getHeight() / 2 - img.getHeight() / 2));

        long version = 0;

        try {
            version = Long
                    .parseLong(new String(Assets.get("version").getData()));
        }
        catch (Exception e) {
        }

        connection = getGameConnector(CoreSystem.getBaseURL().getHost(),
                Integer.parseInt(CoreSystem.getAppProperty("salon")),
                CoreSystem.getAppProperty("nick"), CoreSystem
                        .getAppProperty("hash"), version);

        connection.setLoginHandler(this); // inject scene handler
        connection.connect();
    }

    // ////////////////
    // LoginHandler //
    // ////////////////
    public void loggedIn(final User user) {
        invokeLater(new Runnable() {
            public void run() {
                Stage.setScene(getLobbyScene(user, connection));
            }
        });
    }

    public void disconnected() {
        connection.setLoginHandler(null);
        invokeLater(new Runnable() {
            public void run() {
                Stage.setScene(new DisconnectedScene(Reason.SERVER_DEAD));
            }
        });
    }

    public void duplicatedLogin() {
        connection.setLoginHandler(null);
        invokeLater(new Runnable() {
            public void run() {
                Stage.setScene(new DisconnectedScene(Reason.DUPLICATED_LOGIN));
            }
        });
    }

    public void banned() {
        connection.setLoginHandler(null);
        invokeLater(new Runnable() {
            public void run() {
                Stage.setScene(new DisconnectedScene(Reason.BANNED));
            }
        });
    }

    public void kicked() {
        connection.setLoginHandler(null);
        invokeLater(new Runnable() {
            public void run() {
                Stage.setScene(new DisconnectedScene(Reason.KICKED));
            }
        });
    }

    public void wrongVersion() {
        connection.setLoginHandler(null);
        invokeLater(new Runnable() {
            public void run() {
                Stage.setScene(new DisconnectedScene(Reason.WRONG_VERSION));
            }
        });
    }

    // abstract methods
    protected abstract Scene getLobbyScene(User user,
            AbstractGameConnector connection);

    protected abstract AbstractGameConnector getGameConnector(String host,
            int salon, String user, String pass, long version);

    protected abstract int getPort();
}