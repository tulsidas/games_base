package common.messages;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class TaringaProtocolEncoder extends ProtocolEncoderAdapter {

    // constantes
    public static final byte NULL = 0x00;

    public static final byte NON_NULL = 0x11;

    public static final byte TRUE = (byte) 0xAA;

    public static final byte FALSE = (byte) 0xFF;

    @Override
    public void encode(IoSession session, Object obj, ProtocolEncoderOutput out)
            throws Exception {

        Message msg = (Message) obj;
        ByteBuffer enc = msg.encode();

        out.write(enc);
    }
}
