package client;

import java.net.InetSocketAddress;
import java.util.Collection;

import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.RuntimeIOException;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.apache.mina.transport.socket.nio.SocketConnectorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import common.ifaz.BasicGameHandler;
import common.ifaz.BasicGameMessage;
import common.ifaz.LobbyHandler;
import common.ifaz.LobbyMessage;
import common.ifaz.LoginHandler;
import common.ifaz.LoginMessage;
import common.messages.Message;
import common.messages.TaringaProtocolEncoder;
import common.messages.XORFilter;
import common.messages.chat.LobbyChatMessage;
import common.messages.client.RequestLoginMessage;
import common.model.AbstractRoom;
import common.model.User;

public abstract class AbstractGameConnector extends IoHandlerAdapter implements
        Runnable, LobbyHandler, BasicGameHandler, LoginHandler {

    private String host;

    private int port;

    private String user, pass;

    private long version;

    private int salon;

    private IoSession session;

    // fachada
    protected LobbyHandler lobbyHandler;

    protected BasicGameHandler gameHandler;

    protected LoginHandler loginHandler;

    private Logger log = LoggerFactory.getLogger(getClass());

    private ProtocolDecoder decoder;

    public AbstractGameConnector(String host, int port, int salon, String user,
            String pass, long version, ProtocolDecoder decoder) {
        log.info("conectando a " + host + ":" + port);
        log.info("version: " + version);
        log.info("salon: " + salon + ", user: " + user + ", hash: " + pass);

        this.host = host;
        this.port = port;
        this.salon = salon;
        this.user = user;
        this.pass = pass;
        this.version = version;
        this.decoder = decoder;
    }

    public void connect() {
        // creo el hilo que intenta conexion
        new Thread(this).start();
    }

    public void run() {
        SocketConnector connector = new SocketConnector();
        SocketConnectorConfig scc = new SocketConnectorConfig();
        scc.setThreadModel(ThreadModel.MANUAL);
        connector.setDefaultConfig(scc);

        try {
            ConnectFuture future = connector.connect(new InetSocketAddress(
                    host, port), this);
            future.join();
            IoSession session = future.getSession();
            session.getCloseFuture().join();
        }
        catch (RuntimeIOException rie) {
            log.error("connecting", rie);
            loginHandler.disconnected();
        }
    }

    public void sessionCreated(IoSession sess) throws Exception {
        sess.getFilterChain().addFirst("crypt", new XORFilter());

        sess.getFilterChain().addLast("protocolFilter",
                new ProtocolCodecFilter(new TaringaProtocolEncoder(), decoder));
    }

    public void sessionOpened(IoSession sess) throws Exception {
        session = sess;

        // intento login
        session.write(new RequestLoginMessage(salon, new User(user), pass,
                version));
    }

    @Override
    public void sessionClosed(IoSession session) {
        if (loginHandler != null) {
            loginHandler.disconnected();
        }
        if (gameHandler != null) {
            gameHandler.disconnected();
        }
        if (lobbyHandler != null) {
            lobbyHandler.disconnected();
        }
    }

    public void exceptionCaught(IoSession sess, Throwable t) {
        log.warn("exceptionCaught", t);
    }

    public void messageReceived(IoSession sess, Object message) {
        if (message instanceof BasicGameMessage && gameHandler != null) {
            ((BasicGameMessage) message).execute(this);
        }
        if (message instanceof LobbyMessage && lobbyHandler != null) {
            ((LobbyMessage) message).execute(this);
        }
        if (message instanceof LoginMessage && loginHandler != null) {
            ((LoginMessage) message).execute(this);
        }
    }

    public void disconnect() {
        // cierro la sesion
        session.close().join();
    }

    public void send(Message msg) {
        session.write(msg);
    }

    // ///////////////////////
    // FACHADA!
    // ///////////////////////

    // Dependency Injection
    public void setGameHandler(BasicGameHandler gameHandler) {
        this.gameHandler = gameHandler;
        this.lobbyHandler = null;
        this.loginHandler = null;
    }

    public void setLobbyHandler(LobbyHandler lobbyHandler) {
        this.lobbyHandler = lobbyHandler;
        this.gameHandler = null;
        this.loginHandler = null;
    }

    public void setLoginHandler(LoginHandler loginHandler) {
        this.loginHandler = loginHandler;
        this.lobbyHandler = null;
        this.gameHandler = null;
    }

    // Fin Dependency Injection

    // /////////////
    // LoginHandler
    // /////////////
    public void loggedIn(User user) {
        if (loginHandler != null) {
            loginHandler.loggedIn(user);
        }
    }

    public void duplicatedLogin() {
        session.close();
        if (loginHandler != null) {
            loginHandler.duplicatedLogin();
        }
    }

    public void banned() {
        session.close();
        if (loginHandler != null) {
            loginHandler.banned();
        }
    }

    public void kicked() {
        session.close();
        if (loginHandler != null) {
            loginHandler.kicked();
        }
    }

    public void wrongVersion() {
        session.close();
        if (loginHandler != null) {
            loginHandler.wrongVersion();
        }
    }

    // /////////////
    // BasicGameHandler
    // /////////////
    public void oponenteAbandono(boolean enJuego, User user) {
        if (gameHandler != null) {
            gameHandler.oponenteAbandono(enJuego, user);
        }
    }

    public void updatePoints(int p) {
        if (gameHandler != null) {
            gameHandler.updatePoints(p);
        }
    }

    public void startGame(boolean start) {
        if (gameHandler != null) {
            gameHandler.startGame(start);
        }
    }

    public void newGame() {
        if (gameHandler != null) {
            gameHandler.newGame();
        }
    }

    public void finJuego(boolean victoria) {
        if (gameHandler != null) {
            gameHandler.finJuego(victoria);
        }
    }

    // /////////////
    // LobbyHandler
    // /////////////
    public void lobbyData(int puntos, Collection<AbstractRoom> rooms,
            Collection<User> users, Collection<LobbyChatMessage> lastChats,
            String msg) {
        if (lobbyHandler != null) {
            lobbyHandler.lobbyData(puntos, rooms, users, lastChats, msg);
        }
    }

    public void setLobbyMessage(String msg) {
        if (lobbyHandler != null) {
            lobbyHandler.setLobbyMessage(msg);
        }
    }

    public void incomingChat(User from, String msg) {
        if (lobbyHandler != null) {
            lobbyHandler.incomingChat(from, msg);
        }
        else if (gameHandler != null) {
            gameHandler.incomingChat(from, msg);
        }
    }

    public void roomCreated(AbstractRoom room) {
        if (lobbyHandler != null) {
            lobbyHandler.roomCreated(room);
        }
    }

    @Override
    public void roomsClosed() {
        if (lobbyHandler != null) {
            lobbyHandler.roomsClosed();
        }
    }

    @Override
    public void roomFull() {
        if (lobbyHandler != null) {
            lobbyHandler.roomFull();
        }
    }

    @Override
    public void gameStarted(int roomId) {
        if (lobbyHandler != null) {
            lobbyHandler.gameStarted(roomId);
        }
    }

    public void noAlcanzanPuntos() {
        if (lobbyHandler != null) {
            lobbyHandler.noAlcanzanPuntos();
        }
    }

    public void roomDropped(int roomId) {
        if (lobbyHandler != null) {
            lobbyHandler.roomDropped(roomId);
        }
    }

    public void joinRoomRequest(AbstractRoom room) {
        if (lobbyHandler != null) {
            lobbyHandler.joinRoomRequest(room);
        }
    }

    public void roomJoined(AbstractRoom room, User user) {
        if (gameHandler != null) {
            gameHandler.roomJoined(room, user);
        }
        else if (lobbyHandler != null) {
            lobbyHandler.roomJoined(room, user);
        }
    }

    public void roomLeft(int roomId, User user) {
        if (lobbyHandler != null) {
            lobbyHandler.roomLeft(roomId, user);
        }
    }

    public void lobbyJoined(User user) {
        if (lobbyHandler != null) {
            lobbyHandler.lobbyJoined(user);
        }
    }

    public void userDisconnected(User user) {
        if (lobbyHandler != null) {
            lobbyHandler.userDisconnected(user);
        }
    }

    public void disconnected() {
    }

    @Override
    public void opMessage(String msg) {
        if (lobbyHandler != null) {
            lobbyHandler.opMessage(msg);
        }
    }

    // XXX loguear cuando se recibe uno mal (o sea el handler es null)
}