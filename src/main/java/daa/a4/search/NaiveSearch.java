package daa.a4.search;

import java.util.ArrayList;
import java.util.List;

/**
 * Naive search: try every start position s and compare the pattern with the text character by character.
 * After a mismatch (or a full match) the text pointer goes back and we start at s + 1.
 * Worst case O(n*m): for example text "aaaa...a" and pattern "aa...ab".
 */
public final class NaiveSearch implements StringSearch {

    private long comparisons;

    @Override
    public List<Integer> search(String text, String pattern) {
        if (text == null || pattern == null) throw new IllegalArgumentException("text and pattern must not be null");
        if (pattern.isEmpty()) throw new IllegalArgumentException("pattern must not be empty");
        comparisons = 0;

        List<Integer> result = new ArrayList<>();
        int n = text.length();
        int m = pattern.length();
        if (m > n) return result;                         // pattern longer than text -> no match

        for (int s = 0; s <= n - m; s++) {                // every possible start
            int k = 0;
            while (k < m) {
                comparisons++;                            // counter: one character comparison
                if (text.charAt(s + k) != pattern.charAt(k)) break;
                k++;
            }
            if (k == m) result.add(s);                    // all m characters matched
        }
        return result;
    }

    @Override
    public String name() {
        return "naive";
    }

    @Override
    public long comparisons() {
        return comparisons;
    }

    @Override
    public long preprocessingComparisons() {
        return 0;
    }
}
