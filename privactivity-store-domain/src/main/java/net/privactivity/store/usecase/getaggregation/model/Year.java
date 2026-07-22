package net.privactivity.store.usecase.getaggregation.model;

public record Year(int year) implements Code, Comparable<Year> {
    @Override
    public int compareTo(Year o) {
        return Integer.compare(this.year, o.year);
    }
}
