package client;

import java.util.Iterator;
import java.util.List;

import pulpcore.image.CoreFont;
import pulpcore.sprite.Label;
import pulpcore.util.StringUtil;

public class ColoredChatArea extends LabelScrollable {

   private char separator;

   private CoreFont font, tintedFont;

   private String username;

   public ColoredChatArea(int x, int y, int w, int h, CoreFont font,
         CoreFont tintedFont, char separator, String username) {
      super(x, y, w, h, font);

      this.separator = separator;
      this.username = username;
      this.font = font;
      this.tintedFont = tintedFont;
   }

   public void addLine(String msg) {
      if (msg == null || msg.trim().length() == 0) {
         return;
      }
      else {
         addItem(msg);
      }
   }

   public void createContent(List<String> objects) {
      int y = 0;
      int w = getAvailableSpace();
      Iterator<String> it = objects.iterator();
      while (it.hasNext()) {
         String str = it.next();
         boolean highlight = false;
         int sepIndex = str.indexOf(separator);
         if (sepIndex > 0) {
            highlight = str.substring(sepIndex).toLowerCase().indexOf(
                  username.toLowerCase()) >= 0;
         }

         String[] text = StringUtil.wordWrap(str, font, w);

         if (text.length == 0) {
            text = new String[] { " " };
         }

         String line = StringUtil.replace(text[0], "\t", "   ");
         add(new BiLabel(tintedFont, font, line, separator, 0, y, highlight));

         y += getLineSpacing();

         for (int j = 1; j < text.length; j++) {
            line = StringUtil.replace(text[j], "\t", "   ");

            add(new Label(font, line, 0, y));

            y += getLineSpacing();
         }
      }
   }
}
