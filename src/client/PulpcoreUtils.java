package client;

import pulpcore.sprite.Sprite;

public class PulpcoreUtils {
   public static final void centerSprite(Sprite spr, int base, int width) {
      spr.x.set(base + (width - spr.width.getAsInt()) / 2);
   }

   public static final void alignRight(Sprite spr, int x) {
      spr.x.set(x - spr.width.getAsInt());
   }
}
