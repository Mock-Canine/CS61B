import edu.princeton.cs.algs4.StdRandom;

import java.util.Map;
import java.awt.Color;
import java.util.Set;

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
            case PLANT -> {
                double ratio = (double) Math.max(0, Math.min(lifespan, PLANT_LIFESPAN)) / PLANT_LIFESPAN;
                int g = 120 + (int) Math.round((255 - 120) * ratio);
                yield new Color(0, g, 0);
            }
            case FIRE -> {
                double ratio = (double) Math.max(0, Math.min(lifespan, FIRE_LIFESPAN)) / FIRE_LIFESPAN;
                int r = (int) Math.round(255 * ratio);
                yield new Color(r, 0, 0);
            }
            case FLOWER -> {
                double ratio = (double) Math.max(0, Math.min(lifespan, FLOWER_LIFESPAN)) / FLOWER_LIFESPAN;
                int r = 120 + (int) Math.round((255 - 120) * ratio);
                int g = 70 + (int) Math.round((141 - 70) * ratio);
                int b = 80 + (int) Math.round((161 - 80) * ratio);
                yield new Color(r, g, b);
            }
        };
    }

    void breed(Particle other) {
        other.flavor = flavor;
        other.lifespan = LIFESPANS.get(flavor);
    }

    void moveInto(Particle other) {
        other.flavor = flavor;
        other.lifespan = lifespan;
        flavor = ParticleFlavor.EMPTY;
        lifespan = -1;
    }

    void moveIntoDirect(Map<Direction, Particle> neighbors, Direction d) {
        Particle p = neighbors.get(d);
        if (p.flavor == ParticleFlavor.EMPTY) {
            moveInto(p);
        }
    }

    void breedDirect(Map<Direction, Particle> neighbors, Direction d) {
        Particle p = neighbors.get(d);
        if (p.flavor == ParticleFlavor.EMPTY) {
            breed(p);
        }
    }

    void burn(Map<Direction, Particle> neighbors) {
        for (Particle p : neighbors.values()) {
            if (Set.of(ParticleFlavor.PLANT, ParticleFlavor.FLOWER).contains(p.flavor)) {
                switch (StdRandom.uniformInt(5)) {
                    case 0, 1, 2 -> {}
                    case 3, 4 -> breed(p);
                }
            }
        }
    }

    void grow(Map<Direction, Particle> neighbors) {
        switch (StdRandom.uniformInt(10)) {
            case 0, 1, 2, 3, 4, 5, 6 -> {}
            case 7 -> breedDirect(neighbors, Direction.UP);
            case 8 -> breedDirect(neighbors, Direction.LEFT);
            case 9 -> breedDirect(neighbors, Direction.RIGHT);
        }
    }

    void fall(Map<Direction, Particle> neighbors) {
        moveIntoDirect(neighbors, Direction.DOWN);
    }

    void flow(Map<Direction, Particle> neighbors) {
        switch (StdRandom.uniformInt(3)) {
            case 0 -> {}
            case 1 -> moveIntoDirect(neighbors, Direction.LEFT);
            case 2 -> moveIntoDirect(neighbors, Direction.RIGHT);
        }
    }

    void decrementLifespan() {
        if (lifespan > 0) {
            lifespan--;
        } else if (lifespan == 0){
            flavor = ParticleFlavor.EMPTY;
            lifespan = -1;
        }
    }

    void action(Map<Direction, Particle> neighbors) {
        if (flavor == ParticleFlavor.EMPTY) {
            return;
        }
        if (flavor != ParticleFlavor.BARRIER) {
            fall(neighbors);
        }
        if (flavor == ParticleFlavor.WATER) {
            flow(neighbors);
        }
        if (flavor == ParticleFlavor.PLANT || flavor == ParticleFlavor.FLOWER) {
            grow(neighbors);
        }
        if (flavor == ParticleFlavor.FIRE) {
            burn(neighbors);
        }
    }
}
