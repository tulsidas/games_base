package common.messages.server;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.mina.common.ByteBuffer;

import common.ifaz.LobbyHandler;
import common.ifaz.LobbyMessage;
import common.messages.VariableLengthMessageAdapter;
import common.messages.chat.LobbyChatMessage;
import common.model.AbstractRoom;
import common.model.User;

/**
 * Mensaje del server con datos del lobby
 */
public class LobbyDataMessage extends VariableLengthMessageAdapter implements
        LobbyMessage {

    private int puntos;

    private Collection<AbstractRoom> rooms;

    private Collection<User> users;

    private Collection<LobbyChatMessage> lastChats;

    private String lobbyMsg;

    public LobbyDataMessage() {
    }

    public LobbyDataMessage(int puntos, Collection<AbstractRoom> rooms,
            Collection<User> users, Collection<LobbyChatMessage> lastChats,
            String lobbyMsg) {
        this.puntos = puntos;
        this.rooms = rooms;
        this.users = users;
        this.lastChats = lastChats;
        this.lobbyMsg = lobbyMsg;
    }

    public void execute(LobbyHandler lobby) {
        lobby.lobbyData(puntos, rooms, users, lastChats, lobbyMsg);
    }

    @Override
    public String toString() {
        return "LobbyData";
    }

    @Override
    public void decode(ByteBuffer buff) {
        puntos = buff.getInt();

        int numRooms = buff.getInt();
        rooms = new ArrayList<AbstractRoom>(numRooms);
        for (int i = 0; i < numRooms; i++) {
            rooms.add(AbstractRoom.decodeRoom(buff));
        }

        int numUsers = buff.getInt();
        users = new ArrayList<User>(numUsers);
        for (int i = 0; i < numUsers; i++) {
            User u = User.readFrom(buff);
            if (u != null) {
                users.add(u);
            }
        }

        int numChats = buff.getInt();
        lastChats = new ArrayList<LobbyChatMessage>(numChats);
        for (int i = 0; i < numChats; i++) {
            buff.skip(5); // salteo id + largo

            LobbyChatMessage lcm = new LobbyChatMessage();
            lcm.decode(buff);

            lastChats.add(lcm);
        }

        CharsetDecoder dec = Charset.forName("UTF-8").newDecoder();
        try {
            lobbyMsg = buff.getPrefixedString(dec);
        }
        catch (CharacterCodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ByteBuffer encodedContent() {
        CharsetEncoder enc = Charset.forName("UTF-8").newEncoder();

        ByteBuffer ret = ByteBuffer.allocate(512);
        ret.setAutoExpand(true);

        ret.putInt(puntos);

        ret.putInt(rooms.size());
        for (AbstractRoom room : rooms) {
            ret.put(room.encode());
        }

        ret.putInt(users.size());
        for (User user : users) {
            User.writeTo(user, ret);
        }

        ret.putInt(lastChats.size());
        for (LobbyChatMessage chat : lastChats) {
            ret.put(chat.encode());
        }

        try {
            ret.putPrefixedString(lobbyMsg, enc);
        }
        catch (CharacterCodingException e) {
            e.printStackTrace();
        }

        ret.flip();

        return ret;
    }

    @Override
    public byte getMessageId() {
        return 0x04;
    }
}
