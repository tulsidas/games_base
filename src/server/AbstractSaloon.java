package server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import server.db.RedisManager;

import com.google.common.base.Predicate;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import common.ifaz.BasicServerHandler;
import common.ifaz.POSTHandler;
import common.messages.LobbyJoinedMessage;
import common.messages.Message;
import common.messages.chat.LobbyChatMessage;
import common.messages.chat.RoomChatMessage;
import common.messages.server.DuplicatedLoginMessage;
import common.messages.server.GameStartedMessage;
import common.messages.server.InvalidLoginMessage;
import common.messages.server.LobbyDataMessage;
import common.messages.server.LobbyTopicMessage;
import common.messages.server.LoggedInMessage;
import common.messages.server.NoAlcanzanPuntosMessage;
import common.messages.server.OPMessage;
import common.messages.server.RoomCreatedMessage;
import common.messages.server.RoomDroppedMessage;
import common.messages.server.RoomFullMessage;
import common.messages.server.RoomJoinedMessage;
import common.messages.server.RoomLeftMessage;
import common.messages.server.RoomsClosedMessage;
import common.messages.server.UserDisconnectedMessage;
import common.model.AbstractRoom;
import common.model.FloodControl;
import common.model.User;
import common.util.Randomin;

public abstract class AbstractSaloon implements BasicServerHandler {

    private Logger log = LoggerFactory.getLogger(getClass());

    private int id;

    protected POSTHandler poster;

    /** los jugadores logueados */
    protected BiMap<IoSession, User> users;

    /** los jugadores que estan en el lobby */
    protected Set<IoSession> lobby;

    /** las salas */
    protected Set<AbstractServerRoom> rooms; // TODO ver de pasar a MAP

    /** las ultimas lineas de chat */
    private List<LobbyChatMessage> lastChats;

    private boolean acceptingNewRooms;

    /** el mensaje del lobby */
    private String lobbyMessage;

    public AbstractSaloon(int id, POSTHandler poster) {
        this.id = id;
        this.poster = poster;

        lobby = Collections.synchronizedSet(new HashSet<IoSession>());
        rooms = Collections.synchronizedSet(new HashSet<AbstractServerRoom>());

        HashBiMap<IoSession, User> hbm = HashBiMap.create(100);
        users = Maps.synchronizedBiMap(hbm);
        lastChats = Collections
                .synchronizedList(new LinkedList<LobbyChatMessage>());
        acceptingNewRooms = true;
        lobbyMessage = "";
    }

    boolean disconnectPlayer(String name) {
        User u = new User(name);
        if (users.inverse().containsKey(u)) {

            IoSession sess = users.inverse().get(u);

            abandonGame(sess);
            return true;
        }

        return false;
    }

    // public InetAddress getIP(String name) {
    // User u = new User(name);
    // Map<User, IoSession> reverseMap = users.inverse();
    // if (reverseMap.containsKey(u)) {
    // InetSocketAddress iaddr = (InetSocketAddress) reverseMap.get(u)
    // .getRemoteAddress();
    // return iaddr.getAddress();
    // }
    //
    // return null;
    // }

    String checkClosedSessions() {
        StringBuilder ret = new StringBuilder("");
        synchronized (users) {
            for (Map.Entry<IoSession, User> entry : users.entrySet()) {
                if (entry.getKey().isClosing()) {
                    ret.append(entry.getValue() + " isClosing()\n");
                }
                if (!entry.getKey().isConnected()) {
                    ret.append(entry.getValue() + " !isConnected()\n");
                }
            }
        }

        return ret.toString();
    }

    /**
     * @param s1
     *            el ganador
     * @param s2
     *            el perdedor
     * @param puntos
     *            los puntos en juego
     * @return [p1, p2] los puntajes actualizados de s1 y s2
     */
    public int[] transferPoints(User u1, User u2, int puntos) {
        if (u1 == null || u1.isGuest() || u2 == null || u2.isGuest()) {
            int p1 = u1 == null ? 0 : u1.getPuntos();
            int p2 = u2 == null ? 0 : u2.getPuntos();

            // alguno era invitado, no hay transferencia de puntos
            return new int[] { p1, p2 };
        }
        else {
            return poster.transferPoints(u1, u2, puntos);
        }
    }

