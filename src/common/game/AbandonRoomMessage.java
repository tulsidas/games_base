package common.game;

import org.apache.mina.common.IoSession;

import common.ifaz.BasicClientGameMessage;
import common.ifaz.BasicServerHandler;
import common.messages.FixedLengthMessageAdapter;

public class AbandonRoomMessage extends FixedLengthMessageAdapter implements
        BasicClientGameMessage {

    public void execute(IoSession session, BasicServerHandler salon) {
        salon.removePlayerFromRoom(session);
    }

    @Override
    public String toString() {
        return "Abandon Room";
    }

    @Override
    public byte getMessageId() {
        return 0x07;
    }

    @Override
    public int getContentLength() {
        return 0;
    }
}
