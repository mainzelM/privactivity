package net.privactivity.store.adapter;

import net.privactivity.domain.Maybe;

public record MetaData(Maybe<String> manualTitle) {
    public MetaData(String manualTitle) {
        this(Maybe.some(manualTitle));
    }

    public static MetaData empty() {
        return new MetaData(Maybe.none());
    }

    public MetaData withTitle(String newTitle) {
        return new MetaData(Maybe.some(newTitle));
    }
}
