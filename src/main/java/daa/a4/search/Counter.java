package daa.a4.search;

/** A tiny mutable counter, so a static method (lps) can also report how much work it did. */
final class Counter {
    long value;

    void inc() {
        value++;
    }
}
