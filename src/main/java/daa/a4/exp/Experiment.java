package daa.a4.exp;

import daa.a4.edit.EditDistance;
import daa.a4.edit.RecursiveEditDistance;
import daa.a4.edit.TableEditDistance;
import daa.a4.edit.TwoRowsEditDistance;
import daa.a4.search.KmpSearch;
import daa.a4.search.NaiveSearch;
import daa.a4.search.StringSearch;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Runs the whole experiment and writes results.csv (one row per run).
 *
 * Usage:  java -cp target/classes daa.a4.exp.Experiment results/results.csv
 *
 * The experiment follows the Experiment Design Card in REPORT.md:
 *   - the generator uses the fixed SEED, so the strings (and all counters) are the same on every run;
 *   - the times change a little from run to run (that is why we take the median of several runs).
 */
public final class Experiment {

    static final long SEED = 42L;

    // ---------- sizes (see the Design Card) ----------
    static final int[] REC_SIZES = {3, 6, 9, 11, 12, 13, 14};   // 14 added after the first run (see REPORT.md)
    static final int[] DP_SIZES = {250, 500, 1000, 2000, 4000};
    static final int[] SEARCH_SIZES = {100_000, 200_000, 400_000, 800_000, 1_600_000};
    static final int SEARCH_M = 100;
    static final int SWEEP_N = 1_000_000;
    static final int[] SWEEP_M = {10, 20, 40, 80, 160};

    static final List<String> ED_INPUTS = List.of("random26", "identical", "disjoint");
    static final List<String> SEARCH_INPUTS = List.of("random26", "random2", "aaab_worst");

    static final int RUNS_RECURSIVE = 3;
    static final int RUNS_OTHER = 5;

    private static final String HEADER =
            "family,algorithm,input,n,m,run,time_ms,steps,lps_steps,alloc_cells,result,seed";

    public static void main(String[] args) throws IOException {
        Path out = Path.of(args.length > 0 ? args[0] : "results/results.csv");
        if (out.getParent() != null) Files.createDirectories(out.getParent());

        warmUp();

        try (BufferedWriter w = Files.newBufferedWriter(out)) {
            w.write(HEADER);
            w.newLine();
            runEditDistanceRecursive(w);
            runEditDistanceDp(w);
            runSearchSizes(w);
            runSearchPatternSweep(w);
        }
        System.out.println("Done. Wrote " + out.toAbsolutePath());
    }

    // ---------------------------------------------------------------- Edit Distance

    private static void runEditDistanceRecursive(BufferedWriter w) throws IOException {
        EditDistance rec = new RecursiveEditDistance();
        EditDistance reference = new TableEditDistance();      // used only to check the answer
        for (String input : ED_INPUTS) {
            for (int n : REC_SIZES) {
                String[] p = Generator.editPair(input, n, SEED);
                int expected = reference.distance(p[0], p[1]);
                if (n <= 11) rec.distance(p[0], p[1]);           // one untimed run (too slow to repeat for big n)
                for (int run = 1; run <= RUNS_RECURSIVE; run++) {
                    row(w, "edit", rec, input, n, run, p, expected);
                }
            }
        }
    }

    private static void runEditDistanceDp(BufferedWriter w) throws IOException {
        EditDistance table = new TableEditDistance();
        EditDistance two = new TwoRowsEditDistance();
        for (String input : ED_INPUTS) {
            for (int n : DP_SIZES) {
                String[] p = Generator.editPair(input, n, SEED);
                int expected = table.distance(p[0], p[1]);
                if (two.distance(p[0], p[1]) != expected) {
                    throw new IllegalStateException("table and two rows disagree for " + input + " n=" + n);
                }
                for (EditDistance ed : List.of(table, two)) {
                    ed.distance(p[0], p[1]);                     // one untimed run before measuring
                    for (int run = 1; run <= RUNS_OTHER; run++) {
                        row(w, "edit", ed, input, n, run, p, expected);
                    }
                }
            }
        }
    }

