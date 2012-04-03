package client;

import java.util.Comparator;

import pulpcore.image.CoreFont;

import common.model.User;

public class PlayersBox extends LabelScrollable {

    public PlayersBox(int x, int y, int w, int h, CoreFont font) {
        super(x, y, w, h, font);

        setSorted(true);
        setComparator(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if (o1 != null && o2 != null) {
                    return o1.toLowerCase().compareTo(o2.toLowerCase());
                }
                else {
                    return 0;
                }
            }
        });
    }

    public void addPlayer(User who) {
        if (who != null) {
            String str = truncate(who.getName());
            if (!contains(str)) {
                addItem(str);
            }
        }
    }

    public void removePlayer(User who) {
        if (who != null) {
            super.removeItem(truncate(who.getName()));
        }
    }

    private final String truncate(String str) {
        boolean acortado = false;

        while (font.getStringWidth(str) > getAvailableSpace()) {
            str = str.substring(0, str.length() - 1);
            acortado = true;
        }

        if (acortado) {
            str = str.substring(0, str.length() - 3) + "...";
        }

        return str;
    }
}
