package daa.a4.search;

import java.util.ArrayList;
import java.util.List;

/**
 * Knuth-Morris-Pratt search.
 *
 * LPS array: lps[i] = length of the longest proper prefix of p[0..i] that is also a suffix of p[0..i].
 *            ("proper" = not the whole string). It tells us, after a mismatch, how many characters
 *            of the pattern are STILL matched, so we never re-check them.
 *
 * Search: the text index i only goes forward. The pattern index j says how many characters match now.
 *         On a mismatch we do j = lps[j - 1] (the pattern moves, the text does not).
 *         Every step either moves i forward or makes j smaller, and j can only grow when i moves,
 *         so the search needs at most 2n comparisons -> O(n). Building LPS needs at most 2m -> O(m).
 */
public final class KmpSearch implements StringSearch {

    private long comparisons;
    private long preprocessingComparisons;

    /** Builds the LPS array of pattern p. Public, so tests can call it. Example: lps("ABABCABAB") = [0,0,1,2,0,1,2,3,4]. */
    public static int[] lps(String p) {
        return lps(p, new Counter());
    }

    static int[] lps(String p, Counter counter) {
        int m = p.length();
        int[] lps = new int[m];                           // lps[0] = 0 always
        int len = 0;                                      // length of the current matched prefix-suffix
        int i = 1;
        while (i < m) {
            counter.inc();                                // counter: one character comparison
            if (p.charAt(i) == p.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else if (len > 0) {
                len = lps[len - 1];                       // try a shorter prefix-suffix, i does not move
            } else {
                lps[i] = 0;
                i++;
            }
        }
        return lps;
    }

    @Override
    public List<Integer> search(String text, String pattern) {
        if (text == null || pattern == null) throw new IllegalArgumentException("text and pattern must not be null");
        if (pattern.isEmpty()) throw new IllegalArgumentException("pattern must not be empty");
        comparisons = 0;
        preprocessingComparisons = 0;

        List<Integer> result = new ArrayList<>();
        int n = text.length();
        int m = pattern.length();
        if (m > n) return result;                         // pattern longer than text -> no match

        Counter lpsCounter = new Counter();
        int[] lps = lps(pattern, lpsCounter);
        preprocessingComparisons = lpsCounter.value;

        int j = 0;                                        // how many pattern characters match right now
        for (int i = 0; i < n; i++) {                     // i never goes backwards
            char c = text.charAt(i);
            while (true) {
                comparisons++;                            // counter: one character comparison
                if (c == pattern.charAt(j)) {
                    j++;
                    break;
                }
                if (j == 0) break;                        // nothing matched, go to the next text char
                j = lps[j - 1];                           // mismatch: move the pattern, not the text
            }
            if (j == m) {                                 // full match ends at i
                result.add(i - m + 1);
                j = lps[j - 1];                           // keep the longest border -> overlapping matches work
            }
        }
        return result;
    }

    @Override
    public String name() {
        return "kmp";
    }

    @Override
    public long comparisons() {
        return comparisons;
    }

    @Override
    public long preprocessingComparisons() {
        return preprocessingComparisons;
    }
}