    /**
     * @param s1
     *            el ganador
     * @param s2
     *            el perdedor
     * @param puntos
     *            los puntos en juego
     * @return [p1, p2] los puntajes actualizados de s1 y s2
     */
    public int[] transferPoints(IoSession s1, IoSession s2, int puntos) {
        User u1 = users.get(s1);
        User u2 = users.get(s2);

        return transferPoints(u1, u2, puntos);
    }

    boolean hasUser(IoSession player) {
        return getUser(player) != null;
    }

    public User getUser(IoSession player) {
        return users.get(player);
    }

    /**
     * 
     * @param roomId
     * @return el Room de id roomId
     */
    private AbstractServerRoom getRoom(int roomId) {
        synchronized (rooms) {
            for (AbstractServerRoom r : rooms) {
                if (r.getId() == roomId) {
                    return r;
                }
            }
        }
        return null;
    }

    /**
     * @param player
     * @return el Room donde esta player
     */
    protected AbstractServerRoom getRoom(IoSession player) {
        synchronized (rooms) {
            for (AbstractServerRoom r : rooms) {
                if (r.hasUser(player)) {
                    return r;
                }
            }
        }

        return null;
    }

    /**
     * Envia el mensaje a todo el lobby
     * 
     * @param msg
     *            el mensaje a enviar
     */
    protected void broadcastLobby(Message msg) {
        broadcastLobby(msg, null);
    }

    /**
     * Envia el mensaje a todo el lobby excepto la sesion indicada (generalmente
     * el que envio el mensaje)
     * 
     * @param msg
     *            el mensaje a enviar
     * @param excepto
     *            la sesion a exceptuar del envio
     */
    protected void broadcastLobby(Message msg, IoSession excepto) {
        synchronized (lobby) {
            for (IoSession sess : lobby) {
                if (sess != excepto) {
                    sess.write(msg);
                }
            }
        }
    }

    public boolean logueado(final String name) {
        synchronized (users) {
            return Iterables.any(users.values(), new Predicate<User>() {
                public boolean apply(User user) {
                    return user.getName().equals(name);
                }
            });
        }
    }

    public void setAcceptingNewRooms(boolean acceptingNewRooms) {
        this.acceptingNewRooms = acceptingNewRooms;
    }

    public boolean isAcceptingNewRooms() {
        return acceptingNewRooms;
    }

    // /////////////////
    // ServerHandler //
    // /////////////////
    public void login(IoSession session, User user, String hash) {
        if (hash != null) {
            // registrado
            int fichas = poster.validateUser(user.getName(), hash);

            if (fichas < 0) {
                session.write(new InvalidLoginMessage());
            }
            else {
                user.setPuntos(fichas);
                user.setLock(new ReentrantReadWriteLock());
                user.setFloodControl(new FloodControl());
                user.setGuest(false);

                if (RedisManager.login(user)) {
                    users.put(session, user);

                    log.debug(user.getName() + " logged in");
                    session.write(new LoggedInMessage(user));
                }
                else {
                    log.debug("duplicated login " + user.getName());
                    session.write(new DuplicatedLoginMessage());
                }
            }
        }
        else {
            // invitado
            if (users.containsKey(session)) {
                // esta logueado 2 veces
                session.write(new DuplicatedLoginMessage());
                log.debug("duplicated login " + user.getName());

                return;
            }

            // le agrego el prefijo *
            user = new User("*" + user.getName());

            while (logueado(user.getName())) {
                // obtengo un nombre que no este en uso
                user = new User(user.getName()
                        + ((int) (Math.random() * 9) + 1));
            }

            user.setPuntos(0);
            user.setGuest(true);
            user.setLock(new ReentrantReadWriteLock());
            // al pedo porque no chatea en el lobby
            // user.setFloodControl(new FloodControl());

            users.put(session, user);

            log.debug(user.getName() + " logged in");

            // respondo
            session.write(new LoggedInMessage(user));
        }
    }

    public void lobbyJoined(IoSession session) {
        // esta en el lobby
        lobby.add(session);

        Collection<AbstractRoom> salas = new ArrayList<AbstractRoom>();
        synchronized (rooms) {
            for (AbstractServerRoom roomImp : rooms) {
                AbstractRoom room = roomImp.createRoom();
                room.setStarted(roomImp.isStarted());

                salas.add(room);
            }
        }

        // envio info del lobby
        ArrayList<User> lobbyUsers = new ArrayList<User>(lobby.size());

        synchronized (lobby) {
            for (IoSession sess : lobby) {
                lobbyUsers.add(users.get(sess));
            }
        }

        User u = users.get(session);
        u.getLock().readLock().lock();
        session.write(new LobbyDataMessage(u.getPuntos(), salas, lobbyUsers,
                lastChats, lobbyMessage));
        u.getLock().readLock().unlock();

        // aviso al resto
        broadcastLobby(new LobbyJoinedMessage(users.get(session)), session);
    }

