package common.messages.server;

import common.ifaz.LoginHandler;
import common.ifaz.LoginMessage;
import common.messages.FixedLengthMessageAdapter;

/**
 * Respuesta del servidor indicando que el usuario esta pateado
 */
public class KickedUserMessage extends FixedLengthMessageAdapter implements
      LoginMessage {

   public void execute(LoginHandler lh) {
      lh.kicked();
   }

   @Override
   public String toString() {
      return "Kicked User";
   }

   @Override
   public byte getMessageId() {
      return 0x19;
   }

   @Override
   public int getContentLength() {
      return 0;
   }
}
