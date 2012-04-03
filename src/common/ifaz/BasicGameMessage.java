package common.ifaz;

public interface BasicGameMessage {
    // mensaje del server se ejecuta en el cliente
    public void execute(BasicGameHandler game);
}
