package common.game;

import org.apache.mina.common.ByteBuffer;

import common.messages.FixedLengthMessageAdapter;
import common.messages.TaringaProtocolEncoder;

public class PartnerAbandonedMessage extends FixedLengthMessageAdapter
/* implements BasicGameMessage */{

   private boolean enJuego;

   public PartnerAbandonedMessage() {
   }

   public PartnerAbandonedMessage(boolean enJuego) {
      this.enJuego = enJuego;
   }

   // public void execute(BasicGameHandler game) {
   // game.oponenteAbandono(enJuego);
   // }

   @Override
   public String toString() {
      return "Pareja Abandono";
   }

   @Override
   public int getContentLength() {
      // bool
      return 1;
   }

   @Override
   public void decode(ByteBuffer buff) {
      enJuego = buff.get() == TaringaProtocolEncoder.TRUE;
   }

   @Override
   public void encodeContent(ByteBuffer buff) {
      buff.put(enJuego ? TaringaProtocolEncoder.TRUE
            : TaringaProtocolEncoder.FALSE);
   }

   @Override
   public byte getMessageId() {
      return (byte) 0x18;
   }
}
