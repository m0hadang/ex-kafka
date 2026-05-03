import java.util.Map.Entry;

var stream = List.of(
    Map.entry("a", 1),
    Map.entry("b", 1),
    Map.entry("a", 2),
);

var table = new HashMap<String, Integer>();

stream.forEach(entry -> {
    table.put(entry.getKey(), entry.getValue());
});

// stream ==> [a=1, b=1, a=2]
// table ==> [a=2, b=1]