package common.messages.server;

import common.ifaz.LobbyHandler;
import common.ifaz.LobbyMessage;
import common.messages.FixedLengthMessageAdapter;

/**
 * Mensaje del server que avisa que: la sala a la que el usuario quiso unirse
 * esta llena || la sala a la que el usuario se unio esta completa y listo para
 * empezar el juego
 */
public class RoomFullMessage extends FixedLengthMessageAdapter implements
      LobbyMessage {

   public void execute(LobbyHandler lobby) {
      lobby.roomFull();
   }

   @Override
   public String toString() {
      return "Room full";
   }

   @Override
   public byte getMessageId() {
      return 0x13;
   }

   @Override
   public int getContentLength() {
      return 0;
   }
}
