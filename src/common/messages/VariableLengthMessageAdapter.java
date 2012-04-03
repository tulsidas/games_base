package common.messages;

import org.apache.mina.common.ByteBuffer;

public abstract class VariableLengthMessageAdapter implements
        VariableLengthMessage {

    @Override
    public boolean isFixedLength() {
        return false;
    }

    public abstract ByteBuffer encodedContent();

    @Override
    public final ByteBuffer encode() {
        // creo otro ByteBuffer, con el primer byte de ID y el largo
        ByteBuffer enc = encodedContent();

        ByteBuffer buff = ByteBuffer.allocate(enc.limit() + 5);

        // ID
        buff.put(getMessageId());

        // largo variable
        buff.putInt(enc.limit());

        // el mensaje original
        buff.put(enc);

        buff.flip();

        return buff;
    }
}