    private static void row(BufferedWriter w, String family, EditDistance ed, String input, int n, int run,
                            String[] p, int expected) throws IOException {
        long t0 = System.nanoTime();
        int d = ed.distance(p[0], p[1]);
        long t1 = System.nanoTime();
        if (d != expected) throw new IllegalStateException(ed.name() + " gave " + d + ", expected " + expected);
        write(w, family, ed.name(), input, n, n, run, (t1 - t0) / 1e6,
                ed.steps(), 0, ed.allocatedCells(), d);
        System.out.printf(Locale.ROOT, "%s %-9s %-9s n=%-5d run=%d %.3f ms%n",
                family, ed.name(), input, n, run, (t1 - t0) / 1e6);
    }

    // ---------------------------------------------------------------- String search

    private static void runSearchSizes(BufferedWriter w) throws IOException {
        StringSearch naive = new NaiveSearch();
        StringSearch kmp = new KmpSearch();
        for (String input : SEARCH_INPUTS) {
            for (int n : SEARCH_SIZES) {
                searchConfig(w, naive, kmp, input, n, SEARCH_M);
            }
        }
    }

    private static void runSearchPatternSweep(BufferedWriter w) throws IOException {
        StringSearch naive = new NaiveSearch();
        StringSearch kmp = new KmpSearch();
        for (String input : List.of("random26", "aaab_worst")) {
            for (int m : SWEEP_M) {
                searchConfig(w, naive, kmp, input, SWEEP_N, m);
            }
        }
    }

    private static void searchConfig(BufferedWriter w, StringSearch naive, StringSearch kmp,
                                     String input, int n, int m) throws IOException {
        String[] in = Generator.searchInput(input, n, m, SEED);
        List<Integer> expected = naive.search(in[0], in[1]);
        if (!expected.equals(kmp.search(in[0], in[1]))) {
            throw new IllegalStateException("naive and kmp disagree for " + input + " n=" + n + " m=" + m);
        }
        for (StringSearch s : List.of(naive, kmp)) {
            s.search(in[0], in[1]);                              // one untimed run before measuring
            for (int run = 1; run <= RUNS_OTHER; run++) {
                long t0 = System.nanoTime();
                List<Integer> r = s.search(in[0], in[1]);
                long t1 = System.nanoTime();
                write(w, "search", s.name(), input, n, m, run, (t1 - t0) / 1e6,
                        s.comparisons(), s.preprocessingComparisons(), 0, r.size());
                System.out.printf(Locale.ROOT, "search %-6s %-10s n=%-8d m=%-4d run=%d %.3f ms%n",
                        s.name(), input, n, m, run, (t1 - t0) / 1e6);
            }
        }
    }

    // ---------------------------------------------------------------- helpers

    private static void write(BufferedWriter w, String family, String algorithm, String input, int n, int m, int run,
                              double timeMs, long steps, long lpsSteps, long allocCells, long result) throws IOException {
        w.write(String.format(Locale.ROOT, "%s,%s,%s,%d,%d,%d,%.4f,%d,%d,%d,%d,%d",
                family, algorithm, input, n, m, run, timeMs, steps, lpsSteps, allocCells, result, SEED));
        w.newLine();
        w.flush();
    }

    /** Runs every algorithm a few times so the JIT compiler has finished its work before we measure. */
    private static void warmUp() {
        System.out.println("warm-up...");
        EditDistance rec = new RecursiveEditDistance();
        EditDistance table = new TableEditDistance();
        EditDistance two = new TwoRowsEditDistance();
        StringSearch naive = new NaiveSearch();
        StringSearch kmp = new KmpSearch();
        long sink = 0;
        for (int i = 0; i < 5; i++) {
            String[] a = Generator.editPair("random26", 800, SEED + i);
            sink += table.distance(a[0], a[1]) + two.distance(a[0], a[1]);
            String[] d = Generator.editPair("disjoint", 9, SEED);
            sink += rec.distance(d[0], d[1]);
            for (String type : SEARCH_INPUTS) {
                String[] s = Generator.searchInput(type, 200_000, 50, SEED + i);
                sink += naive.search(s[0], s[1]).size() + kmp.search(s[0], s[1]).size();
            }
        }
        if (sink == 42) System.out.println();                // keeps the JIT from removing the work
    }
}
