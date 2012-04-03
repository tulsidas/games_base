package common.messages.server;

import org.apache.mina.common.ByteBuffer;

import common.ifaz.LobbyHandler;
import common.ifaz.LobbyMessage;
import common.messages.VariableLengthMessageAdapter;
import common.model.User;

/**
 * Mensaje del server de un usuario se unio a una sala
 * 
 * Mensaje del cliente que confirma que ingreso a la sala
 */
public class RoomLeftMessage extends VariableLengthMessageAdapter implements
      LobbyMessage {

   private User user;

   private int roomId;

   public RoomLeftMessage() {
   }

   public RoomLeftMessage(int roomId, User user) {
      this.user = user;
      this.roomId = roomId;
   }

   /**
    * LobbyMessage
    */
   public void execute(LobbyHandler lobby) {
      lobby.roomLeft(roomId, user);
   }

   @Override
   public String toString() {
      return "Room Left: " + user + " -> " + roomId;
   }

   @Override
   public ByteBuffer encodedContent() {
      ByteBuffer ret = ByteBuffer.allocate(32);
      ret.setAutoExpand(true);

      User.writeTo(user, ret);
      ret.putInt(roomId);

      return ret.flip();
   }

   @Override
   public void decode(ByteBuffer buff) {
      user = User.readFrom(buff);
      roomId = buff.getInt();
   }

   @Override
   public byte getMessageId() {
      return 0x22;
   }
}
