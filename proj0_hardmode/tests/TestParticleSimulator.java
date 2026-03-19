import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import org.junit.Test;

import java.util.Map;

public class TestParticleSimulator {

    @Test
    public void testConstructor_initializesEmptyGrid_usingIndices() {
        int w = 50;
        int h = 60;
        ParticleSimulator simulator = new ParticleSimulator(w, h);

        // 1. Verify the outer array length (Width)
        assertThat(simulator.particles).hasLength(w);

        // 2. Iterate using Integer Indices
        for (int x = 0; x < w; x++) {

            // Verify the inner array length (Height) for this column
            assertThat(simulator.particles[x]).hasLength(h);

            for (int y = 0; y < simulator.height; y++) {
                Particle particle = simulator.particles[x][y];

                // Verify the particle is not null
                assertThat(particle).isNotNull();

                // Verify the particle is initialized to EMPTY
                assertWithMessage("Particle at x=%s, y=%s should be EMPTY", x, y)
                        .that(particle.flavor)
                        .isEqualTo(ParticleFlavor.EMPTY);
            }
        }
    }

    @Test
    public void testValidIndex() {
        // Arrange: Create a grid of 10x20
        int width = 10;
        int height = 20;
        ParticleSimulator sim = new ParticleSimulator(width, height);

        // Assert: Valid Indices (Inside bounds)
        assertThat(sim.validIndex(0, 0)).isTrue();             // Bottom-Left Corner
        assertThat(sim.validIndex(9, 19)).isTrue();            // Top-Right Corner (width-1, height-1)
        assertThat(sim.validIndex(5, 10)).isTrue();            // Middle

        // Assert: Invalid Indices (Outside bounds)
        assertThat(sim.validIndex(-1, 0)).isFalse();           // Negative X
        assertThat(sim.validIndex(0, -1)).isFalse();           // Negative Y
        assertThat(sim.validIndex(10, 0)).isFalse();           // X == Width (Off by one)
        assertThat(sim.validIndex(0, 20)).isFalse();           // Y == Height (Off by one)
        assertThat(sim.validIndex(100, 100)).isFalse();        // Far out of bounds
    }
    @Test
    public void testGetNeighbors() {
        // Arrange: Create a small 3x3 grid
        // (0,2) (1,2) (2,2)
        // (0,1) (1,1) (2,1)
        // (0,0) (1,0) (2,0)
        ParticleSimulator sim = new ParticleSimulator(3, 3);

        // Setup specific particles around the center (1,1) to verify correct mapping
        sim.particles[1][2] = new Particle(ParticleFlavor.WATER); // UP of center
        sim.particles[1][0] = new Particle(ParticleFlavor.SAND);  // DOWN of center
        sim.particles[0][1] = new Particle(ParticleFlavor.FIRE);  // LEFT of center
        sim.particles[2][1] = new Particle(ParticleFlavor.PLANT); // RIGHT of center

        // --- Case 1: Center Particle (All neighbors are within bounds) ---
        Map<Direction, Particle> centerNeighbors = sim.getNeighbors(1, 1);

        assertThat(centerNeighbors.get(Direction.UP).flavor).isEqualTo(ParticleFlavor.WATER);
        assertThat(centerNeighbors.get(Direction.DOWN).flavor).isEqualTo(ParticleFlavor.SAND);
        assertThat(centerNeighbors.get(Direction.LEFT).flavor).isEqualTo(ParticleFlavor.FIRE);
        assertThat(centerNeighbors.get(Direction.RIGHT).flavor).isEqualTo(ParticleFlavor.PLANT);

        // --- Case 2: Bottom-Left Corner (0,0) (Verify Off-Screen is Barrier) ---
        // Neighbors for (0,0):
        // UP: (0,1) -> Fire (from setup above)
        // RIGHT: (1,0) -> Sand (from setup above)
        // DOWN: (0,-1) -> Off screen -> Should be BARRIER
        // LEFT: (-1,0) -> Off screen -> Should be BARRIER

        Map<Direction, Particle> cornerNeighbors = sim.getNeighbors(0, 0);

        // Verify valid neighbors
        assertThat(cornerNeighbors.get(Direction.UP).flavor).isEqualTo(ParticleFlavor.FIRE);
        assertThat(cornerNeighbors.get(Direction.RIGHT).flavor).isEqualTo(ParticleFlavor.SAND);

        // Verify invalid/off-screen neighbors are treated as BARRIER
        assertWithMessage("Off-screen neighbor (Down) should be treated as BARRIER")
                .that(cornerNeighbors.get(Direction.DOWN).flavor).isEqualTo(ParticleFlavor.BARRIER);
        assertWithMessage("Off-screen neighbor (Left) should be treated as BARRIER")
                .that(cornerNeighbors.get(Direction.LEFT).flavor).isEqualTo(ParticleFlavor.BARRIER);
    }
    @Test
    public void testTick_updatesParticlesBottomUp() {
        // Arrange: Create a tall, narrow grid (1 wide, 3 high)
        // Coordinates: (0,0) is bottom, (0,2) is top
        ParticleSimulator sim = new ParticleSimulator(1, 3);

        // Setup a stack of sand with a gap at the bottom
        sim.particles[0][0] = new Particle(ParticleFlavor.EMPTY); // Bottom
        sim.particles[0][1] = new Particle(ParticleFlavor.SAND);  // Middle
        sim.particles[0][2] = new Particle(ParticleFlavor.SAND);  // Top

        // Act: Run one simulation step
        sim.tick();

        // Assert: Both particles should have moved down one step

        // 1. The bottom spot (0,0) catches the first falling sand
        assertThat(sim.particles[0][0].flavor).isEqualTo(ParticleFlavor.SAND);

        // 2. The middle spot (0,1) catches the second falling sand
        // (If the loop ran top-down, this would be EMPTY because the top sand would have been blocked)
        assertThat(sim.particles[0][1].flavor).isEqualTo(ParticleFlavor.SAND);

        // 3. The top spot (0,2) should now be empty
        assertThat(sim.particles[0][2].flavor).isEqualTo(ParticleFlavor.EMPTY);
    }
}
