package common.messages;

import org.apache.mina.common.ByteBuffer;

public interface Message {
    public abstract byte getMessageId();

    public abstract ByteBuffer encode();

    public abstract void decode(ByteBuffer buff);

    public abstract boolean isFixedLength();
}
