package common.model;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.mina.common.ByteBuffer;

import common.messages.TaringaProtocolEncoder;

public abstract class AbstractRoom implements Comparable<AbstractRoom> {

    private int id;

    private List<User> players;

    // los puntos apostados
    private int puntosApostados;

    private boolean started;

    public AbstractRoom() {
    }

    public AbstractRoom(int id, int puntosApostados, List<User> players) {
        this.id = id;
        this.puntosApostados = puntosApostados;

        this.players = Collections.synchronizedList(players);
    }

    public int getId() {
        return id;
    }

    public void addPlayer(User u) {
        players.add(u);
    }

    public void removePlayer(User u) {
        players.remove(u);
    }

    public List<User> getPlayers() {
        return new ArrayList<User>(players);
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean equals(Object obj) {
        if (obj instanceof AbstractRoom) {
            return ((AbstractRoom) obj).id == id;
        }
        return false;
    }

    public int hashCode() {
        return id;
    }

    public String toString() {
        StringBuilder ret = new StringBuilder();
        synchronized (players) {
            if (players.size() > 0) {
                ret.append(" (");
                for (int i = 0; i < players.size() - 1; i++) {
                    ret.append(players.get(i).toString() + ", ");
                }

                // el ultimo
                ret.append(players.get(players.size() - 1) + ")");
            }
        }

        return ret.toString();
    }

    public int getPuntosApostados() {
        return puntosApostados;
    }

    public ByteBuffer encode() {
        CharsetEncoder enc = Charset.forName("UTF-8").newEncoder();

        ByteBuffer ret = ByteBuffer.allocate(32);
        ret.setAutoExpand(true);

        // el nombre de la clase
        try {
            ret.putPrefixedString(getClass().getName(), enc);
        }
        catch (CharacterCodingException e) {
            e.printStackTrace();
        }

        ret.putInt(id);
        ret.put(started ? TaringaProtocolEncoder.TRUE
                : TaringaProtocolEncoder.FALSE);

        synchronized (players) {
            ret.putInt(players.size());
            for (User u : players) {
                User.writeTo(u, ret);
            }
        }

        ret.putInt(puntosApostados);

        return ret.flip();
    }

    public void decode(ByteBuffer buff) {
        this.id = buff.getInt();
        this.started = buff.get() == TaringaProtocolEncoder.TRUE;

        int size = buff.getInt();
        ArrayList<User> playerz = new ArrayList<User>(size);
        for (int i = 0; i < size; i++) {
            playerz.add(User.readFrom(buff));
        }

        this.players = Collections.synchronizedList(playerz);

        this.puntosApostados = buff.getInt();
    }

    /** dynamic reflected method */
    public static AbstractRoom decodeRoom(ByteBuffer buff) {
        CharsetDecoder dec = Charset.forName("UTF-8").newDecoder();

        try {
            String clazz = buff.getPrefixedString(dec);
            Class< ? > c = Class.forName(clazz);

            AbstractRoom room = (AbstractRoom) c.newInstance();

            room.decode(buff);

            return room;
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        catch (CharacterCodingException e) {
            e.printStackTrace();
        }
        catch (SecurityException e) {
            e.printStackTrace();
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        catch (InstantiationException e) {
            e.printStackTrace();
        }

        return null;
    }

    public abstract boolean isFull();

    public abstract String getDisplayText();
}
