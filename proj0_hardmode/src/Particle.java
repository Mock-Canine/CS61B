import org.eclipse.jetty.util.HttpCookieStore;

import java.util.Map;
import java.awt.Color;

public class Particle {
    public static final int PLANT_LIFESPAN = 150;
    public static final int FLOWER_LIFESPAN = 75;
    public static final int FIRE_LIFESPAN = 10;
    public static final Map<ParticleFlavor, Integer> LIFESPANS =
        Map.of(ParticleFlavor.FLOWER, FLOWER_LIFESPAN,
                ParticleFlavor.PLANT, PLANT_LIFESPAN,
                ParticleFlavor.FIRE, FIRE_LIFESPAN);
    ParticleFlavor flavor;
    int lifespan;

    Particle(ParticleFlavor f) {
        flavor = f;
        lifespan = switch (f) {
            case FLOWER -> FLOWER_LIFESPAN;
            case PLANT -> PLANT_LIFESPAN;
            case FIRE -> FIRE_LIFESPAN;
            default -> -1;
        };
    }

    public Color color() {
        return switch (flavor) {
            case EMPTY -> Color.BLACK;
            case SAND -> Color.YELLOW;
            case BARRIER -> Color.GRAY;
            case WATER -> Color.BLUE;
            case FOUNTAIN -> Color.CYAN;
            case PLANT -> new Color(0, 255, 0);
            case FIRE -> new Color(255, 0, 0);
            case FLOWER -> new Color(255, 141, 161);
            default -> null;
        };
    }

    void moveInto(Particle other) {
        other.flavor = flavor;
        other.lifespan = lifespan;
        flavor = ParticleFlavor.EMPTY;
        lifespan = -1;
    }

    void fall(Map<Direction, Particle> neighbors) {
        Particle down = neighbors.get(Direction.DOWN);
        if (down.flavor == ParticleFlavor.EMPTY) {
            moveInto(down);
        }
    }
}
