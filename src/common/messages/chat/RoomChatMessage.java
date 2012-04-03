package common.messages.chat;

import org.apache.mina.common.IoSession;

import common.ifaz.BasicGameHandler;
import common.ifaz.BasicGameMessage;
import common.ifaz.BasicServerHandler;

/**
 * mensaje de chat dentro de una sala
 */
public class RoomChatMessage extends AbstractChatMessage implements
        BasicGameMessage {

    public RoomChatMessage() {
    }

    /**
     * @param msg
     *            el mensaje
     */
    public RoomChatMessage(String msg) {
        super(msg);
    }

    public void execute(IoSession session, BasicServerHandler salon) {
        salon.roomChat(session, getMsg());
    }

    public void execute(BasicGameHandler game) {
        game.incomingChat(getFrom(), getMsg());
    }

    @Override
    public String toString() {
        return "RoomChat: " + msg;
    }

    @Override
    public byte getMessageId() {
        return 0x11;
    }
}
