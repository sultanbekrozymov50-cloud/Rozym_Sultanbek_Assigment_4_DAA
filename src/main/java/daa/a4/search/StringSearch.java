package daa.a4.search;

import java.util.List;

/**
 * Finds ALL occurrences (also overlapping ones) of a pattern in a text.
 * Returns start indices (from 0) in increasing order. No match -> empty list.
 */
public interface StringSearch {

    /**
     * @throws IllegalArgumentException if the pattern is empty
     */
    List<Integer> search(String text, String pattern);

    String name();

    /** Counter of the LAST call of search(): how many times two characters were compared. */
    long comparisons();

    /** Extra counter: comparisons done while preparing the pattern (only KMP builds an LPS array). */
    long preprocessingComparisons();
}
