package daa.a4;

import daa.a4.edit.EditDistance;
import daa.a4.edit.RecursiveEditDistance;
import daa.a4.edit.TableEditDistance;
import daa.a4.edit.TwoRowsEditDistance;
import daa.a4.exp.Generator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EditDistanceTest {

    private static List<EditDistance> versions() {
        return List.of(new RecursiveEditDistance(), new TableEditDistance(), new TwoRowsEditDistance());
    }

    @Test
    void referenceValues() {
        for (EditDistance ed : versions()) {
            String who = ed.name();
            assertEquals(3, ed.distance("kitten", "sitting"), who);
            assertEquals(2, ed.distance("flaw", "lawn"), who);
            assertEquals(5, ed.distance("intention", "execution"), who);
            assertEquals(3, ed.distance("", "abc"), who);
            assertEquals(0, ed.distance("abc", "abc"), who);
        }
    }

    @Test
    void emptyStrings() {
        for (EditDistance ed : versions()) {
            String who = ed.name();
            assertEquals(0, ed.distance("", ""), who);
            assertEquals(3, ed.distance("abc", ""), who);
            assertEquals(3, ed.distance("", "abc"), who);
            assertEquals(1, ed.distance("", "x"), who);
        }
    }

    @Test
    void threeVersionsAgreeOnRandomShortStrings() {
        Random rnd = new Random(12345);
        EditDistance rec = new RecursiveEditDistance();
        EditDistance tab = new TableEditDistance();
        EditDistance two = new TwoRowsEditDistance();
        for (int t = 0; t < 100; t++) {
            String a = Generator.random(rnd, rnd.nextInt(9), 3);      // length 0..8, letters a..c
            String b = Generator.random(rnd, rnd.nextInt(9), 3);
            int expected = tab.distance(a, b);
            assertEquals(expected, rec.distance(a, b), "recursive on '" + a + "' / '" + b + "'");
            assertEquals(expected, two.distance(a, b), "two rows on '" + a + "' / '" + b + "'");
        }
    }

    @Test
    void distanceIsSymmetric() {
        Random rnd = new Random(7);
        EditDistance two = new TwoRowsEditDistance();
        for (int t = 0; t < 50; t++) {
            String a = Generator.random(rnd, rnd.nextInt(30), 4);
            String b = Generator.random(rnd, rnd.nextInt(30), 4);
            assertEquals(two.distance(a, b), two.distance(b, a));
        }
    }

    @Test
    void tableAndTwoRowsAgreeOnLongerStrings() {
        Random rnd = new Random(99);
        EditDistance tab = new TableEditDistance();
        EditDistance two = new TwoRowsEditDistance();
        for (int t = 0; t < 20; t++) {
            String a = Generator.random(rnd, 200 + rnd.nextInt(100), 5);
            String b = Generator.random(rnd, 200 + rnd.nextInt(100), 5);
            assertEquals(tab.distance(a, b), two.distance(a, b));
        }
    }

    // ---- the counters themselves are measured inside the algorithms: check that they behave as expected ----

    @Test
    void counterTableComputesExactlyNTimesMCells() {
        TableEditDistance tab = new TableEditDistance();
        tab.distance("kitten", "sitting");                      // n = 6, m = 7
        assertEquals(6L * 7L, tab.steps());
        assertEquals(7L * 8L, tab.allocatedCells());            // (n+1)*(m+1)
    }

    @Test
    void counterTwoRowsComputesSameCellsButAllocatesOnlyTwoRows() {
        TwoRowsEditDistance two = new TwoRowsEditDistance();
        two.distance("kitten", "sitting");
        assertEquals(6L * 7L, two.steps());
        assertEquals(2L * 8L, two.allocatedCells());            // 2 * (m+1)
    }

    @Test
    void counterRecursiveBestCaseIsLinearWorstCaseIsExponential() {
        RecursiveEditDistance rec = new RecursiveEditDistance();
        String same = "abcdefgh";
        rec.distance(same, same);
        assertEquals(same.length() + 1L, rec.steps());          // one call per character + the base case

        assertEquals(8, rec.distance("aaaaaaaa", "bbbbbbbb"));  // nothing matches: 3 calls on every mismatch
        assertEquals(398_593L, rec.steps());                    // 398593 calls for n = 8 (counted by the algorithm)
    }
}
