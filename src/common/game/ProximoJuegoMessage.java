package common.game;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;

import common.ifaz.BasicClientGameMessage;
import common.ifaz.BasicServerHandler;
import common.messages.FixedLengthMessageAdapter;
import common.messages.TaringaProtocolEncoder;

public class ProximoJuegoMessage extends FixedLengthMessageAdapter implements
      BasicClientGameMessage {

   private boolean acepta;

   public ProximoJuegoMessage() {
   }

   public ProximoJuegoMessage(boolean acepta) {
      this.acepta = acepta;
   }

   public void execute(IoSession session, BasicServerHandler server) {
      server.proximoJuego(session, acepta);
   }

   @Override
   public String toString() {
      return "Proximo Juego: " + acepta;
   }

   @Override
   public int getContentLength() {
      // booleano si/no
      return 1;
   }

   @Override
   public void decode(ByteBuffer buff) {
      acepta = buff.get() == TaringaProtocolEncoder.TRUE;
   }

   @Override
   public void encodeContent(ByteBuffer buff) {
      buff.put(acepta ? TaringaProtocolEncoder.TRUE
            : TaringaProtocolEncoder.FALSE);
   }

   @Override
   public byte getMessageId() {
      return 0x10;
   }
}