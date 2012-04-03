package common.messages.client;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;

import common.ifaz.BasicClientGameMessage;
import common.ifaz.BasicServerHandler;
import common.messages.FixedLengthMessageAdapter;

/**
 * Pedido de un usuario de crear una sala
 */
public class CreateRoomMessage extends FixedLengthMessageAdapter implements
      BasicClientGameMessage {

   protected int puntos;

   public CreateRoomMessage() {
   }

   public CreateRoomMessage(int puntos) {
      this.puntos = puntos;
   }

   public void execute(IoSession session, BasicServerHandler salon) {
      salon.createRoom(session, puntos);
   }

   @Override
   public String toString() {
      return "Create Room (" + puntos + " pts)";
   }

   @Override
   public int getContentLength() {
      return 4;
   }

   @Override
   public void decode(ByteBuffer buff) {
      puntos = buff.getInt();
   }

   @Override
   protected void encodeContent(ByteBuffer buff) {
      buff.putInt(puntos);
   }

   @Override
   public byte getMessageId() {
      return 0x09;
   }
}
