package common.ifaz;

import org.apache.mina.common.IoSession;

public interface ClientLoginMessage {
    // mensaje de login del cliente que se ejecuta en el server
    public void execute(IoSession session, ClientLoginHandler clh);
}
