package server;

import java.util.List;

import org.apache.mina.integration.jmx.IoServiceManagerMBean;

public interface GameServiceManagerMBean extends IoServiceManagerMBean {
    public void acceptNewRooms(boolean val);

    public void kickPlayer(String name, int min);

    public void banPlayer(String name);

    public void unbanPlayer(String name);

    public List<String> listBans();

    public List<String> listKicks();

    public void addOp(String op);

    public void removeOp(String op);

    public List<String> listOps();

    public void changeLobbyMessage(String s);
}
