package net.privactivity.domain;

import java.util.function.Function;
import java.util.function.Supplier;

public record Some<T>(T value) implements Maybe<T> {

    @Override
    public <R> Maybe<R> map(Function<T, R> f) {
        return Maybe.some(f.apply(value));
    }

    @Override
    public T orElse(T e) {
        return value;
    }

    @Override
    public T orElseGet(Supplier<T> supplier) {
        return value;
    }

    @Override
    public T orThrow() {
        return value;
    }

    @Override
    public boolean isPresent() {
        return true;
    }
}
