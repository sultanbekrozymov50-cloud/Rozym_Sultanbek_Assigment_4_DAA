# DAA Assignment 4 - Dynamic Programming and String Algorithms

**City Search & Spell-Check**: Edit Distance (three versions) and Naive / KMP string search, with counters,
a seeded generator, JUnit 5 tests and an experiment.

Course: Design and Analysis of Algorithms. Author: *name surname, group* (fill in before submitting).

## Layout

```
src/main/java/daa/a4/edit/     EditDistance (interface), Recursive / Table / TwoRows versions
src/main/java/daa/a4/search/   StringSearch (interface), NaiveSearch, KmpSearch (+ public static lps)
src/main/java/daa/a4/exp/      Generator (seeded) and Experiment (writes results.csv)
src/test/java/daa/a4/          JUnit 5 tests
scripts/make_plots.py          draws the plots from results.csv
results/results.csv            one row per run
results/plots/                 7 PNG plots (+ takeaways.json with the sentence under each plot)
docs/experiment_card.md        the Experiment Design Card as written BEFORE the first run
REPORT.md                      the report
```

## Build and test (needs JDK 17+ and Maven)

```bash
mvn package          # compiles and runs all JUnit 5 tests (one command)
mvn test             # only the tests
```

## Run the experiment and draw the plots

```bash
mvn -q compile
java -cp target/classes daa.a4.exp.Experiment results/results.csv   # about 2-3 minutes
python3 scripts/make_plots.py                                       # needs python3 + matplotlib
```

(`mvn compile exec:java` does the same as the `java -cp ...` line.)

The generator uses a fixed seed (`SEED = 42`), so the strings and **all counters are identical on every run**.
The measured times change a little from run to run - that is why every configuration is run 3-5 times and the plots
use the median.

## results.csv columns

| column | meaning |
|---|---|
| `family` | `edit` or `search` |
| `algorithm` | `recursive`, `table`, `two_rows`, `naive`, `kmp` |
| `input` | input type, see `Generator` and the Design Card |
| `n`, `m` | Edit Distance: both strings have length n (m = n). Search: text length n, pattern length m |
| `run` | run number (1..3 for the recursion, 1..5 for the rest); one row per run |
| `time_ms` | wall-clock time of one call, ms (KMP includes building the LPS array) |
| `steps` | counter inside the algorithm: recursive calls (`recursive`), computed cells (`table`, `two_rows`), character comparisons (`naive`, `kmp`) |
| `lps_steps` | comparisons spent building the LPS array (only `kmp`) |
| `alloc_cells` | int cells really allocated (only the DP versions) |
| `result` | edit distance, or number of matches (used to check that the algorithms agree) |
| `seed` | generator seed |

## Machine used for the numbers in REPORT.md

Linux container, 1 vCPU (Intel Xeon @ 2.30 GHz), OpenJDK 21. On another computer the counters will be the same,
the times will not.

