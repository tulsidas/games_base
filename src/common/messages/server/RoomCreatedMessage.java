package common.messages.server;

import org.apache.mina.common.ByteBuffer;

import common.ifaz.LobbyHandler;
import common.ifaz.LobbyMessage;
import common.messages.VariableLengthMessageAdapter;
import common.model.AbstractRoom;

/**
 * Mensaje del server que informa que se creo una nueva sala
 * 
 */
public class RoomCreatedMessage extends VariableLengthMessageAdapter implements
        LobbyMessage {

    private AbstractRoom room;

    public RoomCreatedMessage() {
    }

    /**
     * @param room
     *            la sala creada (contiene adentro al usuario)
     */
    public RoomCreatedMessage(AbstractRoom room) {
        this.room = room;
    }

    public AbstractRoom getRoom() {
        return room;
    }

    public void execute(LobbyHandler lobby) {
        lobby.roomCreated(room);
    }

    @Override
    public String toString() {
        return "Room created";
    }

    @Override
    public ByteBuffer encodedContent() {
        ByteBuffer ret = ByteBuffer.allocate(64);
        ret.setAutoExpand(true);

        ret.put(room.encode());

        ret.flip();

        return ret;
    }

    @Override
    public void decode(ByteBuffer buff) {
        room = AbstractRoom.decodeRoom(buff);
    }

    @Override
    public byte getMessageId() {
        return 0x17;
    }
}
