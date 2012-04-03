package server;

import java.util.List;

import org.apache.mina.common.IoService;
import org.apache.mina.integration.jmx.IoServiceManager;

public class GameServiceManager extends IoServiceManager implements
        GameServiceManagerMBean {

    private JMXHandler server;

    public GameServiceManager(IoService service, JMXHandler server) {
        super(service);
        this.server = server;
    }

    public void acceptNewRooms(boolean val) {
        server.acceptNewRooms(val);
    }

    public void kickPlayer(String name, int minutos) {
        server.kickPlayer("jconsole", name, minutos);
    }

    public void banPlayer(String name) {
        server.banPlayer("jconsole", name);
    }

    public void unbanPlayer(String name) {
        server.unbanPlayer("jconsole", name);
    }

    public void changeLobbyMessage(String s) {
        server.changeLobbyMessage(s);
    }

    @Override
    public List<String> listBans() {
        return server.listBans();
    }

    @Override
    public List<String> listKicks() {
        return server.listKicks();
    }

    public void addOp(String op) {
        server.addOp(op);
    }

    public void removeOp(String op) {
        server.removeOp(op);
    }

    public List<String> listOps() {
        return server.listOps();
    }
}
