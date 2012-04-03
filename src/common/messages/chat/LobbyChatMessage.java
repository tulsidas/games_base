package common.messages.chat;

import org.apache.mina.common.IoSession;

import common.ifaz.BasicServerHandler;
import common.ifaz.LobbyHandler;
import common.ifaz.LobbyMessage;

/**
 * Mensaje de chat en el lobby
 */
public class LobbyChatMessage extends AbstractChatMessage implements
        LobbyMessage {

    public LobbyChatMessage() {
    }

    public LobbyChatMessage(String msg) {
        super(msg);
    }

    public void execute(IoSession session, BasicServerHandler salon) {
        salon.lobbyChat(session, getMsg());
    }

    public void execute(LobbyHandler client) {
        client.incomingChat(getFrom(), getMsg());
    }

    @Override
    public String toString() {
        return "LobbyChat: " + msg;
    }

    @Override
    public byte getMessageId() {
        return 0x05;
    }
}