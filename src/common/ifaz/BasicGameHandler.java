package common.ifaz;

import common.model.AbstractRoom;
import common.model.User;

/**
 * Mensajes que llegan al cliente
 */
public interface BasicGameHandler {
   /**
    * Un usuario ingreso a una sala
    * 
    * @param room
    * @param user
    */
   public void roomJoined(AbstractRoom room, User user);

   /**
    * @param from
    *           el remitente
    * @param msg
    *           el mensaje
    */
   public void incomingChat(User from, String msg);

   public void oponenteAbandono(boolean enJuego, User user);

   public void updatePoints(int puntos);

   public void disconnected();

   public void startGame(boolean start);

   public void newGame();

   public void finJuego(boolean victoria);
}
