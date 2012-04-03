package common.messages;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoFilterAdapter;
import org.apache.mina.common.IoSession;

public class XORFilter extends IoFilterAdapter {
    @Override
    public void messageReceived(NextFilter nextFilter, IoSession session,
            Object message) throws Exception {

        // System.out.println("incoming");
        nextFilter.messageReceived(session, xor((ByteBuffer) message));
    }

    @Override
    public void filterWrite(NextFilter nextFilter, IoSession session,
            WriteRequest writeRequest) throws Exception {
        // System.out.println("outgoing");

        ByteBuffer inBuffer = (ByteBuffer) writeRequest.getMessage();

        nextFilter.filterWrite(session, new WriteRequest(xor(inBuffer),
                writeRequest.getFuture()));
    }

    private ByteBuffer xor(ByteBuffer in) {
        // System.out.println("\tin: " + in.getHexDump());

        ByteBuffer out = in.duplicate();
        int pad = 0xD2D2D2D2;

        while (in.hasRemaining()) {
            if (in.remaining() >= 4) {
                int xor = in.getInt() ^ pad;
                out.putInt(xor);
            }
            else {
                byte xor = (byte) (in.get() ^ 0xD2);
                out.put(xor);
            }
        }

        out = out.flip();
        // System.out.println("\tout: " + out.getHexDump());

        return out;
    }
}
