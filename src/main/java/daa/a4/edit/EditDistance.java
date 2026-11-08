package daa.a4.edit;

/**
 * One interface for all three Edit Distance versions, so the experiment is one loop.
 *
 * Edit distance = the smallest number of insertions, deletions and replacements
 * that turn string a into string b.
 */
public interface EditDistance {

    int distance(String a, String b);

    /** Short name used in results.csv. */
    String name();

    /**
     * Counter 1 ("units of work") of the LAST call of distance().
     * Recursive version: number of recursive calls.
     * Table / two-rows versions: number of table cells that were computed.
     */
    long steps();

    /**
     * Counter 2 (memory) of the LAST call of distance():
     * how many int cells the algorithm really allocated (sum of array lengths).
     */
    long allocatedCells();
}
