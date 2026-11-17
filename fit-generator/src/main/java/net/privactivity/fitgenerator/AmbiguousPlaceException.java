package net.privactivity.fitgenerator;

import java.util.List;

public class AmbiguousPlaceException extends GenerationException {

    private final List<String> candidates;

    public AmbiguousPlaceException(String townName, List<String> candidates) {
        super("'" + townName + "' is ambiguous. Use one of these names for the next run:\n  "
              + String.join("\n  ", candidates));
        this.candidates = List.copyOf(candidates);
    }

    public List<String> candidates() {
        return candidates;
    }
}
