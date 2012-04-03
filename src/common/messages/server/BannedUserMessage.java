package common.messages.server;

import common.ifaz.LoginHandler;
import common.ifaz.LoginMessage;
import common.messages.FixedLengthMessageAdapter;

/**
 * Respuesta del servidor indicando que el usuario esta baneado
 */
public class BannedUserMessage extends FixedLengthMessageAdapter implements
        LoginMessage {

    public void execute(LoginHandler lh) {
        lh.banned();
    }

    @Override
    public String toString() {
        return "Banned User";
    }

    @Override
    public byte getMessageId() {
        return 0x08;
    }

   @Override
   public int getContentLength() {
      return 0;
   }
}
