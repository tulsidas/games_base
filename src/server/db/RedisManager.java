package server.db;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.pool.impl.GenericObjectPool.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import server.RedisAction;

import com.google.common.collect.Maps;
import common.model.User;

public class RedisManager {
    private static JedisPool jPool;
    static {
        Config conf = new Config();
        conf.testOnBorrow = true;

        jPool = new JedisPool(conf, "localhost");
    }

    public static final String J_FICHAS = "fichas";

    public static final String J_UPDATE = "update";

    public static final String J_PING = "ping";

    private static final int START_VALUE = 200;

    private static final int RELOAD_VALUE = 100;

    private static Logger log = LoggerFactory.getLogger(RedisManager.class);

    private static Object execute(RedisAction action) {
        Jedis j = null;
        try {
            j = jPool.getResource();

            return action.execute(j);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        finally {
            if (j != null) {
                try {
                    jPool.returnResource(j);
                }
                catch (Exception e) {
                    log.error("execute", e);
                }
            }
        }
    }

    public static void transferPoints(final User u1, final User u2,
            final int fichas) {
        execute(new RedisAction() {
            @Override
            public Object execute(Jedis j) {
                int f1 = getFichas(u1.getName());
                int f2 = getFichas(u2.getName());

                if (f1 - fichas < 0 || f2 - fichas < 0) {
                    log.warn(String.format(
                            "ilegal! - %d fichas de %s (%d) a %s (%d)", fichas,
                            u1, f1, u2, f2));
                }
                else {
                    Transaction t = j.multi();
                    t.hincrBy(u1.getName(), J_FICHAS, fichas);
                    t.hincrBy(u2.getName(), J_FICHAS, -fichas);
                    List<Object> ret = t.exec();

                    log.debug(fichas + " fichas de " + u2 + " a " + u1);

                    // write lock
                    if (u1 != null) {
                        int puntos1 = ((Long) ret.get(0)).intValue();
                        u1.getLock().writeLock().lock();
                        u1.setPuntos(puntos1);
                        u1.getLock().writeLock().unlock();
                    }
                    if (u2 != null) {
                        int puntos2 = ((Long) ret.get(1)).intValue();
                        u2.getLock().writeLock().lock();
                        u2.setPuntos(puntos2);
                        u2.getLock().writeLock().unlock();
                    }
                }

                return null;
            }
        });
    }

    public static int getFichas(final String usuario) {
        return (Integer) execute(new RedisAction() {
            @Override
            public Object execute(Jedis j) {
                int ret = -1;
                String r = j.hget(usuario, J_FICHAS);
                if (r == null) {
                    // nuevo usuario
                    Map<String, String> map = Maps.newHashMap();
                    map.put(J_FICHAS, Integer.toString(START_VALUE));
                    map.put(J_UPDATE, Long.toString(System.currentTimeMillis()));

                    j.hmset(usuario, map);

                    ret = START_VALUE;
                }
                else {
                    ret = Integer.parseInt(r);

                    if (ret == 0) {
                        long ts = Long.parseLong(j.hget(usuario, J_UPDATE));
                        long now = System.currentTimeMillis();
                        if (now - ts > TimeUnit.DAYS.toMillis(1)) {
                            // recarga
                            Map<String, String> map = Maps.newHashMap();
                            map.put(J_FICHAS, Integer.toString(RELOAD_VALUE));
                            map.put(J_UPDATE,
                                    Long.toString(System.currentTimeMillis()));

                            j.hmset(usuario, map);

                            ret = RELOAD_VALUE;
                        }
                    }
                }

                return ret;
            }
        });
    }

    public static void sacarPuntos(final Iterable<User> players,
            final int fichas) {
        if (fichas > 0) {
            execute(new RedisAction() {
                @Override
                public Object execute(Jedis j) {
                    Transaction t = j.multi();
                    for (User ply : players) {
                        t.hincrBy(ply.getName(), J_FICHAS, -fichas);
                        ply.getLock().writeLock().lock();
                        ply.setPuntos(ply.getPuntos() - fichas);
                        ply.getLock().writeLock().unlock();
                    }
                    t.exec();

                    log.debug("saco " + fichas + " a " + players);

                    return null;
                }
            });
        }
    }

    public static void darPuntos(final User player, final int fichas) {
        execute(new RedisAction() {
            @Override
            public Object execute(Jedis j) {
                j.hincrBy(player.getName(), J_FICHAS, fichas);

                log.debug(player + " gano " + fichas + " fichas ");

                player.getLock().writeLock().lock();
                player.setPuntos(player.getPuntos() + fichas);
                player.getLock().writeLock().unlock();

                return null;
            }
        });
    }

    public static void ping(final User player) {
        execute(new RedisAction() {
            @Override
            public Object execute(Jedis j) {
                j.hset(player.getName(), J_PING,
                        Long.toString(System.currentTimeMillis()));
                return null;
            }
        });
    }

    public static boolean login(final User player) {
        return (Boolean) execute(new RedisAction() {
            @Override
            public Object execute(Jedis j) {
                // watch player key
                j.watch(player.getName());

                String pingStr = j.hget(player.getName(), J_PING);
                boolean logueado = false;
                if (pingStr != null) {
                    long ping = Long.parseLong(pingStr);

                    // true si pasó menos de 2 min del último ping
                    logueado = System.currentTimeMillis() - ping < TimeUnit.MINUTES
                            .toMillis(2);
                }

                if (logueado) {
                    // fallo el login, ya esta logueado
                    return false;
                }
                else {
                    Transaction t = j.multi();
                    t.hset(player.getName(), J_PING,
                            Long.toString(System.currentTimeMillis()));
                    Object ret = t.exec();

                    if (ret == null) {
                        // fallo el locking optimista
                        return false;
                    }
                    else {
                        return true;
                    }
                }
            }
        });
    }

    public static void logout(final User player) {
        execute(new RedisAction() {

            @Override
            public Object execute(Jedis j) {
                j.hdel(player.getName(), J_PING);
                return null;
            }
        });
    }

    // public static void mute(final User player, final User muteado) {
    // execute(new RedisAction() {
    //
    // @Override
    // public Object execute(Jedis j) {
    // j.select(1);
    //
    // return null;
    // }
    // });
    // }
}
