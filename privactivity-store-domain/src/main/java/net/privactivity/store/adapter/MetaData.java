package net.privactivity.store.adapter;

import net.privactivity.domain.Maybe;
import java.util.Objects;

public record MetaData(Maybe<String> manualTitle) {
    public MetaData(String manualTitle) {
        Objects.requireNonNull(manualTitle, "manualTitle must not be null");
        if (manualTitle.isEmpty()) {
            throw new IllegalStateException("manualTitle must not be empty");
        }
        this(Maybe.some(manualTitle));
    }

    public static MetaData empty() {
        return new MetaData(Maybe.none());
    }

    public MetaData withTitle(String newTitle) {
        Objects.requireNonNull(newTitle, "newTitle must not be null");
        if (newTitle.isEmpty()) {
            throw new IllegalStateException("newTitle must not be empty");
        }
        return new MetaData(Maybe.some(newTitle));
    }
}
