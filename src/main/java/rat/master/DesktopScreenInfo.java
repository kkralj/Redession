package rat.master;

import java.io.Serializable;

public class DesktopScreenInfo implements Serializable {

    private static final long serialVersionUID = -7351264005280508550L;

    private String id;
    private int width, height;
    private double minX, minY;

    public DesktopScreenInfo(String id, int width, int height, double minX, double minY) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.minX = minX;
        this.minY = minY;
    }

    public String getId() {
        return id;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }
}
