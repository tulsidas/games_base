package common.model;

public class FloodControl {
    private long timestamp;

    private int count;

    public FloodControl() {
        count = 0;
        timestamp = System.currentTimeMillis();
    }

    /** actualiza timestamp y contador, devuelve true si esta floodeando */
    public boolean isFlooding() {
        long now = System.currentTimeMillis();
        if (now - timestamp < 1000) {
            count++;
        }
        else {
            count = 0;
        }

        timestamp = now;
        return count > 3;
    }
}
