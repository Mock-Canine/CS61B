package disc2;
// Use list instead of array
class Planet {
    double x, y, mass;

    Planet(double x, double y, double mass) {
        this.x = x;
        this.y = y;
        this.mass = mass;
    }

    double distanceTo(Planet other) {
        return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
    }

    static double totalMass(Planet[] planets) {
        double total = 0;
        for (Planet p : planets) {
            total += p.mass;
        }
        return total;
    }

    static void main() {
        Planet p1 = new Planet(5, 10, 100);
        Planet p2 = new Planet(1, 2, 200);
        double dis = p1.distanceTo(p2);
        Planet[] arr = {p1, p2};
        double total = Planet.totalMass(arr);
    }
}
