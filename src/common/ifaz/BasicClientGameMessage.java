package common.ifaz;

import org.apache.mina.common.IoSession;

public interface BasicClientGameMessage {
    // mensaje del cliente se ejecuta en el server
    public void execute(IoSession session, BasicServerHandler salon);
}
