package client;

import java.util.Iterator;

import pulpcore.image.CoreFont;
import pulpcore.image.CoreImage;
import pulpcore.sprite.Button;

import common.ifaz.LobbyHandler;
import common.model.AbstractRoom;
import common.model.User;

public class RoomsBox extends ButtonScrollable<AbstractRoom> {

   private LobbyHandler handler;

   private CoreImage[] open, closed;

   public RoomsBox(LobbyHandler handler, int x, int y, int w, int h,
         CoreFont font) {
      super(x, y, w, h, font);
      this.handler = handler;

      CoreImage img = CoreImage.load("imgs/btn-closed-room.png");
      closed = new CoreImage[] { img, img, img };
      open = CoreImage.load("imgs/btn-open-room.png").split(3);

      setSorted(true);
   }

   @Override
   public int getLineSpacing() {
      return open[0].getHeight();
   }

   /**
    * agrega la sala al componente
    * 
    * @param r
    */
   public void addRoom(AbstractRoom r) {
      addItem(r);
   }

   /**
    * agrega la sala al componente
    * 
    * @param id
    *           roomId
    */
   public void dropRoom(int id) {
      Iterator<AbstractRoom> it = getObjects().iterator();
      while (it.hasNext()) {
         AbstractRoom r = it.next();
         if (r.getId() == id) {
            it.remove();
         }
      }
      refresh();
   }

   /**
    * Agrega al usuario u a la sala room
    * 
    * @param room
    * @param u
    */
   public void addPlayer(AbstractRoom room, User u) {
      for (AbstractRoom r : getObjects()) {
         if (r.equals(room)) {
            r.addPlayer(u);
         }
      }
      refresh();
   }

   /**
    * Saca al usuario u de la sala room
    * 
    * @param room
    * @param u
    */
   public void removePlayer(int roomId, User u) {
      for (AbstractRoom r : getObjects()) {
         if (r.getId() == roomId) {
            r.removePlayer(u);
         }
      }
      refresh();
   }

   @Override
   protected Button createButton(AbstractRoom room, int x, int y) {
      String text = room.getDisplayText();
      CoreImage[] images = room.isFull() || room.isStarted() ? closed : open;

      return Button.createLabeledButton(images, font, text, x, y);
   }

   public void buttonClicked(AbstractRoom r) {
      if (!r.isFull() && !r.isStarted()) {
         handler.joinRoomRequest(r);
      }
   }

   /**
    * Disable the button belonging to the room
    * 
    * @param room
    */
   public void enableButton(AbstractRoom room, boolean enabled) {
      for (Button b : map.keySet()) {
         if (map.get(b).equals(room)) {
            b.enabled.set(enabled);
         }
      }
   }

   /**
    * agrega la sala al componente
    * 
    * @param id
    *           roomId
    */
   public void gameStarted(int id) {
      Iterator<AbstractRoom> it = getObjects().iterator();
      while (it.hasNext()) {
         AbstractRoom r = it.next();
         if (r.getId() == id) {
            r.setStarted(true);
         }
      }
      refresh();
   }
}