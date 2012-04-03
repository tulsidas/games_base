package server;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.common.IoSession;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import common.messages.Message;
import common.model.AbstractRoom;
import common.model.User;

public abstract class AbstractServerRoom {

    private static AtomicInteger roomCounter = new AtomicInteger(0);

    private static final IoSession[] NO_SESSIONS = new IoSession[] {};

    private int id;

    private AtomicBoolean started;

    // los puntos apostados
    protected int puntosApostados;

    private AtomicBoolean enJuego;

    protected AbstractSaloon saloon;

    public AbstractServerRoom(AbstractSaloon saloon, int puntosApostados) {
        this.id = roomCounter.getAndIncrement();

        this.saloon = saloon;
        this.puntosApostados = Math.max(0, puntosApostados);

        this.enJuego = new AtomicBoolean(false);
        this.started = new AtomicBoolean(false);
    }

    public int getId() {
        return id;
    }

    public int getPuntosApostados() {
        return puntosApostados;
    }

    /** si estan jugando o en la etapa de elegir jugar otro */
    public boolean isEnJuego() {
        return enJuego.get() && isGameOn();
    }

    /** si estan en juego i.e. si al abandonar pierde puntos */
    public abstract boolean isGameOn();

    public void setEnJuego(boolean enJuego) {
        this.enJuego.set(enJuego);
    }

    public boolean isStarted() {
        return started.get();
    }

    public void setStarted(boolean started) {
        this.started.set(started);
    }

    @Override
    public String toString() {
        return getClass().getName() + ":" + id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractServerRoom other = (AbstractServerRoom) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

    public boolean hasUser(IoSession player) {
        return getUserSessions().contains(player);
    }

    public abstract Collection<IoSession> getUserSessions();

    public List<User> getUsers() {
        return Lists.newArrayList(Iterables.transform(getUserSessions(),
                new Function<IoSession, User>() {
                    @Override
                    public User apply(IoSession io) {
                        return saloon.getUser(io);
                    }
                }));
    }

    /**
     * pedido de unirse a la sala
     * 
     * @param session
     * @return si se unió o no
     */
    public abstract boolean join(IoSession session);

    /**
     * confirmación de estar dentro de la sala
     * 
     * @param session
     */
    public abstract void joined(IoSession session);

    /**
     * Envia el mensaje msg a todos los usuarios (y espectadores) excepto el
     * pasado
     * 
     * @param msg
     * @param session
     */
    protected void multicast(Message msg) {
        multicast(msg, NO_SESSIONS);
    }

    protected void multicast(Message msg, IoSession... excepciones) {
        multicast(msg, getUserSessions(), excepciones);
    }

    /**
     * Envia el mensaje msg a todos los usuarios (y espectadores) excepto el
     * pasado
     * 
     * @param msg
     * @param session
     */
    protected void multicast(Message msg, Collection<IoSession> sessions,
            IoSession... excepciones) {
        sessions.removeAll(Lists.newArrayList(excepciones));

        for (IoSession sess : sessions) {
            sess.write(msg);
        }
    }

    /**
     * Si la sala esta llena
     * 
     * @return
     */
    public abstract boolean isComplete();

    public abstract void abandon(IoSession session);

    public abstract void startGame();

    public abstract void proximoJuego(IoSession session, boolean acepta);

    public abstract AbstractRoom createRoom();

    /**
     * La cantidad minima para que la sala exista, si no hay, se baja la sala
     */
    public abstract int getMinimumPlayers();

    /**
     * La cantidad minima para poder seguir jugando, si enJuego() y hay menos
     * que este numero, se acaba el juego
     */
    public abstract int getMinimumPlayingPlayers();

}