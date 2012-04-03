package client;

import java.util.List;

import pulpcore.image.CoreFont;
import pulpcore.sprite.Label;
import pulpcore.util.StringUtil;

public abstract class LabelScrollable extends Scrollable<String> {

   protected CoreFont font;

   public LabelScrollable(int x, int y, int w, int h, CoreFont font) {
      super(x, y, w, h);

      this.font = font;
   }

   public void createContent(List<String> objects) {
      int posY = 0;

      for (String str : objects) {
         String[] text = StringUtil.wordWrap(str, font, getAvailableSpace());

         if (text.length == 0) {
            text = new String[] { " " };
         }

         for (int j = 0; j < text.length; j++) {
            String line = StringUtil.replace(text[j], "\t", "   ");

            add(new Label(font, line, 0, posY));

            posY += getLineSpacing();
         }
      }
   }

   @Override
   public int getLineSpacing() {
      return font.getHeight() + 2;
   }
}
