package net.privactivity.domain;

import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;

public record None<T>() implements Maybe<T> {
    @Override
    public <R> Maybe<R> map(Function<T, R> f) {
        return Maybe.none();
    }

    @Override
    public T orElse(T e) {
        return e;
    }

    @Override
    public T orElseGet(Supplier<T> supplier) {
        return supplier.get();
    }

    @Override
    public T orThrow() {
        throw new NoSuchElementException();
    }

    @Override
    public boolean isPresent() {
        return false;
    }
}
