package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import server.db.RedisManager;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import common.ifaz.BasicClientGameMessage;
import common.ifaz.ClientLoginHandler;
import common.ifaz.ClientLoginMessage;
import common.ifaz.POSTHandler;
import common.messages.TaringaProtocolEncoder;
import common.messages.XORFilter;
import common.messages.server.BannedUserMessage;
import common.messages.server.KickedUserMessage;
import common.messages.server.WrongVersionMessage;
import common.model.User;

/**
 * {@link IoHandler} for Server
 */
public abstract class ServerSessionHandler extends IoHandlerAdapter implements
        JMXHandler, HttpServerHandler, ClientLoginHandler, POSTHandler {

    private static final int ONE_MINUTE = 60 * 1000;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Set<String> bans;

    /**
     * Map de String (usuario), Long (timestamp) para registro de los kicks
     */
    private List<KickInfo> kicks;

    protected Collection<AbstractSaloon> salones;

    private boolean noConnect;

    private long version;

    private static final String dbHost = "taringaSQL1";

    // conexion a la base
    private JdbcTemplate jdbc;

    // el decodificador
    private ProtocolDecoder decoder;

    private Set<String> ops;

    public ServerSessionHandler(ProtocolDecoder decoder) {
        this.decoder = decoder;

        this.bans = Collections.synchronizedSet(new HashSet<String>());
        this.kicks = Collections.synchronizedList(new ArrayList<KickInfo>());

        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(getClass()
                    .getClassLoader().getResourceAsStream("version")));
            version = Long.parseLong(in.readLine());
            log.debug("Version " + version);
        }
        catch (Exception ioe) {
            System.out.println(ioe);
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException ioe) {
                }
            }
        }

        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setUrl("jdbc:mysql://" + dbHost + "/taringa3");
        ds.setUsername("truco");
        ds.setPassword("***");

        // pool config
        ds.setInitialSize(1);
        ds.setMaxActive(10);
        ds.setMaxIdle(5);
        ds.setMinIdle(1);
        ds.setPoolPreparedStatements(true);
        ds.setValidationQuery("select 1");

        jdbc = new JdbcTemplate(ds);

        Set<String> _ops = Sets.newHashSet("Tulsi");
        ops = Collections.synchronizedSet(_ops);
    }

    /** el codigo del juego en la base */
    protected abstract int getCodigoJuego();

    public void sessionCreated(IoSession session) throws Exception {
        session.getFilterChain().addFirst("crypt", new XORFilter());

        session.getFilterChain().addLast("protocolFilter",
                new ProtocolCodecFilter(new TaringaProtocolEncoder(), decoder));

        // chequeo que haya escrito algo cada X tiempo
        session.setIdleTime(IdleStatus.READER_IDLE, 75);
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status)
            throws Exception {
        User u = getUser(session);
        if (u != null) {
            if (u.isIdle()) {
                // segundo idle, cierro la sesion
                log.debug("disconnect " + u + " (idle)");
                session.close();
            }
            else {
                log.debug(u + " is idle");
                u.setIdle(true);
            }
        }
        else {
            session.close();
        }
    }

    public void sessionClosed(IoSession session) {
        // FIXME este puede ser llamado por varios a la vez, no es thread safe
        try {
            getSaloon(session).abandonGame(session);
        }
        catch (NoSuchElementException nsee) {
        }
    }

    @Override
    public void messageReceived(IoSession session, Object message) {
        User u = getUser(session);
        String name = u == null ? "?" : u.getName();

        if (u != null) {
            u.setIdle(false);
        }

        log.debug("-> " + message.toString() + " (" + name + ")");

        if (message instanceof BasicClientGameMessage) {
            ((BasicClientGameMessage) message).execute(session,
                    getSaloon(session));
        }
        else if (message instanceof ClientLoginMessage) {
            ((ClientLoginMessage) message).execute(session, this);
        }
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        super.messageSent(session, message);
    }

    public void exceptionCaught(IoSession session, Throwable cause) {
        log.error("exceptionCaught: " + cause.toString() + " | user:"
                + getUser(session), cause);
        if (cause instanceof IOException) {
            session.close();
        }
        else {
            cause.printStackTrace();
        }
    }

    // ///////////////
    // Utilities
    // ///////////////
    public boolean logueado(final String name) {
        synchronized (salones) {
            return Iterables.any(salones, new Predicate<AbstractSaloon>() {
                public boolean apply(AbstractSaloon s) {
                    return s.logueado(name);
                }
            });
        }
    }

    private User getUser(IoSession player) {
        synchronized (salones) {
            for (AbstractSaloon s : salones) {
                if (s.hasUser(player)) {
                    return s.getUser(player);
                }
            }
        }
        return null;
    }

    /**
     * @param player
     * @return el salon donde esta el player
     */
    private AbstractSaloon getSaloon(final IoSession player) {
        synchronized (salones) {
            return Iterables.find(salones, new Predicate<AbstractSaloon>() {
                public boolean apply(AbstractSaloon s) {
                    return s.hasUser(player);
                }
            });
        }
    }

    /**
     * @param id
     * @return el salon de id id
     */
    private AbstractSaloon getSaloon(final int id) {
        synchronized (salones) {
            return Iterables.find(salones, new Predicate<AbstractSaloon>() {
                public boolean apply(AbstractSaloon s) {
                    return s.getId() == id;
                }
            });
        }
    }

    public int[] transferPoints(User u1, User u2, int puntos) {

        if (puntos > 0) {
            log.debug("transferPoints " + u2.getName() + " -> " + u1.getName()
                    + " (" + puntos + " pts)");
            RedisManager.transferPoints(u1, u2, puntos);
        }

        return new int[] { u1.getPuntos(), u2.getPuntos() };
    }

    /**
     * @return (valido),(puntos), valido=0|1
     */
    public int validateUser(String username, String hash) {
        if (noConnect) {
            return getFichas(username);
        }
        else {
            String points = (String) jdbc.queryForObject(
                    "select validate_user_and_get_score(?, ?)", new Object[] {
                            username, hash }, String.class);

            if (points.charAt(0) == '1') {
                return getFichas(username);
            }
            else {
                return -10;
            }
        }
    }

    /**
     * las fichas de Redis
     */
    private int getFichas(String usuario) {
        return RedisManager.getFichas(usuario);
    }

    // ///////////////
    // ClientLoginHandler
    // ///////////////
    public void login(IoSession session, int sid, User usr, String hash,
            long version) {
        log.debug("login " + session.getRemoteAddress() + " | " + usr.getName()
                + "(" + version + ")");

        if (!noConnect) {
            if (version < this.version) {
                log.debug(usr.getName() + " tiene version vieja (" + version
                        + ")");
                session.write(new WrongVersionMessage());
                return;
            }
        }

        if (usr.getName().length() > 35) {
            // hacker rompe bolas
            log.warn("molesto con nick > 35: " + usr.getName().substring(0, 35)
                    + " | " + session.getRemoteAddress());
            return;
        }

        if (isBanned(usr.getName())) {
            // || isBanned(((InetSocketAddress) session.getRemoteAddress())
            // .getAddress())) {
            log.debug(usr.getName() + " is banned");
            session.write(new BannedUserMessage());
        }
        else if (isKicked(usr.getName())) {
            log.debug(usr.getName() + " is kicked");
            session.write(new KickedUserMessage());
        }
        else {
            getSaloon(sid).login(session, usr, hash);
        }
    }

    // ///////////////
    // JMX
    // ///////////////
    public void acceptNewRooms(boolean val) {
        synchronized (salones) {
            for (AbstractSaloon s : salones) {
                s.setAcceptingNewRooms(val);
            }
        }
    }

    // ///////////////
    // Managment
    // ///////////////
    // private InetAddress getIP(final String name) {
    // synchronized (salones) {
    // try {
    // return Iterables.find(salones, new Predicate<AbstractSaloon>() {
    // public boolean apply(AbstractSaloon t) {
    // return t.getIP(name) != null;
    // }
    // }).getIP(name);
    // }
    // catch (NoSuchElementException nsee) {
    // return null;
    // }
    // }
    // }

    public void kickPlayer(String origen, String name, int min) {
        // lo agrego a kicks
        KickInfo ki = getKickInfo(name);
        if (ki == null) {
            kicks.add(new KickInfo(name, min));
        }
        else {
            ki.add(min);
        }

        log.debug("[" + origen + "] kick " + name + ", " + min + "'");

        // adios
        disconnectPlayer(name);
    }

    @Override
    public void banPlayer(String origen, String name) {
        log.debug("[" + origen + "] ban " + name);

        // lo agrego a bans
        bans.add(name.toLowerCase());

        // adios
        disconnectPlayer(name);
    }

    @Override
    public void unbanPlayer(String origen, String name) {
        log.debug("[" + origen + "] unban " + name);
        bans.remove(name.toLowerCase());
    }

    private boolean isKicked(String name) {
        KickInfo ki = getKickInfo(name);
        if (ki == null) {
            return false;
        }
        else {
            if (ki.isActive()) {
                return true;
            }
            else {
                kicks.remove(ki);
                return false;
            }
        }
    }

    private boolean isBanned(String name) {
        return bans.contains(name.toLowerCase());
    }

    public boolean isOp(String name) {
        return ops.contains(name);
    }

    // private boolean isBanned(InetAddress addr) {
    // return bans.inverse().containsKey(toInt(addr));
    // }

    public List<String> listBans() {
        return new ArrayList<String>(bans);
    }

    public List<String> listKicks() {
        synchronized (kicks) {
            Iterable<KickInfo> active = Iterables.filter(kicks,
                    new Predicate<KickInfo>() {
                        @Override
                        public boolean apply(KickInfo ki) {
                            return ki.isActive();
                        }
                    });

            // borro los inactivos
            Collection<KickInfo> inactivos = new HashSet<KickInfo>(kicks);
            inactivos.removeAll(Sets.newHashSet(active));
            for (KickInfo ki : inactivos) {
                kicks.remove(ki);
            }

            return Lists.newArrayList(Iterables.transform(active,
                    new Function<KickInfo, String>() {
                        @Override
                        public String apply(KickInfo ki) {
                            return ki.toString();
                        }
                    }));
        }
    }

    @Override
    public void addOp(String op) {
        ops.add(op);
    }

    @Override
    public void removeOp(String op) {
        ops.remove(op);
    }

    @Override
    public List<String> listOps() {
        return new ArrayList<String>(ops);
    }

    private boolean disconnectPlayer(final String name) {
        // lo desconecto del salon en que este
        synchronized (salones) {
            return Iterables.any(salones, new Predicate<AbstractSaloon>() {
                public boolean apply(AbstractSaloon t) {
                    return t.disconnectPlayer(name);
                }
            });
        }
    }

    private KickInfo getKickInfo(final String name) {
        synchronized (kicks) {
            try {
                return Iterables.find(kicks, new Predicate<KickInfo>() {
                    public boolean apply(KickInfo kick) {
                        return kick.name.equalsIgnoreCase(name);
                    }
                });
            }
            catch (NoSuchElementException nsee) {
                return null;
            }
        }
    }

    public void changeLobbyMessage(String msg) {
        // broadcast
        synchronized (salones) {
            for (AbstractSaloon salon : salones) {
                salon.setLobbyMessage(msg);
            }
        }
    }

    public String checkClosedSessions() {
        StringBuffer ret = new StringBuffer();
        synchronized (salones) {
            for (AbstractSaloon s : salones) {
                ret.append(s.checkClosedSessions());
            }
        }

        return ret.toString();
    }

    public void setNoConnect() {
        this.noConnect = true;
    }

    /*
     * HttpServerHandler implementation
     */
    public String getUsersRooms() {
        StringBuffer ret = new StringBuffer();
        synchronized (salones) {
            for (AbstractSaloon s : salones) {
                ret.append(s.getRooms());
                ret.append("|");
                ret.append(s.getUsers());
                ret.append("|");
            }
        }

        return ret.toString();
    }

    // private static long toInt(InetAddress inet) {
    // long ret = 0;
    //
    // if (inet != null) {
    // byte[] add = inet.getAddress();
    //
    // ret += (add[0] & 0xFFL) << 24;
    // ret += (add[1] & 0xFFL) << 16;
    // ret += (add[2] & 0xFFL) << 8;
    // ret += (add[3] & 0xFFL);
    // }
    //
    // return ret;
    // }

    private static class KickInfo {
        // quien
        private String name;

        // cuando expira el kick
        private long timestamp;

        public KickInfo(String name, int minutos) {
            this.name = name;
            this.timestamp = System.currentTimeMillis() + minutos * ONE_MINUTE;
        }

        @Override
        public String toString() {
            return name + " " + (getRemaining() / 1000) + "s";
        }

        private long getRemaining() {
            return timestamp - System.currentTimeMillis();
        }

        public boolean isActive() {
            return getRemaining() > 0;
        }

        public void add(int minutos) {
            timestamp += minutos * ONE_MINUTE;
        }

        @Override
        public int hashCode() {
            return name == null ? 0 : name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final KickInfo other = (KickInfo) obj;
            if (name == null) {
                if (other.name != null)
                    return false;
            }
            else if (!name.equals(other.name))
                return false;
            return true;
        }
    }
}