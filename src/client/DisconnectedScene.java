package client;

import pulpcore.Stage;
import pulpcore.image.Colors;
import pulpcore.image.CoreImage;
import pulpcore.scene.Scene2D;
import pulpcore.sprite.FilledSprite;
import pulpcore.sprite.ImageSprite;

public class DisconnectedScene extends Scene2D {

   public enum Reason {
      DUPLICATED_LOGIN, ERROR, BANNED, FAILED, SERVER_DEAD, KICKED, WRONG_VERSION
   }

   private CoreImage img;

   public DisconnectedScene(Reason reason) {
      if (reason == Reason.DUPLICATED_LOGIN) {
         img = CoreImage.load("imgs/conexion-duplicada.png");
      }
      else if (reason == Reason.ERROR) {
         img = CoreImage.load("imgs/error-conexion.png");
      }
      else if (reason == Reason.FAILED) {
         img = CoreImage.load("imgs/fallo-conexion.png");
      }
      else if (reason == Reason.SERVER_DEAD) {
         img = CoreImage.load("imgs/server-dead.png");
      }
      else if (reason == Reason.BANNED) {
         img = CoreImage.load("imgs/baneado.png");
      }
      else if (reason == Reason.KICKED) {
         img = CoreImage.load("imgs/pateado.png");
      }
      else if (reason == Reason.WRONG_VERSION) {
         img = CoreImage.load("imgs/wrong_version.png");
      }
   }

   public void load() {
      // fondo
      add(new FilledSprite(Colors.rgb(0x0d8ee8)));

      // imagen!
      add(new ImageSprite(img, Stage.getWidth() / 2 - img.getWidth() / 2, Stage
            .getHeight()
            / 2 - img.getHeight() / 2));
   }
}