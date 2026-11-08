package daa.a4.exp;

import java.util.Arrays;
import java.util.Random;

/**
 * Our own input generator. Everything is made from a fixed seed, so the same call always gives the same strings.
 */
public final class Generator {

    private Generator() {
    }

    /** Random string of length len over the first `alphabet` lowercase letters. */
    public static String random(Random rnd, int len, int alphabet) {
        char[] c = new char[len];
        for (int i = 0; i < len; i++) c[i] = (char) ('a' + rnd.nextInt(alphabet));
        return new String(c);
    }

    public static String repeat(char ch, int len) {
        char[] c = new char[len];
        Arrays.fill(c, ch);
        return new String(c);
    }

    private static Random rng(String type, int n, long seed) {
        return new Random(seed * 1_000_003L + n * 31L + type.hashCode());
    }

    /**
     * Pair of strings (a, b), both of length n, for Edit Distance.
     *   random26  - two independent random strings over 26 letters: the "average" input.
     *   identical - b equals a: best case of the recursion (the last chars always match, one call per step).
     *   disjoint  - a = "aaa...", b = "bbb...": no character ever matches, the recursion makes 3 calls
     *               every time: WORST case of the recursion.
     */
    public static String[] editPair(String type, int n, long seed) {
        Random rnd = rng(type, n, seed);
        switch (type) {
            case "random26": {
                String a = random(rnd, n, 26);
                String b = random(rnd, n, 26);
                return new String[]{a, b};
            }
            case "identical": {
                String a = random(rnd, n, 26);
                return new String[]{a, a};
            }
            case "disjoint":
                return new String[]{repeat('a', n), repeat('b', n)};
            default:
                throw new IllegalArgumentException("unknown edit input type: " + type);
        }
    }

    /**
     * Pair (text, pattern) for string search. The text has length n, the pattern has length m.
     *   random26   - random text over 26 letters, the pattern is cut from the text (so there is at least 1 match):
     *                the "average" input, mismatches are found at the first character almost every time.
     *   random2    - random text over 2 letters ('a','b'), pattern cut from the text:
     *                long partial matches happen often, so Naive has to go back often.
     *   aaab_worst - text = "aaa...a", pattern = "aa...ab" (m-1 letters 'a' and one 'b'):
     *                WORST case of Naive: every start compares m characters and fails on the last one.
     */
    public static String[] searchInput(String type, int n, int m, long seed) {
        Random rnd = rng(type, n * 1009 + m, seed);
        switch (type) {
            case "random26":
            case "random2": {
                int alphabet = type.equals("random26") ? 26 : 2;
                String text = random(rnd, n, alphabet);
                int pos = rnd.nextInt(n - m + 1);
                return new String[]{text, text.substring(pos, pos + m)};
            }
            case "aaab_worst":
                return new String[]{repeat('a', n), repeat('a', m - 1) + "b"};
            default:
                throw new IllegalArgumentException("unknown search input type: " + type);
        }
    }
}
