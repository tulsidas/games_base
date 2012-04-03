package common.messages.server;

import common.ifaz.LoginHandler;
import common.ifaz.LoginMessage;
import common.messages.FixedLengthMessageAdapter;

/**
 * Respuesta del servidor indicando que el usuario ya esta logueado
 */
public class DuplicatedLoginMessage extends FixedLengthMessageAdapter implements
        LoginMessage {

    public void execute(LoginHandler lh) {
        lh.duplicatedLogin();
    }

    @Override
    public String toString() {
        return "Duplicated Login";
    }

    @Override
    public byte getMessageId() {
        return 0x0A;
    }

   @Override
   public int getContentLength() {
      return 0;
   }
}
