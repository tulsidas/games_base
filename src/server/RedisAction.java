package server;

import redis.clients.jedis.Jedis;

public abstract class RedisAction {
    public abstract Object execute(Jedis j);
}
