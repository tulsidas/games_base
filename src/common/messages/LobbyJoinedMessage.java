package common.messages;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;

import common.ifaz.BasicClientGameMessage;
import common.ifaz.BasicServerHandler;
import common.ifaz.LobbyHandler;
import common.ifaz.LobbyMessage;
import common.model.User;

/**
 * Mensaje del server de un usuario se unio a una sala
 * 
 * Mensaje del usuario que termino de unirse a la sala
 */
public class LobbyJoinedMessage extends VariableLengthMessageAdapter implements
        LobbyMessage, BasicClientGameMessage {

    private User user;

    public LobbyJoinedMessage() {
    }

    public LobbyJoinedMessage(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void execute(LobbyHandler game) {
        game.lobbyJoined(user);
    }

    public void execute(IoSession session, BasicServerHandler salon) {
        salon.lobbyJoined(session);
    }

    @Override
    public String toString() {
        return "Lobby Joined: " + user;
    }

    @Override
    public ByteBuffer encodedContent() {
        ByteBuffer ret = ByteBuffer.allocate(32);
        ret.setAutoExpand(true);

        User.writeTo(user, ret);

        ret.flip();

        return ret;
    }

    @Override
    public void decode(ByteBuffer buff) {
        user = User.readFrom(buff);
    }

    @Override
    public byte getMessageId() {
        return 0x03;
    }
}
