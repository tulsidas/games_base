package common.messages.server;

import org.apache.mina.common.ByteBuffer;

import common.ifaz.LoginHandler;
import common.ifaz.LoginMessage;
import common.messages.VariableLengthMessageAdapter;
import common.model.User;

/**
 * Respuesta del servidor indicando que el usuario se ha sumado al juego
 */
public class LoggedInMessage extends VariableLengthMessageAdapter implements
        LoginMessage {

    private User user;

    public LoggedInMessage() {
    }

    public LoggedInMessage(User user) {
        this.user = user;
    }

    public void execute(LoginHandler lh) {
        lh.loggedIn(user);
    }

    @Override
    public String toString() {
        return "Logged In";
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
        return 0x02;
    }
}
