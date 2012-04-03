package client;

import pulpcore.scene.Scene2D;

import common.messages.server.PingMessage;

public class PingScene extends Scene2D {

    private AbstractGameConnector connector;

    private int time;

    private static final int PING_TIME = 60 * 1000;

    public PingScene(AbstractGameConnector connector) {
        this.connector = connector;
        this.time = 0;
    }

    @Override
    public void update(int elapsedTime) {
        time += elapsedTime;

        if (time > PING_TIME) {
            connector.send(new PingMessage());
            time = 0;
        }
    }
}
