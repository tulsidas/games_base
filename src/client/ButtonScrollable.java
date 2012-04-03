package client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pulpcore.image.CoreFont;
import pulpcore.sprite.Button;

public abstract class ButtonScrollable<T extends Comparable< ? super T>>
        extends Scrollable<T> {

    protected Map<Button, T> map;

    protected CoreFont font;

    public ButtonScrollable(int x, int y, int w, int h, CoreFont font) {
        super(x, y, w, h);
        this.font = font;
        this.map = new HashMap<Button, T>();
    }

    public void createContent(List<T> objects) {
        int posY = 0;

        // clear stored buttons
        map.clear();

        for (T t : objects) {
            Button but = createButton(t, 0, posY);

            add(but);

            // save the button
            map.put(but, t);

            // ignore lineSpacing
            posY += getLineSpacing();
        }
    }

    /**
     * Hook for subclasses
     * 
     * @param t
     * @return
     */
    protected abstract Button createButton(T t, int x, int y);

    public void update(int elapsedTime) {
        super.update(elapsedTime);

        // check for every button, and trigger event if clicked
        for (Button b : map.keySet()) {
            if (b.isClicked()) {
                buttonClicked(map.get(b));
            }
        }
    }

    /**
     * Called when a button of this scrollable was clicked
     * 
     * @param t
     *            the object the button was created for (the originator of the
     *            click)
     */
    public abstract void buttonClicked(T t);
}
