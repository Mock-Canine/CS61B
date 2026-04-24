import main.Graph;
import main.WorldNet;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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
        String nothingness = wn.getHyponyms("MockCanine").toString();
        assertThat(nothingness).isEqualTo("[]");

        // Test case: including itself, in order
        String easy = wn.getHyponyms("action").toString();
        List<String> easyResult = List.of("action", "change", "demotion", "variation");
        assertThat(easy).isEqualTo(easyResult.toString());

        // Test case: no repeated words, handle connections
        String verbose = wn.getHyponyms("natural_event").toString();
        // Repeated: alteration, modification
        List<String> expected = new ArrayList<>(List.of("happening", "occurrence", "occurrent", "natural_event", "change",
                "alteration", "modification", "transition", "increase", "leap", "jump", "saltation", "adjustment",
                "conversion", "mutation"));
        expected.sort(null);
        assertThat(verbose).isEqualTo(expected.toString());
    }
}
