package disc4;

public class OHRequest {
    public String description;
    public String name;
    public boolean isSetup;
    public OHRequest next;

    public OHRequest(String d, String n, boolean i, OHRequest next) {
        description = d;
        name = n;
        isSetup = i;
        this.next = next;
    }
}
