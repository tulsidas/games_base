package common.messages.server;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;

import common.ifaz.BasicClientGameMessage;
import common.ifaz.BasicGameHandler;
import common.ifaz.BasicGameMessage;
import common.ifaz.BasicServerHandler;
import common.ifaz.LobbyHandler;
import common.ifaz.LobbyMessage;
import common.messages.TaringaProtocolEncoder;
import common.messages.VariableLengthMessageAdapter;
import common.model.AbstractRoom;
import common.model.User;

/**
 * Mensaje del server de un usuario se unio a una sala
 * 
 * Mensaje del cliente que confirma que ingreso a la sala
 */
public class RoomJoinedMessage extends VariableLengthMessageAdapter implements
        LobbyMessage, BasicGameMessage, BasicClientGameMessage {

    private User user;

    private AbstractRoom room;

    public RoomJoinedMessage() {
    }

    public RoomJoinedMessage(AbstractRoom room, User user) {
        this.user = user;
        this.room = room;
    }

    /**
     * LobbyMessage
     */
    public void execute(LobbyHandler lobby) {
        lobby.roomJoined(room, user);
    }

    /**
     * GameMessage
     */
    public void execute(BasicGameHandler game) {
        game.roomJoined(room, user);
    }

    /**
     * ClientMessage
     */
    public void execute(IoSession session, BasicServerHandler salon) {
        salon.roomJoined(session);
    }

    @Override
    public String toString() {
        return "Room Joined: " + user + " -> " + room;
    }

    @Override
    public ByteBuffer encodedContent() {
        ByteBuffer ret = ByteBuffer.allocate(32);
        ret.setAutoExpand(true);

        User.writeTo(user, ret);
        if (room != null) {
            ret.put(TaringaProtocolEncoder.NON_NULL);
            ret.put(room.encode());
        }
        else {
            ret.put(TaringaProtocolEncoder.NULL);
        }

        return ret.flip();
    }

    @Override
    public void decode(ByteBuffer buff) {
        user = User.readFrom(buff);
        if (buff.get() == TaringaProtocolEncoder.NON_NULL) {
            room = AbstractRoom.decodeRoom(buff);
        }
    }

    @Override
    public byte getMessageId() {
        return 0x16;
    }
}
