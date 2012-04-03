package common.messages.server;

import org.apache.mina.common.ByteBuffer;

import common.ifaz.LobbyHandler;
import common.ifaz.LobbyMessage;
import common.messages.FixedLengthMessageAdapter;

/**
 * Mensaje del server que avisa que empezo el juego en la sala
 */
public class GameStartedMessage extends FixedLengthMessageAdapter implements
      LobbyMessage {

   private int roomId;

   public GameStartedMessage() {
   }

   public GameStartedMessage(int roomId) {
      this.roomId = roomId;
   }

   public void execute(LobbyHandler lobby) {
      lobby.gameStarted(roomId);
   }

   @Override
   public String toString() {
      return "Game Started (" + roomId + ")";
   }

   @Override
   public void decode(ByteBuffer buff) {
      roomId = buff.getInt();
   }

   @Override
   protected void encodeContent(ByteBuffer buff) {
      buff.putInt(roomId);
   }

   @Override
   public byte getMessageId() {
      return 0x21;
   }

   @Override
   public int getContentLength() {
      return 4;
   }
}
