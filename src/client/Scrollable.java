package client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import pulpcore.sprite.ScrollPane;

public abstract class Scrollable<T extends Comparable< ? super T>> extends
        ScrollPane {

    protected int maxLines = 150;

    private boolean needsRefresh;

    protected List<T> objects;

    private final int availableSpace;

    private boolean sorted, scrollOnRefresh;

    private Comparator< ? super T> comparator;

    public Scrollable(int x, int y, int w, int h) {
        super(x, y, w, h);

        objects = new ArrayList<T>(maxLines / 2);
        availableSpace = w - ScrollPane.SCROLLBAR_WIDTH;
        scrollOnRefresh = true;

        setAnimationDuration(60, 250);
    }

    public void refresh() {
        setScrollUnitSize(getLineSpacing());

        // Trim the log if there are too many lines.
        while (objects.size() > maxLines) {
            objects.remove(0);
        }

        if (isSorted()) {
            if (comparator != null) {
                Collections.sort(objects, comparator);
            }
            else {
                Collections.sort(objects);
            }
        }

        removeAll();
        createContent(objects);

        if (scrollOnRefresh) {
            scrollEnd();
        }
    }

    /**
     * Creates the content Sprites of this Scrollable
     * 
     * @param objects
     *            the objects this Scrollable holds
     * @param lineSpacing
     *            the line spacing between Sprites
     */
    public abstract void createContent(List<T> objects);

    public abstract int getLineSpacing();

    /**
     * @return the maximum number of objects this Scrollable can have before it
     *         starts trimming
     */
    public int getMaxLines() {
        return maxLines;
    }

    /**
     * sets the maximum number of objects this Scrollable can have before it
     * starts trimming
     */
    public void setMaxLines(int maxLines) {
        this.maxLines = maxLines;
    }

    public void setScrollOnRefresh(boolean scrollOnRefresh) {
        this.scrollOnRefresh = scrollOnRefresh;
    }

    public void update(int elapsedTime) {
        super.update(elapsedTime);

        if (needsRefresh) {
            refresh();
            needsRefresh = false;
        }
    }

    protected void addItem(T t) {
        objects.add(t);
        needsRefresh = true;
    }

    protected void removeItem(Object obj) {
        objects.remove(obj);
        needsRefresh = true;
    }

    protected List<T> getObjects() {
        return objects;
    }

    protected void clear() {
        objects.clear();
        needsRefresh = true;
    }

    public boolean contains(T t) {
        return objects.contains(t);
    }

    public int getAvailableSpace() {
        return availableSpace;
    }

    public boolean isSorted() {
        return sorted;
    }

    public void needsRefresh() {
        needsRefresh = true;
    }

    public void setSorted(boolean sorted) {
        this.sorted = sorted;
    }

    public Comparator< ? super T> getComparator() {
        return comparator;
    }

    public void setComparator(Comparator< ? super T> comparator) {
        this.comparator = comparator;
    }
}