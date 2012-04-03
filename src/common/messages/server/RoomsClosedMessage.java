package common.messages.server;

import common.ifaz.LobbyHandler;
import common.ifaz.LobbyMessage;
import common.messages.FixedLengthMessageAdapter;

/**
 * Mensaje del server que avisa que no se pueden crear salas
 */
public class RoomsClosedMessage extends FixedLengthMessageAdapter implements
      LobbyMessage {

   public void execute(LobbyHandler lobby) {
      lobby.roomsClosed();
   }

   @Override
   public String toString() {
      return "Rooms closed";
   }

   @Override
   public byte getMessageId() {
      return 0x20;
   }

   @Override
   public int getContentLength() {
      return 0;
   }
}
