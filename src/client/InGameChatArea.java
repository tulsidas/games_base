package client;

import pulpcore.image.CoreFont;

public class InGameChatArea extends LabelScrollable {

   public InGameChatArea(CoreFont font, int x, int y, int w, int h) {
      super(x, y, w, h, font);

      setMaxLines(100);
   }

   public void addLine(String msg) {
      addItem(msg);
   }
}
