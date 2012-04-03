package common.messages.server;

import common.ifaz.LoginHandler;
import common.ifaz.LoginMessage;
import common.messages.FixedLengthMessageAdapter;

/**
 * Respuesta del servidor indicando que el usuario esta pateado
 */
public class WrongVersionMessage extends FixedLengthMessageAdapter implements
      LoginMessage {

   public void execute(LoginHandler lh) {
      lh.wrongVersion();
   }

   @Override
   public String toString() {
      return "Wrong Version";
   }

   @Override
   public byte getMessageId() {
      return 0x23;
   }

   @Override
   public int getContentLength() {
      return 0;
   }
}
