package rat.master;

import java.awt.*;
import java.io.Serializable;

public class WebcamInfo implements Serializable {

    private static final long serialVersionUID = 6716675421534555788L;

    private String name;

    private Dimension resolution;

    public WebcamInfo(String name, Dimension resolution) {
        this.name = name;
        this.resolution = resolution;
    }

    public Dimension getResolution() {
        return resolution;
    }

    public String getName() {
        return name;
    }
}
