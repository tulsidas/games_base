package common.model;

import java.util.ArrayList;
import java.util.List;

import common.util.StringUtil;

public class TwoPlayerRoom extends AbstractRoom {

    public TwoPlayerRoom() {
        super();
    }

    public TwoPlayerRoom(int id, int puntosApostados, List<User> players) {
        super(id, puntosApostados, players);
    }

    public String getDisplayText() {
        List<User> users = new ArrayList<User>(getPlayers());

        User u1 = null, u2 = null;

        if (users.size() > 0) {
            u1 = users.get(0);
        }
        if (users.size() > 1) {
            u2 = users.get(1);
        }
        int maxSize = 8;

        String text = "";

        if (u1 != null && u2 != null) {
            text += StringUtil.truncate(u1.getName(), maxSize) + " vs "
                    + StringUtil.truncate(u2.getName(), maxSize);
        }
        else if (u1 != null) {

            text += StringUtil.truncate(u1.getName(), 15);
        }
        else if (u2 != null) {
            text += StringUtil.truncate(u2.getName(), 15);
        }

        text += " (x" + getPuntosApostados() + getRoomInfo();

        return text;
    }

    protected String getRoomInfo() {
        return ")";
    }

    /**
     * Empty rooms on top
     */
    public int compareTo(AbstractRoom other) {
        return other.getPlayers().size() - getPlayers().size();
    }

    @Override
    public boolean isFull() {
        return getPlayers().size() == 2;
    }
}