    public void setLobbyMessage(String msg) {
        this.lobbyMessage = msg;

        // aviso a los que estan en el lobby
        synchronized (lobby) {
            for (IoSession sess : lobby) {
                sess.write(new LobbyTopicMessage(lobbyMessage));
            }
        }
    }

    public AbstractServerRoom createRoom(IoSession session, int puntos,
            AbstractServerRoom rImp) {
        User user = users.get(session);

        if (acceptingNewRooms) {
            // veo si tiene puntos disponibles
            if (puntos >= 0 && user.getPuntos() >= puntos) {
                // lo saco del lobby
                lobby.remove(session);

                rooms.add(rImp);

                // Room description
                AbstractRoom room = rImp.createRoom();

                // aviso que creo y se unio (en la izq)
                session.write(new RoomJoinedMessage(room, user));

                // broadcast al resto del lobby
                broadcastLobby(new RoomCreatedMessage(room), session);

                return rImp;
            }
            else {
                // no alcanzan los puntos
                session.write(new NoAlcanzanPuntosMessage());
            }
        }
        else {
            // no se pueden crear salas
            session.write(new RoomsClosedMessage());
        }

        return null;
    }

    public void joinRoomRequest(IoSession session, int roomId) {
        AbstractServerRoom room = getRoom(roomId);
        User u = users.get(session);

        if (room != null) {
            synchronized (room) {
                if (u.getPuntos() < room.getPuntosApostados()) {
                    session.write(new RoomFullMessage());
                }
                else if (room.join(session)) {
                    // se unio ok

                    // lo saco del lobby
                    lobby.remove(session);

                    AbstractRoom clientRoom = room.createRoom();

                    RoomJoinedMessage rta = new RoomJoinedMessage(clientRoom, u);

                    // respondo
                    session.write(rta);

                    // aviso al lobby
                    broadcastLobby(rta, session);

                    // aviso a la sala
                    room.multicast(rta, session);
                }
                else {
                    // lleno
                    session.write(new RoomFullMessage());
                }
            }
        }
        else { // no existe el room
            session.write(new RoomFullMessage());
        }
    }

    public void roomJoined(IoSession session) {
        // si la sala esta completa, damos inicio al juego
        AbstractServerRoom room = getRoom(session);
        if (room != null) {
            synchronized (room) {
                log.debug(getUser(session) + " joined room " + room.getId());
                room.joined(session);
                if (room.isComplete() && !room.isStarted()) {
                    room.setStarted(true);
                    room.startGame();

                    // aviso al lobby que empezo el juego en esta sala
                    broadcastLobby(new GameStartedMessage(room.getId()));
                }
            }
        }
    }

    public void abandonGame(IoSession session) {
        User user = getUser(session);

        // fuera del lobby (si esta)
        lobby.remove(session);

        // fuera de la sala (si esta)
        removePlayerFromRoom(session);

        // lo saco del map
        users.remove(session);

        // hook
        userDisconnected(session);

        if (user != null) {
            String name = user.getName();
            log.debug("abandonGame: " + session.getRemoteAddress() + " ("
                    + name + ")");

            RedisManager.logout(user);

            // aviso al lobby que se fue
            broadcastLobby(new UserDisconnectedMessage(user));

            // desconecto
            session.close();
        }
    }

    /**
     * gancho para que subclases llamen
     * 
     * @param user
     */
    protected void userDisconnected(IoSession session) {
    }

