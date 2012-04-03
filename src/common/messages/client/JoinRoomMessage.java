package common.messages.client;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;

import common.ifaz.BasicClientGameMessage;
import common.ifaz.BasicServerHandler;
import common.messages.FixedLengthMessageAdapter;

/**
 * Pedido de sumacion del usuario a una sala
 */
public class JoinRoomMessage extends FixedLengthMessageAdapter implements
      BasicClientGameMessage {

   private int roomId;

   public JoinRoomMessage() {
   }

   /**
    * @param room
    *           la sala a la que quiere sumarse
    */
   public JoinRoomMessage(int roomId) {
      this.roomId = roomId;
   }

   public void execute(IoSession session, BasicServerHandler salon) {
      salon.joinRoomRequest(session, roomId);
   }

   @Override
   public String toString() {
      return "Join Room " + roomId;
   }

   @Override
   public int getContentLength() {
      return 4;
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
      return 0x0C;
   }
}
