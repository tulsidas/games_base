package common.messages;

import java.util.HashMap;
import java.util.Map;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import common.game.AbandonRoomMessage;
import common.game.OponentAbandonedMessage;
import common.game.PartnerAbandonedMessage;
import common.game.ProximoJuegoMessage;
import common.messages.chat.LobbyChatMessage;
import common.messages.chat.RoomChatMessage;
import common.messages.client.CreateRoomMessage;
import common.messages.client.JoinRoomMessage;
import common.messages.client.RequestLoginMessage;
import common.messages.server.BannedUserMessage;
import common.messages.server.DuplicatedLoginMessage;
import common.messages.server.GameStartedMessage;
import common.messages.server.InvalidLoginMessage;
import common.messages.server.KickedUserMessage;
import common.messages.server.LobbyDataMessage;
import common.messages.server.LobbyTopicMessage;
import common.messages.server.LoggedInMessage;
import common.messages.server.NoAlcanzanPuntosMessage;
import common.messages.server.OPMessage;
import common.messages.server.PingMessage;
import common.messages.server.RoomCreatedMessage;
import common.messages.server.RoomDroppedMessage;
import common.messages.server.RoomFullMessage;
import common.messages.server.RoomJoinedMessage;
import common.messages.server.RoomLeftMessage;
import common.messages.server.RoomsClosedMessage;
import common.messages.server.UpdatedPointsMessage;
import common.messages.server.UserDisconnectedMessage;
import common.messages.server.WrongVersionMessage;

public abstract class TaringaProtocolDecoder extends CumulativeProtocolDecoder {

    protected Map<Byte, Class< ? >> classes;

    private static final int MAX_SIZE = 64 * 1024;

    public TaringaProtocolDecoder() {
        classes = new HashMap<Byte, Class< ? >>();

        classes.put(new RequestLoginMessage().getMessageId(),
                RequestLoginMessage.class);
        classes
                .put(new LoggedInMessage().getMessageId(),
                        LoggedInMessage.class);
        classes.put(new LobbyJoinedMessage().getMessageId(),
                LobbyJoinedMessage.class);
        classes.put(new LobbyDataMessage().getMessageId(),
                LobbyDataMessage.class);
        classes.put(new LobbyChatMessage().getMessageId(),
                LobbyChatMessage.class);
        classes.put(new PingMessage().getMessageId(), PingMessage.class);
        classes.put(new AbandonRoomMessage().getMessageId(),
                AbandonRoomMessage.class);
        classes.put(new BannedUserMessage().getMessageId(),
                BannedUserMessage.class);
        classes.put(new CreateRoomMessage().getMessageId(),
                CreateRoomMessage.class);
        classes.put(new DuplicatedLoginMessage().getMessageId(),
                DuplicatedLoginMessage.class);
        classes.put(new InvalidLoginMessage().getMessageId(),
                InvalidLoginMessage.class);
        classes
                .put(new JoinRoomMessage().getMessageId(),
                        JoinRoomMessage.class);
        classes.put(new LobbyTopicMessage().getMessageId(),
                LobbyTopicMessage.class);
        classes.put(new NoAlcanzanPuntosMessage().getMessageId(),
                NoAlcanzanPuntosMessage.class);
        classes.put(new OponentAbandonedMessage().getMessageId(),
                OponentAbandonedMessage.class);
        classes.put(new ProximoJuegoMessage().getMessageId(),
                ProximoJuegoMessage.class);
        classes
                .put(new RoomChatMessage().getMessageId(),
                        RoomChatMessage.class);
        classes.put(new RoomDroppedMessage().getMessageId(),
                RoomDroppedMessage.class);
        classes
                .put(new RoomFullMessage().getMessageId(),
                        RoomFullMessage.class);
        classes.put(new UpdatedPointsMessage().getMessageId(),
                UpdatedPointsMessage.class);
        classes.put(new UserDisconnectedMessage().getMessageId(),
                UserDisconnectedMessage.class);
        classes.put(new RoomJoinedMessage().getMessageId(),
                RoomJoinedMessage.class);
        classes.put(new RoomCreatedMessage().getMessageId(),
                RoomCreatedMessage.class);
        classes.put(new PartnerAbandonedMessage().getMessageId(),
                PartnerAbandonedMessage.class);
        classes.put(new KickedUserMessage().getMessageId(),
                KickedUserMessage.class);
        classes.put(new RoomsClosedMessage().getMessageId(),
                RoomsClosedMessage.class);
        classes.put(new GameStartedMessage().getMessageId(),
                GameStartedMessage.class);
        classes
                .put(new RoomLeftMessage().getMessageId(),
                        RoomLeftMessage.class);
        classes.put(new WrongVersionMessage().getMessageId(),
                WrongVersionMessage.class);
        classes.put(new OPMessage().getMessageId(), OPMessage.class);
    }

    @Override
    protected boolean doDecode(IoSession session, ByteBuffer in,
            ProtocolDecoderOutput out) throws Exception {
        if (in.remaining() < 1 /* Byte.SIZE */) {
            return false;
        }
        else {
            int start = in.position();

            // llego un byte!
            // veo si es tamaño fijo o variable
            byte id = in.get();

            Class< ? > clazz = classes.get(id);

            if (clazz == null) {
                throw new IllegalArgumentException("Unknown msg with id: " + id);
            }

            Message msg = (Message) clazz.newInstance();

            if (msg.isFixedLength()) {
                FixedLengthMessage flm = (FixedLengthMessage) msg;

                if (in.remaining() < flm.getContentLength()) {

                    // rewind
                    in.position(start);

                    return false;
                }
                else {
                    flm.decode(in);
                    out.write(flm);

                    return true;
                }
            }
            else { // variable
                // necesito al menos 5 bytes: id + largo
                if (in.remaining() < 5) {
                    // rewind
                    in.position(start);

                    return false;
                }
                else {
                    if (in.prefixedDataAvailable(4, MAX_SIZE)) {
                        /* int largo = */in.getInt();

                        VariableLengthMessage vlm = (VariableLengthMessage) msg;

                        vlm.decode(in);

                        out.write(vlm);

                        return true;
                    }
                    else {
                        // rewind
                        in.position(start);

                        return false;
                    }
                }
            }
        }
    }
}
