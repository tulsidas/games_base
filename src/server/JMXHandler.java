package server;

import java.util.List;

public interface JMXHandler {
    public void acceptNewRooms(boolean val);

    public void kickPlayer(String origen, String name, int minutos);

    public void banPlayer(String origen, String name);

    public void unbanPlayer(String origen, String name);

    public List<String> listBans();

    public List<String> listKicks();

    public List<String> listOps();

    public void addOp(String op);

    public void removeOp(String op);
    
    public void changeLobbyMessage(String s);
}
