import java.util.ArrayList;
import java.util.List;

public final class Main {
    private static final String[] NAMES = {
            "Downtown", "Harbor", "Airport", "University", "TechPark", "OldTown", "Stadium"
    };

    private static String name(int v) {
        return NAMES[v];
    }

    private static String path(List<Integer> p) {
        List<String> parts = new ArrayList<>();
        for (int v : p) {
            parts.add(name(v));
        }
        return String.join(" -> ", parts);
    }

    public static void main(String[] args) {
    }
}