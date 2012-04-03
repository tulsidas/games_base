package common.game;

import org.apache.mina.common.ByteBuffer;

import common.ifaz.BasicGameHandler;
import common.ifaz.BasicGameMessage;
import common.messages.TaringaProtocolEncoder;
import common.messages.VariableLengthMessageAdapter;
import common.model.User;

public class OponentAbandonedMessage extends VariableLengthMessageAdapter
      implements BasicGameMessage {

   private boolean enJuego;

   private User user;

   public OponentAbandonedMessage() {
   }

   public OponentAbandonedMessage(boolean enJuego, User user) {
      this.enJuego = enJuego;
      this.user = user;
   }

   public void execute(BasicGameHandler game) {
      game.oponenteAbandono(enJuego, user);
   }

   @Override
   public String toString() {
      return "Oponente Abandono";
   }

   @Override
   public void decode(ByteBuffer buff) {
      enJuego = buff.get() == TaringaProtocolEncoder.TRUE;
      user = User.readFrom(buff);
   }

   @Override
   public ByteBuffer encodedContent() {
      ByteBuffer ret = ByteBuffer.allocate(32);
      ret.setAutoExpand(true);

      ret.put(enJuego ? TaringaProtocolEncoder.TRUE
            : TaringaProtocolEncoder.FALSE);

      User.writeTo(user, ret);

      ret.flip();

      return ret;
   }

   @Override
   public byte getMessageId() {
      return 0x0F;
   }
}
