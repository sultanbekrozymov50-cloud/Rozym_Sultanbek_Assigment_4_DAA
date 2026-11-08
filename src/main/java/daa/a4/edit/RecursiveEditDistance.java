package daa.a4.edit;

/**
 * Version A - plain recursion, no memory. Slow on purpose.
 *
 * DP design:
 *   State:      f(i, j) = edit distance between the first i chars of a and the first j chars of b.
 *   Transition: if a[i-1] == b[j-1]  ->  f(i, j) = f(i-1, j-1)            (last chars are equal, nothing to pay)
 *               else                 ->  f(i, j) = 1 + min( f(i-1, j),     (delete a[i-1])
 *                                                           f(i, j-1),     (insert b[j-1])
 *                                                           f(i-1, j-1) )  (replace a[i-1] by b[j-1])
 *   Base case:  f(0, j) = j  (insert j chars),   f(i, 0) = i  (delete i chars).
 *
 * Why it is slow: the same f(i, j) is computed again and again, because nothing is stored.
 * If the last chars differ, one call makes THREE calls, so the number of calls grows exponentially.
 * (If the last chars are equal we make only ONE call - that is why identical strings are the best case.)
 */
public final class RecursiveEditDistance extends AbstractEditDistance {

    @Override
    public int distance(String a, String b) {
        start(a, b);
        return f(a, b, a.length(), b.length());
    }

    private int f(String a, String b, int i, int j) {
        steps++;                                   // counter: one recursive call = one unit of work
        if (i == 0) return j;                      // base case
        if (j == 0) return i;                      // base case
        if (a.charAt(i - 1) == b.charAt(j - 1)) {
            return f(a, b, i - 1, j - 1);
        }
        int delete  = f(a, b, i - 1, j);
        int insert  = f(a, b, i, j - 1);
        int replace = f(a, b, i - 1, j - 1);
        return 1 + min3(delete, insert, replace);
    }

    @Override
    public String name() {
        return "recursive";
    }
}
