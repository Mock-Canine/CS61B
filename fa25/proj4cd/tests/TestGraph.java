import main.Graph;
import main.WorldNet;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static main.Main.*;

import static com.google.common.truth.Truth.assertThat;

public class TestGraph {

    @Test
    public void testGraph() {
        Graph graph = new Graph();
        // Test case: not add, query
        List<String> edges1 = new ArrayList<>(graph.adj("1"));
        assertThat(edges1).isEmpty();
        // add an edge, query
        graph.addEdge("2", "1");
        List<String> edges2 = new ArrayList<>(graph.adj("2"));
        assertThat(edges2).isEqualTo(List.of("1"));
        assertThat(edges1).isEmpty();
        // add another edge
        graph.addEdge("2", "5");
        edges2 = new ArrayList<>(graph.adj("2"));
        assertThat(edges2).isEqualTo(List.of("1", "5"));
    }

    @Test
    public void testWorldNetBasic() {
        WorldNet wn = new WorldNet(SYNSETS_SIZE16_FILE, HYPONYMS_SIZE16_FILE);
        // Test case: query words not in the dataset
        Set<String> nothingness = wn.getHyponyms("MockCanine");
        assertThat(nothingness).isEqualTo(new HashSet<String>());

        // Test case: including itself, no order now
        Set<String> easy = wn.getHyponyms("action");
        Set<String> easyResult = Set.of("action", "change", "demotion", "variation");
        assertThat(easy).isEqualTo(easyResult);

        // Test case: no repeated words, handle connections
        Set<String> verbose = wn.getHyponyms("natural_event");
        Set<String> expected = Set.of("happening", "occurrence", "occurrent", "natural_event", "change",
                "alteration", "modification", "transition", "increase", "leap", "jump", "saltation", "adjustment",
                "conversion", "mutation");
        assertThat(verbose).isEqualTo(expected);
    }
}
