package common.ifaz;

public interface LoginMessage {

    // mensaje del server se ejecuta en el cliente
    public void execute(LoginHandler server);
}
