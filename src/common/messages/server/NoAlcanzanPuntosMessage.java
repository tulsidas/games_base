package common.messages.server;

import common.ifaz.LobbyHandler;
import common.ifaz.LobbyMessage;
import common.messages.FixedLengthMessageAdapter;

public class NoAlcanzanPuntosMessage extends FixedLengthMessageAdapter implements
        LobbyMessage {

    public void execute(LobbyHandler lobby) {
        lobby.noAlcanzanPuntos();
    }

    @Override
    public String toString() {
        return "No Alcanzan Puntos";
    }

    @Override
    public byte getMessageId() {
        return 0x0E;
    }

   @Override
   public int getContentLength() {
      return 0;
   }
}
