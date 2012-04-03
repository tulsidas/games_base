package common.messages.server;

import common.ifaz.LoginHandler;
import common.ifaz.LoginMessage;
import common.messages.FixedLengthMessageAdapter;

/**
 * Mensaje del server que indica que las credenciales son invalidas
 * 
 * @author Tulsi
 */
public class InvalidLoginMessage extends FixedLengthMessageAdapter implements
        LoginMessage {

    public void execute(LoginHandler server) {
    }

    @Override
    public byte getMessageId() {
        return 0x0B;
    }

   @Override
   public int getContentLength() {
      return 0;
   }
}