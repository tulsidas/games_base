package common.messages.server;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import org.apache.mina.common.ByteBuffer;

import common.ifaz.LobbyHandler;
import common.ifaz.LobbyMessage;
import common.messages.VariableLengthMessageAdapter;

/**
 * Mensaje de respuesta a un comando OP
 */
public class OPMessage extends VariableLengthMessageAdapter implements
        LobbyMessage {

    private String msg;

    public OPMessage() {
    }

    public OPMessage(String msg) {
        this.msg = msg;
    }

    public void execute(LobbyHandler lobby) {
        lobby.opMessage(msg);
    }

    @Override
    public String toString() {
        return "OP: " + msg;
    }

    @Override
    public ByteBuffer encodedContent() {
        ByteBuffer ret = ByteBuffer.allocate(64);
        ret.setAutoExpand(true);

        CharsetEncoder enc = Charset.forName("UTF-8").newEncoder();
        try {
            ret.putPrefixedString(msg, enc);
        }
        catch (CharacterCodingException e) {
            e.printStackTrace();
        }

        return ret.flip();
    }

    @Override
    public void decode(ByteBuffer buff) {
        CharsetDecoder dec = Charset.forName("UTF-8").newDecoder();

        try {
            msg = buff.getPrefixedString(dec);
        }
        catch (CharacterCodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte getMessageId() {
        return 0x24;
    }
}
