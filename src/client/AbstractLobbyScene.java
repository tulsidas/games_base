package client;

import static pulpcore.image.Colors.WHITE;
import static pulpcore.image.Colors.rgb;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import pulpcore.Assets;
import pulpcore.CoreSystem;
import pulpcore.Input;
import pulpcore.Stage;
import pulpcore.image.Colors;
import pulpcore.image.CoreFont;
import pulpcore.image.CoreImage;
import pulpcore.image.filter.DropShadow;
import pulpcore.scene.Scene;
import pulpcore.sound.Sound;
import pulpcore.sprite.Button;
import pulpcore.sprite.Group;
import pulpcore.sprite.ImageSprite;
import pulpcore.sprite.Label;
import pulpcore.sprite.Sprite;
import pulpcore.sprite.TextField;
import pulpcore.util.StringUtil;
import client.DisconnectedScene.Reason;

import common.ifaz.LobbyHandler;
import common.messages.LobbyJoinedMessage;
import common.messages.Message;
import common.messages.chat.LobbyChatMessage;
import common.messages.client.CreateRoomMessage;
import common.messages.client.JoinRoomMessage;
import common.model.AbstractRoom;
import common.model.User;

/**
 * @author Tulsi
 */
public abstract class AbstractLobbyScene extends PingScene implements
        LobbyHandler {
    protected AbstractGameConnector connection;

    private ColoredChatArea chat;

    private TextField chatTF;

    private Button send;

    protected PlayersBox players;

    protected RoomsBox rooms;

    private Button crearSala, disableSounds;

    private Label conectandoLabel;

    protected boolean mustDisconnect;

    private Spinner puntos;

    protected User currentUser;

    private CoreFont msgFont;

    private Group messageGrp;

    private Sound tic;

    protected Set<String> muted;

    public AbstractLobbyScene(User user, AbstractGameConnector connection) {
        super(connection);

        this.currentUser = user;
        this.connection = connection;
        this.connection.setLobbyHandler(this); // inject scene handler
        this.mustDisconnect = true;

        muted = new HashSet<String>();
    }

    public void mute(User u) {
        muted.add(u.getName().toLowerCase());
    }

    public void load() {
        CoreFont din13 = CoreFont.load("imgs/DIN13.font.png");
        CoreFont din15 = CoreFont.load("imgs/DIN15.font.png");
        CoreFont din30 = CoreFont.load("imgs/DIN30.font.png");
        CoreFont din18numeric = CoreFont.load("imgs/DIN18_numeric.font.png");

        // fondo
        add(new ImageSprite("imgs/lobby.png", 0, 0));

        // rooms box
        rooms = new RoomsBox(this, 13, 90, 236, 289, din13);
        add(rooms);

        // players box
        players = new PlayersBox(270, 90, 176, 289, din13);
        add(players);

        // campo de texto donde se chatea
        chatTF = new TextField(din15, din15.tint(WHITE), "", 467, 393, 217, -1);
        chatTF.setMaxNumChars(150);
        add(chatTF);

        // boton para enviar el chat (asociado al ENTER)
        send = new Button(CoreImage.load("imgs/btn-send.png").split(3), 688,
                392);
        send.setKeyBinding(Input.KEY_ENTER);
        add(send);

        // boton para crear sala
        crearSala = new Button(CoreImage.load("imgs/btn-crear-sala.png").split(
                3), 153, 390);
        add(crearSala);

        // boton para mutear
        disableSounds = new Button(CoreImage.load("imgs/sonidos.png").split(6),
                125, 60, true);
        disableSounds.setSelected(CoreSystem.isMute());
        add(disableSounds);

        // chat box
        chat = new ColoredChatArea(467, 90, 235, 289, din15, din15
                .tint(rgb(0xaa0000)), ':', currentUser.getName());
        add(chat);

        // spinner background
        add(new ImageSprite(CoreImage.load("imgs/jugar-por.png"), 19, 385));

        // spinner
        CoreImage[] up = CoreImage.load("imgs/btn-puntos.png").split(3);

        puntos = new Spinner(16, 400, 54, 25, din18numeric, up);
        puntos.setValue(Math.min(puntos.getValue(), currentUser.getPuntos()));
        add(puntos);

        // nombre del juego
        add(getGameImage());

        // label conectando
        conectandoLabel = new Label(din30, "Obteniendo datos...", 300, 300);
        add(conectandoLabel);

        // lobby message
        msgFont = CoreFont.load("imgs/DIN15.font.png").tint(rgb(WHITE));

        messageGrp = new Group(180, 10, 330, 50);
        add(messageGrp);

        setPaused(true);

        tic = Sound.load("sfx/tic.wav");

        // version del juego
        long version = 0;

        try {
            version = Long
                    .parseLong(new String(Assets.get("version").getData()));
        }
        catch (Exception e) {
        }
        add(new Label(din13.tint(Colors.WHITE), "v" + version, 0, 0));

        // mando el ACK que me uni
        connection.send(new LobbyJoinedMessage());
    }

    public void update(int elapsedTime) {
        super.update(elapsedTime);

        if (!isPaused()) {
            // los guests no pueden chatear en el lobby
            if (send.isClicked() && !currentUser.isGuest()) {
                if (chatTF.getText().trim().length() > 0) {
                    String txt = chatTF.getText();

                    if (txt.startsWith("/mute ")) {
                        String quien = txt.substring("/mute ".length());
                        muted.add(quien);
                        chat.addLine("[ callaste a \"" + quien + "\" ]");
                    }
                    else {
                        // mando
                        connection.send(new LobbyChatMessage(txt));
                        chat.addLine(currentUser.getName() + ": " + txt);
                    }
                    chatTF.setText("");
                }
            }
            else if (crearSala.enabled.get() && crearSala.isMouseReleased()
                    && puedeCrearSala()) {
                crearSala.enabled.set(false);
                rooms.enabled.set(false);

                connection.send(createRoomMessage(puntos.getValue()));
            }
            else if (disableSounds.isClicked()) {
                CoreSystem.setMute(disableSounds.isSelected());
            }
        }
    }

    protected Message createRoomMessage(int value) {
        return new CreateRoomMessage(value);
    }

    public void unload() {
        if (mustDisconnect) {
            connection.disconnect();
        }
    }

    // ////////////////
    // LobbyHandler //
    // ////////////////

    public void lobbyData(final int puntosDisponibles,
            final Collection<AbstractRoom> salas,
            final Collection<User> jugadores,
            final Collection<LobbyChatMessage> lastChats,
            final String lobbyMessage) {

        final CoreFont din18w = CoreFont.load("imgs/DIN18.font.png")
                .tint(WHITE);
        currentUser.setPuntos(puntosDisponibles);
        puntos.setMaxValue(puntosDisponibles);

        // listo para interactuar
        setPaused(false);

        invokeLater(new Runnable() {
            public void run() {
                remove(conectandoLabel);

                // puntos disponibles
                Label tf = new Label(din18w.tint(0x67e3f9), "Tus fichas", 320,
                        390);
                tf.setFilter(new DropShadow(2, 2, Colors.BLUE, 2));
                add(tf);

                add(new Label(din18w,
                        Integer.toString(currentUser.getPuntos()), 330, 410));

                // populo rooms box
                for (AbstractRoom room : salas) {
                    rooms.addRoom(room);
                }

                // populo players box
                for (User u : jugadores) {
                    players.addPlayer(u);
                }

                // populo last chats
                for (LobbyChatMessage lcm : lastChats) {
                    chat.addLine(lcm.getFrom().getName() + ": " + lcm.getMsg());
                }

                // lobby message
                setLobbyMessage(lobbyMessage);
            }
        });
    }

    public void incomingChat(final User usr, final String msg) {
        if (!muted.contains(usr.getName().toLowerCase())) {
            invokeLater(new Runnable() {
                public void run() {
                    chat.addLine(usr.getName() + ": " + msg);

                    // tic si me mencionan
                    if (msg.toLowerCase().indexOf(
                            currentUser.getName().toLowerCase()) >= 0) {
                        tic.play();
                    }
                }
            });
        }
    }

    public void setLobbyMessage(final String msg) {
        invokeLater(new Runnable() {
            public void run() {
                messageGrp.removeAll();

                String[] str = StringUtil.wordWrap(msg, msgFont, 330);
                if (str.length == 0) {
                    return;
                }

                for (int i = 0; i < str.length; i++) {
                    Label l = new Label(msgFont, str[i], 0, 15 * i);
                    l.setFilter(new DropShadow());
                    PulpcoreUtils.centerSprite(l, 0, messageGrp.width
                            .getAsInt());

                    messageGrp.add(l);
                }
            }
        });
    }

    public void noAlcanzanPuntos() {
        // XXX mostrar mensaje
        invokeLater(new Runnable() {
            public void run() {
                crearSala.enabled.set(true);
            }
        });
    }

    public void roomCreated(final AbstractRoom room) {
        invokeLater(new Runnable() {
            public void run() {
                // crearon una sala, agrego a la lista de salas
                rooms.addRoom(room);

                // saco a los usuarios dentro de la sala del lobby
                for (User u : room.getPlayers()) {
                    players.removePlayer(u);
                }
            }
        });
    }

    public void roomDropped(final int roomId) {
        invokeLater(new Runnable() {
            public void run() {
                // chau sala
                rooms.dropRoom(roomId);
            }
        });
    }

    @Override
    public void gameStarted(final int roomId) {
        invokeLater(new Runnable() {
            public void run() {
                // chau sala
                rooms.gameStarted(roomId);
            }
        });
    }

    @Override
    public void roomsClosed() {
        // XXX dejo deshabilitado el crear sala?

        // re-habilito rooms box
        rooms.enabled.set(true);
    }

    @Override
    public void roomFull() {
        crearSala.enabled.set(true);
        rooms.enabled.set(true);
    }

    public void roomJoined(final AbstractRoom room, final User user) {
        // si soy yo, me meto a la sala, sino agrego a la lista
        if (user.equals(currentUser)) {
            invokeLater(new Runnable() {
                public void run() {
                    // user generated scene change, don't disconnect from the
                    // server
                    mustDisconnect = false;
                    Stage.setScene(getGameScene(connection, currentUser, room));
                }
            });
        }
        else {
            invokeLater(new Runnable() {
                public void run() {
                    // agrego a la sala
                    rooms.addPlayer(room, user);

                    // lo saco del lobby
                    players.removePlayer(user);
                }
            });
        }
    }

    public void roomLeft(int roomId, User user) {
        rooms.removePlayer(roomId, user);
    }

    public void lobbyJoined(final User user) {
        invokeLater(new Runnable() {
            public void run() {
                // agrego el usuario a la lista
                players.addPlayer(user);
            }
        });
    }

    public void userDisconnected(final User user) {
        invokeLater(new Runnable() {
            public void run() {
                players.removePlayer(user);
            }
        });
    }

    public void joinRoomRequest(AbstractRoom room) {
        // mando el pedido de unirme a la sala (si no esta llena)
        if (!room.isFull() && !room.isStarted()) {
            crearSala.enabled.set(false);
            rooms.enabled.set(false);

            // mando pedido
            connection.send(new JoinRoomMessage(room.getId()));
        }
    }

    public void disconnected() {
        System.out.println("disconnected!");
        invokeLater(new Runnable() {
            public void run() {
                Stage.setScene(new DisconnectedScene(Reason.FAILED));
            }
        });
    }

    /**
     * hook para validar que los parametros de spinners y demas sean validos
     * 
     * @return
     */
    protected boolean puedeCrearSala() {
        return true;
    }

    @Override
    public void opMessage(String msg) {
        chat.addLine("[OP] " + msg);
    }

    // abstract methods
    protected abstract Scene getGameScene(AbstractGameConnector connection,
            User usr, AbstractRoom room);

    protected abstract Sprite getGameImage();

}