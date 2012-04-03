package server;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.mina.common.IoSession;

import common.game.OponentAbandonedMessage;

public abstract class TwoPlayersServerRoom extends AbstractServerRoom {

    protected IoSession player1, player2;

    private boolean ready1, ready2;

    private boolean abandon1, abandon2; // TODO horrible

    private boolean proxJuego1, proxJuego2;

    private Object playerLock;

    public TwoPlayersServerRoom(AbstractSaloon saloon, IoSession session,
            int puntosApostados) {
        super(saloon, puntosApostados);
        this.player1 = session;

        this.ready1 = false;
        this.ready2 = false;

        playerLock = new Object();
    }

    @Override
    public Collection<IoSession> getUserSessions() {
        Collection<IoSession> ret = new ArrayList<IoSession>(2);
        synchronized (playerLock) {
            if (player1 != null) {
                ret.add(player1);
            }
            if (player2 != null) {
                ret.add(player2);
            }
        }
        return ret;
    }

    public IoSession getOtherPlayer(IoSession session) {
        synchronized (playerLock) {
            return session == player1 ? player2 : player1;
        }
    }

    @Override
    public boolean hasUser(IoSession player) {
        synchronized (playerLock) {
            return player1 == player || player2 == player;
        }
    }

    @Override
    public boolean isComplete() {
        synchronized (playerLock) {
            return ready1 && ready2 && player1 != null && player2 != null;
        }
    }

    /**
     * Intenta unir al usuario a la sala
     * 
     * @param session
     *            el usuario
     * @return
     */
    @Override
    public boolean join(IoSession session) {
        synchronized (playerLock) {
            if (player1 == null) {
                player1 = session;
                return true;
            }
            else if (player2 == null) {
                player2 = session;
                return true;
            }
            return false;
        }
    }

    @Override
    public void joined(IoSession session) {
        synchronized (playerLock) {
            if (session == player1) {
                ready1 = true;
            }
            else if (session == player2) {
                ready2 = true;
            }
        }
    }

    public boolean isReady(IoSession session) {
        if (session == player1) {
            return ready1;
        }
        else if (session == player2) {
            return ready2;
        }

        return false;
    }

    @Override
    public void abandon(IoSession session) {
        synchronized (playerLock) {
            if (session.equals(player1)) {
                // aviso
                if (player2 != null) {
                    player2.write(new OponentAbandonedMessage(isEnJuego(),
                            saloon.getUser(player1)));

                    // pierde los puntos si estaban jugando
                    if (isEnJuego()) {
                        saloon
                                .transferPoints(player2, player1,
                                        puntosApostados);
                    }
                }

                // nulifico
                player1 = null;
            }
            else if (session.equals(player2)) {
                // aviso
                if (player1 != null) {
                    player1.write(new OponentAbandonedMessage(isEnJuego(),
                            saloon.getUser(player2)));

                    // pierde los puntos si estaban jugando
                    if (isEnJuego()) {
                        saloon
                                .transferPoints(player1, player2,
                                        puntosApostados);
                    }
                }

                // nulifico
                player2 = null;
            }
        }
    }

    @Override
    public void proximoJuego(IoSession session, boolean acepta) {
        synchronized (playerLock) {
            if (!acepta) {
                // no acepto, el otro al lobby
                multicast(new OponentAbandonedMessage(isEnJuego(), saloon
                        .getUser(getOtherPlayer(session))), session);
            }
            else {
                if (session == player1) {
                    proxJuego1 = acepta;
                }
                else {
                    proxJuego2 = acepta;
                }
            }

            if (proxJuego1 && proxJuego2) {
                // reseteo variables
                proxJuego1 = proxJuego2 = false;

                // otro partido
                startNuevoJuego();
            }
        }
    }

    @Override
    public int getMinimumPlayers() {
        return 2;
    }

    @Override
    public int getMinimumPlayingPlayers() {
        return 2;
    }

    /**
     * metodo invocado cuando los jugadores quieren comenzar un nuevo juego
     */
    public abstract void startNuevoJuego();

    /**
     * el user abandono antes de que empiece el juego (para partidas de torneo)
     * 
     * @param sess
     */
    public void setAbandoned(IoSession session) {
        synchronized (playerLock) {
            if (session == player1) {
                abandon1 = true;
            }
            else if (session == player2) {
                abandon2 = true;
            }
        }
    }

    public boolean isAbandoned(IoSession session) {
        if (session == player1) {
            return abandon1;
        }
        else if (session == player2) {
            return abandon2;
        }

        return false;
    }
}
