package common.messages.chat;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import org.apache.mina.common.ByteBuffer;

import common.ifaz.BasicClientGameMessage;
import common.messages.VariableLengthMessageAdapter;
import common.model.User;

public abstract class AbstractChatMessage extends VariableLengthMessageAdapter
        implements BasicClientGameMessage {

    protected String msg;

    protected User from;

    public AbstractChatMessage() {
    }

    public AbstractChatMessage(String msg) {
        this.msg = msg;
    }

    public User getFrom() {
        return from;
    }

    public void setFrom(User from) {
        this.from = from;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public void decode(ByteBuffer buff) {
        setFrom(User.readFrom(buff));

        CharsetDecoder dec = Charset.forName("UTF-8").newDecoder();

        try {
            setMsg(buff.getPrefixedString(dec));
        }
        catch (CharacterCodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ByteBuffer encodedContent() {
        CharsetEncoder enc = Charset.forName("UTF-8").newEncoder();

        ByteBuffer ret = ByteBuffer.allocate(64);
        ret.setAutoExpand(true);

        User.writeTo(from, ret);

        try {
            ret.putPrefixedString(msg, enc);
        }
        catch (CharacterCodingException e) {
            e.printStackTrace();
        }

        ret.flip();

        return ret;
    }
}
