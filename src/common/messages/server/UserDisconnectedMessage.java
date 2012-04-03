package common.messages.server;

import org.apache.mina.common.ByteBuffer;

import common.ifaz.LobbyHandler;
import common.ifaz.LobbyMessage;
import common.messages.VariableLengthMessageAdapter;
import common.model.User;

public class UserDisconnectedMessage extends VariableLengthMessageAdapter implements
        LobbyMessage {

    private User user;

    public UserDisconnectedMessage() {
    }

    public UserDisconnectedMessage(User user) {
        super();
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void execute(LobbyHandler game) {
        game.userDisconnected(user);
    }

    @Override
    public String toString() {
        return "User Disconected " + (user != null ? user.getName() : "?");
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
    public byte getMessageId() {
        return 0x15;
    }

    public void decode(ByteBuffer buff) {
        user = User.readFrom(buff);
    }
}
