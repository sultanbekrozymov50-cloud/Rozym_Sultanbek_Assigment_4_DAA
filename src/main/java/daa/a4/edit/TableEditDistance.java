package daa.a4.edit;

/**
 * Version B - bottom-up table, (n+1) x (m+1) cells, time O(n*m), memory O(n*m).
 *
 * DP design:
 *   State:      d[i][j] = edit distance between the first i chars of a and the first j chars of b.
 *   Transition: d[i][j] = d[i-1][j-1]                                  if a[i-1] == b[j-1]
 *               d[i][j] = 1 + min(d[i-1][j], d[i][j-1], d[i-1][j-1])   otherwise
 *               (up = delete, left = insert, diagonal = replace)
 *   Base case:  d[i][0] = i (first column),  d[0][j] = j (first row).
 *   Answer:     d[n][m]. Cells are filled row by row, so every cell we need is already ready.
 */
public final class TableEditDistance extends AbstractEditDistance {

    @Override
    public int distance(String a, String b) {
        start(a, b);
        int n = a.length();
        int m = b.length();

        int[][] d = new int[n + 1][m + 1];
        allocatedCells += (long) d.length * d[0].length;      // counter: really allocated cells

        for (int i = 0; i <= n; i++) d[i][0] = i;             // base case
        for (int j = 0; j <= m; j++) d[0][j] = j;             // base case

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                steps++;                                      // counter: one computed cell
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    d[i][j] = d[i - 1][j - 1];
                } else {
                    d[i][j] = 1 + min3(d[i - 1][j], d[i][j - 1], d[i - 1][j - 1]);
                }
            }
        }
        return d[n][m];
    }

    @Override
    public String name() {
        return "table";
    }
}
