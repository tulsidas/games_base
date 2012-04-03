package common.ifaz;

public interface LobbyMessage {

    // mensaje del server se ejecuta en el lobby
    public void execute(LobbyHandler server);
}
