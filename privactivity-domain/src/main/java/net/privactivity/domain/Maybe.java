package net.privactivity.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Supplier;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "none", value = None.class),
        @JsonSubTypes.Type(name = "some", value = Some.class)
})
public sealed interface Maybe<T> permits None, Some {

    None<?> NONE = new None<>();

    static <T> Some<T> some(T x) {
        return new Some<>(x);
    }

    static <T> None<T> none() {
        return (None<T>) Maybe.NONE;
    }

    static <T> Maybe<T> nullAsNone(T x) {
        if (x == null) {
            return none();
        } else {
            return some(x);
        }
    }

    <R> Maybe<R> map(Function<T, R> f);

    T orElse(T e);
    
    T orElseGet(Supplier<T> supplier);

    T orThrow();

    @JsonIgnore
    boolean isPresent();

    static <R> Maybe<R> fromOptional(Optional<R> optional) {
        if (optional.isPresent()) {
            return some(optional.get());
        } else {
            return none();
        }
    }

    static Maybe<Double> fromOptional(OptionalDouble optional) {
        if (optional.isPresent()) {
            return some(optional.getAsDouble());
        } else {
            return none();
        }
    }

    static Maybe<Integer> fromOptional(OptionalInt optional) {
        if (optional.isPresent()) {
            return some(optional.getAsInt());
        } else {
            return none();
        }
    }
}


