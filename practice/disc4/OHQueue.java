package disc4;

import java.util.Iterator;

public class OHQueue implements Iterable<OHRequest> {
    private OHRequest request;
    public OHQueue(OHRequest request) {
        this.request = request;
    }

    @Override
    public Iterator<OHRequest> iterator() {
        return new OHIterator(request);
    }

    static void main() {
        OHRequest s1 = new OHRequest("git: not work, thank u", "MockCanine", true, null);
        OHQueue q = new OHQueue(s1);
        for (OHRequest r : q) {
            IO.println(r.name);
        }
    }
}
