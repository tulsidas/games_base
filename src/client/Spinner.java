package client;

import pulpcore.image.Colors;
import pulpcore.image.CoreFont;
import pulpcore.image.CoreImage;
import pulpcore.sprite.Button;
import pulpcore.sprite.Group;
import pulpcore.sprite.TextField;

public class Spinner extends Group {

   // scroll buttons
   private Button up, down;

   private TextField textField;

   private CoreFont font;

   private int value;

   private int step;

   private int minValue, maxValue;

   public Spinner(int x, int y, int width, int height, CoreFont font,
         CoreImage[] upImgs) {
      this(x, y, width, height, font, upImgs, null);
   }

   public Spinner(int x, int y, int width, int height, CoreFont font,
         CoreImage[] upImgs, CoreImage[] downImgs) {
      super(x, y, width, height);

      this.value = 10;

      // default values
      this.step = 1;
      this.minValue = 0;
      this.maxValue = 100;

      this.font = font;

      // scroll buttons
      if (upImgs == null) {
         throw new IllegalArgumentException("Arrow images mustn't be null");
      }
      else if (upImgs.length < 3) {
         throw new IllegalArgumentException(
               "Must supply at least 3 arrow images (regular, hover, pressed)");
      }

      if (downImgs == null || downImgs.length < 3) {
         downImgs = new CoreImage[3];

         // flip up images
         for (int i = 0; i < 3; i++) {
            CoreImage image = upImgs[i];
            downImgs[i] = image.flip();
         }
      }

      int xgap = 0;
      int ygap = 0;

      // add(new FilledSprite(0, 0, width, height, Colors.rgba(0x222222,
      // 0x77)));

      up = new Button(upImgs, width - upImgs[0].getWidth() - xgap, ygap);

      down = new Button(downImgs, width - downImgs[0].getWidth() - xgap, height
            - ygap);

      textField = new TextField(font, font.tint(Colors.WHITE), Integer
            .toString(value), xgap, ygap, width - upImgs[0].getWidth() - xgap,
            -1);
      textField.setMaxNumChars(3);

      // add(new FilledSprite(textField.x.getAsInt(), textField.y.getAsInt(),
      // textField.width.getAsInt(), textField.height.getAsInt(), Colors
      // .rgba(0xFF0000, 0x77)));

      add(textField);
      add(up);
      add(down);

      drawText();
   }

   public void setMaxNumChars(int maxNumChars) {
      textField.setMaxNumChars(maxNumChars);
   }

   @Override
   public void update(int elapsedTime) {
      super.update(elapsedTime);

      int newValue = 0;
      try {
         newValue = Integer.parseInt(textField.getText());
      }
      catch (NumberFormatException nfe) {
         // nada
      }

      if (newValue != value) {
         value = newValue;
         drawText();
      }
      else if (up.isClicked()) {
         if (value + step <= maxValue) {
            value += step;
            drawText();
         }
      }
      else if (down.isClicked()) {
         if (value - step >= minValue) {
            value -= step;
            drawText();
         }
      }
   }

   public void setStep(int step) {
      this.step = step;
   }

   public int getMinValue() {
      return minValue;
   }

   public void setMinValue(int minValue) {
      this.minValue = minValue;

      if (minValue > value) {
         value = minValue;
         drawText();
      }
   }

   public int getMaxValue() {
      return maxValue;
   }

   public void setMaxValue(int maxValue) {
      this.maxValue = maxValue;

      if (maxValue < value) {
         value = maxValue;
         drawText();
      }
   }

   public int getStep() {
      return step;
   }

   public int getValue() {
      return value;
   }

   public void setValue(int value) {
      this.value = value;
      drawText();
   }

   private void drawText() {
      String val = Integer.toString(value);
      int textWidth = font.getStringWidth(val);
      int w = width.getAsInt() - up.width.getAsInt();
      textField.setText(val);

      textField.x.set((w - textWidth) / 2);

      setDirty(true);
   }
}