    public synchronized void removePlayerFromRoom(final IoSession session) {
        AbstractServerRoom theRoom = null;

        // of the room (if any)
        synchronized (rooms) {
            try {
                theRoom = Iterables.find(rooms,
                        new Predicate<AbstractServerRoom>() {
                            @Override
                            public boolean apply(AbstractServerRoom room) {
                                return room.hasUser(session);
                            }
                        });
            }
            catch (NoSuchElementException nse) {
            }
        }

        // out of the synchronized block to avoid deadlock
        if (theRoom != null) {
            theRoom.abandon(session);

            int players = theRoom.getUserSessions().size();
            if (theRoom.isEnJuego()
                    && players < theRoom.getMinimumPlayingPlayers()
                    || players < theRoom.getMinimumPlayers()) {
                dropRoom(theRoom);
                rooms.remove(theRoom);
            }
            else {
                // broadcast to lobby that the user left the room
                broadcastLobby(new RoomLeftMessage(theRoom.getId(),
                        getUser(session)));
            }
        }

        // hook
        playerLeftRoom(session);
    }

    /**
     * gancho para que subclases llamen
     * 
     * @param user
     */
    protected void playerLeftRoom(IoSession session) {
    }

    private void dropRoom(AbstractServerRoom room) {
        // el que queda en la sala
        Collection<IoSession> sessions = room.getUserSessions();

        // mando los que estan dentro al lobby
        synchronized (lobby) {
            for (IoSession sess : sessions) {
                lobby.add(sess);
            }
        }

        // aviso que saque la sala
        broadcastLobby(new RoomDroppedMessage(room.getId()));
    }

    public void lobbyChat(IoSession session, String msg) {
        Randomin.reseed(msg); // ¡ENTROPIA!

        User u = getUser(session);

        // los guests no pueden chatear
        if (!u.isGuest()) {

            if (u.getFloodControl().isFlooding()) {
                // kick por flooder
                poster.kickPlayer("flooding", u.getName(), 10);
                return;
            }

            if (!handleOPMessage(session, msg)) {
                LobbyChatMessage chat = new LobbyChatMessage(msg);
                chat.setFrom(u);

                broadcastLobby(chat, session);

                // mantengo tamaño 10 los chats que guardo
                synchronized (lastChats) {
                    lastChats.add(chat);
                    while (lastChats.size() > 10) {
                        lastChats.remove(0);
                    }
                }
            }
        }
    }

    /**
     * chequea si el mensaje de lobby es de OP (kick, ban), y hace lo propio
     * 
     * @return si es de OP o no
     */
    protected boolean handleOPMessage(IoSession sess, String msg) {
        User u = getUser(sess);

        if (!poster.isOp(u.getName())) {
            return false;
        }

        // /kick [user] [tiempo=10]
        if (msg.startsWith("/kick ")) {
            // bla bla
            String[] params = msg.substring(6).split(" ");

            String usuario = params[0].trim();
            int tiempo = 10;
            if (params.length > 1) {
                try {
                    tiempo = Integer.parseInt(params[1]);
                }
                catch (NumberFormatException nfe) {
                }
            }

            poster.kickPlayer(u.getName(), usuario, tiempo);
            sess.write(new OPMessage("kick " + usuario + " " + tiempo + "'"));

            return true; // los mensajes OP nunca pasan
        }
        else if (msg.startsWith("/ban ")) {
            // TODO DRY
            String usuario = msg.substring(5);

            poster.banPlayer(u.getName(), usuario);
            sess.write(new OPMessage("ban " + usuario));

            return true; // los mensajes OP nunca pasan
        }
        else if (msg.startsWith("/unban ")) {
            // TODO DRY
            String usuario = msg.substring(7);

            poster.unbanPlayer(u.getName(), usuario);
            sess.write(new OPMessage("unban " + usuario));

            return true; // los mensajes OP nunca pasan
        }
        else {
            return false;
        }
    }

    public void roomChat(IoSession session, String msg) {
        AbstractServerRoom room = getRoom(session);

        if (room != null) {
            RoomChatMessage chat = new RoomChatMessage(msg);
            chat.setFrom(users.get(session));

            room.multicast(chat, session);
        }
    }

    public void proximoJuego(IoSession session, boolean acepta) {
        AbstractServerRoom room = getRoom(session);
        if (room != null) {
            room.proximoJuego(session, acepta);

            if (!acepta) {
                // liquido la sala
                dropRoom(room);

                // y la saco
                rooms.remove(room);
            }
        }
    }

    @Override
    public void ping(IoSession session) {
        User u = getUser(session);
        if (u != null) {
            RedisManager.ping(u);
        }
    }

    // http helpers
    int getRooms() {
        return rooms.size();
    }

    int getUsers() {
        return users.size();
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return getClass().getName() + " " + getId();
    }
}