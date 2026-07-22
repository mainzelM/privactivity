package net.privactivity.store.usecase.getaggregation.model;

public record Month(int month) implements Code, Comparable<Month> {
    @Override
    public int compareTo(Month o) {
        return Integer.compare(this.month, o.month);
    }
}
