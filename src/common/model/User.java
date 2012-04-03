package common.model;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.concurrent.locks.ReadWriteLock;

import org.apache.mina.common.ByteBuffer;

import common.messages.TaringaProtocolEncoder;

public class User {
    private String name;

    private int puntos;

    private boolean guest;

    private transient ReadWriteLock lock;

    private transient boolean idle = false;

    private transient FloodControl floodControl;

    public User(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    public ReadWriteLock getLock() {
        return lock;
    }

    public FloodControl getFloodControl() {
        return floodControl;
    }

    public boolean isIdle() {
        return idle;
    }

    public void setLock(ReadWriteLock lock) {
        this.lock = lock;
    }

    public void setFloodControl(FloodControl floodControl) {
        this.floodControl = floodControl;
    }

    public void setIdle(boolean idle) {
        this.idle = idle;
    }

    public boolean isGuest() {
        return guest;
    }

    public void setGuest(boolean guest) {
        this.guest = guest;
    }

    public String toString() {
        return name;
    }

    public boolean equals(Object obj) {
        if (obj instanceof User) {
            User u = (User) obj;
            return u.name.equalsIgnoreCase(name);
        }
        return false;
    }

    public int hashCode() {
        return name.toLowerCase().hashCode();
    }

    public static void writeTo(User user, ByteBuffer buff) {
        CharsetEncoder enc = Charset.forName("UTF-8").newEncoder();

        if (user != null) {
            // non-null marker
            buff.put(TaringaProtocolEncoder.NON_NULL);

            // username
            try {
                buff.putPrefixedString(user.getName(), enc);
            }
            catch (CharacterCodingException e) {
                e.printStackTrace();
            }

            // puntos
            buff.putInt(user.getPuntos());

            // guest
            buff.put(user.isGuest() ? TaringaProtocolEncoder.TRUE
                    : TaringaProtocolEncoder.FALSE);
        }
        else {
            // null marker
            buff.put(TaringaProtocolEncoder.NULL);
        }
    }

    public static User readFrom(ByteBuffer buff) {
        CharsetDecoder dec = Charset.forName("UTF-8").newDecoder();

        boolean isNull = buff.get() == TaringaProtocolEncoder.NULL;

        if (isNull) {
            return null;
        }
        else {
            String username = "?";
            try {
                username = buff.getPrefixedString(dec);
            }
            catch (CharacterCodingException e) {
                e.printStackTrace();
            }
            int puntos = buff.getInt();
            boolean guest = buff.get() == TaringaProtocolEncoder.TRUE;

            User usr = new User(username);
            usr.setPuntos(puntos);
            usr.setGuest(guest);

            return usr;
        }
    }
}
