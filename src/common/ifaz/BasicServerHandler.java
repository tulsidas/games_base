package common.ifaz;

import org.apache.mina.common.IoSession;

import common.model.User;

public interface BasicServerHandler {

    // ////////
    // LOGIN
    // ////////
    public void login(IoSession session, User user, String key);

    // ////////
    // LOBBY
    // ////////
    public void lobbyJoined(IoSession session);

    public void joinRoomRequest(IoSession session, int roomId);

    public void createRoom(IoSession session, int puntos);

    public void lobbyChat(IoSession session, String msg);

    // ////////
    // GAME
    // ////////

    public void roomChat(IoSession session, String msg);

    public void removePlayerFromRoom(IoSession session);

    public void roomJoined(IoSession session);

    public void proximoJuego(IoSession session, boolean acepta);

    public void ping(IoSession session);
}