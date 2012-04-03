package common.messages.server;

import org.apache.mina.common.ByteBuffer;

import common.ifaz.LobbyHandler;
import common.ifaz.LobbyMessage;
import common.messages.FixedLengthMessageAdapter;

/**
 * Mensaje del server que informa que se elimino una sala
 * 
 */
public class RoomDroppedMessage extends FixedLengthMessageAdapter implements
      LobbyMessage {

   private int roomId;

   public RoomDroppedMessage() {
   }

   /**
    * @param room
    *           la sala creada (contiene adentro al usuario)
    */
   public RoomDroppedMessage(int roomId) {
      this.roomId = roomId;
   }

   public void execute(LobbyHandler lobby) {
      lobby.roomDropped(roomId);
   }

   @Override
   public String toString() {
      return "Room " + roomId + " dropped";
   }

   @Override
   public int getContentLength() {
      // nada extra
      return 4;
   }

   @Override
   public void decode(ByteBuffer buff) {
      // nada que decodificar
      roomId = buff.getInt();
   }

   @Override
   protected void encodeContent(ByteBuffer buff) {
      buff.putInt(roomId);
   }

   @Override
   public byte getMessageId() {
      return 0x12;
   }
}
