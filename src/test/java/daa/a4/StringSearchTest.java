package daa.a4;

import daa.a4.exp.Generator;
import daa.a4.search.KmpSearch;
import daa.a4.search.NaiveSearch;
import daa.a4.search.StringSearch;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringSearchTest {

    private static List<StringSearch> algorithms() {
        return List.of(new NaiveSearch(), new KmpSearch());
    }

    // ---------------- LPS ----------------

    @Test
    void lpsReferenceArrays() {
        assertArrayEquals(new int[]{0, 1, 2, 0, 1, 2, 3, 3}, KmpSearch.lps("AAACAAAA"));
        assertArrayEquals(new int[]{0, 0, 1, 2, 0, 1, 2, 3, 4}, KmpSearch.lps("ABABCABAB"));
    }

    @Test
    void lpsSmallCases() {
        assertArrayEquals(new int[]{0}, KmpSearch.lps("A"));
        assertArrayEquals(new int[]{0, 0, 0}, KmpSearch.lps("ABC"));
        assertArrayEquals(new int[]{0, 1, 2, 3}, KmpSearch.lps("AAAA"));
    }

    // ---------------- search: reference values ----------------

    @Test
    void referenceMatches() {
        for (StringSearch s : algorithms()) {
            String who = s.name();
            assertEquals(List.of(10), s.search("ABABDABACDABABCABAB", "ABABCABAB"), who);
            assertEquals(List.of(0, 1, 2, 3), s.search("AAAAA", "AA"), who);
            assertEquals(List.of(0, 2, 4), s.search("ABABABA", "ABA"), who);
        }
    }

    @Test
    void noMatchReturnsEmptyList() {
        for (StringSearch s : algorithms()) {
            assertTrue(s.search("ABCDEF", "XYZ").isEmpty(), s.name());
            assertTrue(s.search("AAAA", "AB").isEmpty(), s.name());
        }
    }

    @Test
    void matchAtTheVeryStartAndTheVeryEnd() {
        for (StringSearch s : algorithms()) {
            assertEquals(List.of(0, 4), s.search("abXXab", "ab"), s.name());
            assertEquals(List.of(0), s.search("abc", "abc"), s.name());
            assertEquals(List.of(2), s.search("xxa", "a"), s.name());
        }
    }

    // ---------------- edge cases ----------------

    @Test
    void emptyPatternThrows() {
        for (StringSearch s : algorithms()) {
            assertThrows(IllegalArgumentException.class, () -> s.search("abc", ""), s.name());
            assertThrows(IllegalArgumentException.class, () -> s.search("", ""), s.name());
        }
    }

    @Test
    void patternLongerThanTextReturnsEmptyList() {
        for (StringSearch s : algorithms()) {
            assertTrue(s.search("ab", "abc").isEmpty(), s.name());
            assertTrue(s.search("", "a").isEmpty(), s.name());
        }
    }

    // ---------------- cross check ----------------

    @Test
    void naiveAndKmpGiveTheSameListOnRandomTexts() {
        Random rnd = new Random(2024);
        StringSearch naive = new NaiveSearch();
        StringSearch kmp = new KmpSearch();
        for (int t = 0; t < 100; t++) {
            int alphabet = 2 + rnd.nextInt(2);                         // 2 or 3 letters: many overlaps
            String text = Generator.random(rnd, rnd.nextInt(60), alphabet);
            String pattern = Generator.random(rnd, 1 + rnd.nextInt(5), alphabet);
            assertEquals(naive.search(text, pattern), kmp.search(text, pattern),
                    "text='" + text + "' pattern='" + pattern + "'");
        }
    }

    @Test
    void resultIsInIncreasingOrder() {
        Random rnd = new Random(5);
        String text = Generator.random(rnd, 500, 2);
        for (StringSearch s : algorithms()) {
            List<Integer> r = s.search(text, "ab");
            for (int i = 1; i < r.size(); i++) assertTrue(r.get(i - 1) < r.get(i), s.name());
        }
    }

    // ---------------- counters (measured inside the algorithms) ----------------

    @Test
    void counterNaiveWorstCaseDoesAboutNTimesMComparisons() {
        NaiveSearch naive = new NaiveSearch();
        int n = 1000, m = 10;
        String[] in = Generator.searchInput("aaab_worst", n, m, 1);
        assertTrue(naive.search(in[0], in[1]).isEmpty());
        assertEquals((long) (n - m + 1) * m, naive.comparisons());      // every start costs exactly m
    }

    @Test
    void counterKmpNeverDoesMoreThanTwoNComparisons() {
        KmpSearch kmp = new KmpSearch();
        int n = 1000, m = 10;
        for (String type : List.of("aaab_worst", "random2", "random26")) {
            String[] in = Generator.searchInput(type, n, m, 1);
            kmp.search(in[0], in[1]);
            assertTrue(kmp.comparisons() <= 2L * n, type + ": " + kmp.comparisons());
            assertTrue(kmp.preprocessingComparisons() <= 2L * m, type);
        }
    }

    @Test
    void generatorIsReproducible() {
        assertArrayEquals(Generator.editPair("random26", 50, 42), Generator.editPair("random26", 50, 42));
        assertArrayEquals(Generator.searchInput("random2", 500, 20, 42), Generator.searchInput("random2", 500, 20, 42));
    }
}
