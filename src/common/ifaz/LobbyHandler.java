package common.ifaz;

import java.util.Collection;

import common.messages.chat.LobbyChatMessage;
import common.model.AbstractRoom;
import common.model.User;

public interface LobbyHandler {

    public void lobbyData(int puntos, Collection<AbstractRoom> rooms,
            Collection<User> users, Collection<LobbyChatMessage> lastChats,
            String lobbyMsg);

    public void setLobbyMessage(String msg);

    /**
     * @param from
     *            el remitente
     * @param msg
     *            el mensaje
     */
    public void incomingChat(User from, String msg);

    /**
     * se creo una sala
     * 
     * @param room
     *            la sala creada
     */
    public void roomCreated(AbstractRoom room);

    /**
     * se elimino una sala
     * 
     * @param room
     *            la sala creada
     */
    public void roomDropped(int roomId);

    /**
     * Salas cerradas, no se puede crear una nueva
     */
    public void roomsClosed();

    /**
     * Pedido para unirse a una sala
     * 
     * @param roomId
     */
    public void joinRoomRequest(AbstractRoom room);

    /**
     * Un usuario ingreso a una sala
     * 
     * @param room
     * @param user
     */
    public void roomJoined(AbstractRoom room, User user);

    /**
     * Un usuario salio de una sala
     * 
     * @param room
     * @param user
     */
    public void roomLeft(int roomId, User user);

    /**
     * La sala a la que me quise unir está llena
     */
    public void roomFull();

    /**
     * Un usuario entro al lobby
     * 
     * @param user
     *            el entrante
     */
    public void lobbyJoined(User user);

    /**
     * 
     * @param user
     */
    public void userDisconnected(User user);

    public void disconnected();

    /**
     * Se intento crear una sala por mas puntos de los disponibles
     */
    public void noAlcanzanPuntos();

    /**
     * empezo el juego en la sala
     * 
     * @param roomId
     */
    public void gameStarted(int roomId);

    /**
     * respuesta a un mensaje OP
     */
    public void opMessage(String msg);
}