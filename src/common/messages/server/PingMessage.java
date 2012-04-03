package common.messages.server;

import org.apache.mina.common.IoSession;

import common.ifaz.BasicClientGameMessage;
import common.ifaz.BasicServerHandler;
import common.messages.FixedLengthMessageAdapter;

/**
 * Respuesta del servidor indicando que el usuario ya esta logueado
 */
public class PingMessage extends FixedLengthMessageAdapter implements
        BasicClientGameMessage {

    public void execute(IoSession session, BasicServerHandler salon) {
        salon.ping(session);
    }

    @Override
    public String toString() {
        return "PING";
    }

    @Override
    public byte getMessageId() {
        return 0x06;
    }

    @Override
    public int getContentLength() {
        return 0;
    }
}