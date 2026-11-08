package daa.a4.edit;

/**
 * Version C - the same answer from two rows only. Time O(n*m), extra memory O(m).
 *
 * DP design:
 *   State:      prev[j] = d[i-1][j] (the previous row),  cur[j] = d[i][j] (the row we are filling now).
 *   Transition: cur[j] = prev[j-1]                                  if a[i-1] == b[j-1]
 *               cur[j] = 1 + min(prev[j], cur[j-1], prev[j-1])      otherwise
 *               Row i only needs row i-1, so older rows can be thrown away.
 *   Base case:  prev[j] = j for row 0;  cur[0] = i at the start of every row i.
 *   Answer:     after the last row is finished and swapped into prev, the answer is prev[m].
 */
public final class TwoRowsEditDistance extends AbstractEditDistance {

    @Override
    public int distance(String a, String b) {
        start(a, b);
        int n = a.length();
        int m = b.length();

        int[] prev = new int[m + 1];
        int[] cur = new int[m + 1];
        allocatedCells += (long) prev.length + cur.length;    // counter: really allocated cells

        for (int j = 0; j <= m; j++) prev[j] = j;             // base case: row 0

        for (int i = 1; i <= n; i++) {
            cur[0] = i;                                       // base case: column 0
            for (int j = 1; j <= m; j++) {
                steps++;                                      // counter: one computed cell
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    cur[j] = prev[j - 1];
                } else {
                    cur[j] = 1 + min3(prev[j], cur[j - 1], prev[j - 1]);
                }
            }
            int[] tmp = prev;                                 // cur becomes the "previous" row
            prev = cur;
            cur = tmp;
        }
        return prev[m];
    }

    @Override
    public String name() {
        return "two_rows";
    }
}
