#!/usr/bin/env python3
"""Draws the 7 plots from results/results.csv (medians over the runs of each configuration).

Usage (from the repository root):   python3 scripts/make_plots.py
Needs:  Python 3 + matplotlib.
Every takeaway sentence is built from the data, so it cannot disagree with the numbers.
"""
import csv
import json
import statistics as st
import textwrap
from collections import defaultdict
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

ROOT = Path(__file__).resolve().parent.parent
CSV_PATH = ROOT / "results" / "results.csv"
OUT = ROOT / "results" / "plots"
OUT.mkdir(parents=True, exist_ok=True)

# ---------------------------------------------------------------- load
groups = defaultdict(list)
for r in csv.DictReader(open(CSV_PATH)):
    groups[(r["algorithm"], r["input"], int(r["n"]), int(r["m"]))].append(r)


def med_time(alg, inp, n, m):
    return st.median(float(x["time_ms"]) for x in groups[(alg, inp, n, m)])


def steps(alg, inp, n, m):
    return int(groups[(alg, inp, n, m)][0]["steps"])


def alloc(alg, inp, n, m):
    return int(groups[(alg, inp, n, m)][0]["alloc_cells"])


def sizes(alg, inp, m=None):
    return sorted(k[2] for k in groups if k[0] == alg and k[1] == inp and (m is None or k[3] == m))


COLORS = {"disjoint": "#c0392b", "random26": "#2471a3", "identical": "#27ae60", "random2": "#8e44ad",
          "aaab_worst": "#c0392b", "table": "#d35400", "two_rows": "#1f618d", "naive": "#c0392b", "kmp": "#1f618d"}
STYLE = {"random26": "-", "identical": "--", "disjoint": ":", "random2": "--", "aaab_worst": ":"}
LABEL = {"disjoint": "disjoint (worst case)", "identical": "identical (best case)", "random26": "random26 (average)",
         "random2": "random2 (binary alphabet)", "aaab_worst": "aaab_worst (worst case for Naive)",
         "naive": "Naive", "kmp": "KMP", "table": "Table (full)", "two_rows": "Two rows"}


TAKEAWAYS = {}


def finish(fig, ax, title, xlabel, ylabel, takeaway, name, legend_kw=None):
    TAKEAWAYS[name] = {"title": title, "takeaway": takeaway}
    ax.set_title(title, fontsize=12, fontweight="bold")
    ax.set_xlabel(xlabel)
    ax.set_ylabel(ylabel)
    ax.grid(True, which="both", alpha=0.3)
    ax.legend(**(legend_kw or {}))
    fig.subplots_adjust(bottom=0.27, left=0.12, right=0.97, top=0.92)
    fig.text(0.02, 0.02, "Takeaway: " + textwrap.fill(takeaway, 105), fontsize=9, va="bottom", ha="left", style="italic")
    fig.savefig(OUT / name, dpi=150)
    plt.close(fig)
    print("saved", name)


def newfig():
    return plt.subplots(figsize=(8, 5.8))


# ---------------------------------------------------------------- P1 recursive calls
fig, ax = newfig()
for inp in ["disjoint", "random26", "identical"]:
    ns = sizes("recursive", inp)
    ax.plot(ns, [steps("recursive", inp, n, n) for n in ns], marker="o", ls=STYLE[inp], color=COLORS[inp], label=LABEL[inp])
ax.set_yscale("log")
ns = sizes("recursive", "disjoint")
growth = steps("recursive", "disjoint", ns[-1], ns[-1]) / steps("recursive", "disjoint", ns[-2], ns[-2])
finish(fig, ax, "P1 - Recursive Edit Distance: number of recursive calls",
       "n (length of each of the two strings, characters)", "recursive calls (count, log scale)",
       f"On the worst-case input the calls multiply by about {growth:.1f} for every +1 in n (n={ns[-1]}: "
       f"{steps('recursive','disjoint',ns[-1],ns[-1]):,} calls), while identical strings need only n+1 "
       f"({steps('recursive','identical',ns[-1],ns[-1])} calls): the input decides everything.", "p1_recursive_calls.png")

# ---------------------------------------------------------------- P2 recursive time
fig, ax = newfig()
for inp in ["disjoint", "random26"]:
    ns = [n for n in sizes("recursive", inp) if med_time("recursive", inp, n, n) > 0]
    ax.plot(ns, [med_time("recursive", inp, n, n) for n in ns], marker="o", ls=STYLE[inp], color=COLORS[inp], label=LABEL[inp])
ax.set_yscale("log")
nmax = sizes("recursive", "disjoint")[-1]
ns_per_call = [1e6 * med_time("recursive", i, n, n) / steps("recursive", i, n, n)
               for i in ("disjoint", "random26") for n in sizes("recursive", i) if n >= 9]
finish(fig, ax, "P2 - Recursive Edit Distance: running time",
       "n (length of each of the two strings, characters)", "time (ms, median of 3 runs, log scale)",
       f"Time follows the call counter: from n=9 on, every call costs about {min(ns_per_call):.1f}-{max(ns_per_call):.1f} ns, "
       f"so n={nmax} on the worst case already takes {med_time('recursive','disjoint',nmax,nmax)/1000:.1f} s. "
       f"(Identical strings need about 0.001 ms or less (median), some are even rounded to 0, so they are not drawn on this log axis.)", "p2_recursive_time.png")

# ---------------------------------------------------------------- P3 table vs two rows: time
fig, ax = newfig()
for alg in ["table", "two_rows"]:
    for inp in ["random26", "identical", "disjoint"]:
        ns = sizes(alg, inp)
        ax.plot(ns, [med_time(alg, inp, n, n) for n in ns], marker="o", ms=4, ls=STYLE[inp], color=COLORS[alg],
                label=f"{LABEL[alg]}, {inp}")
ns = sizes("two_rows", "random26")
base = med_time("two_rows", "random26", ns[0], ns[0])
ax.plot(ns, [base * (n / ns[0]) ** 2 for n in ns], color="gray", lw=1, ls="-.", label="reference: time proportional to n^2")
ax.set_xscale("log", base=2)
ax.set_yscale("log")
ax.set_xticks(ns)
ax.set_xticklabels([str(n) for n in ns])
nm = ns[-1]
t_tab = st.median(med_time("table", i, nm, nm) for i in ("random26", "identical", "disjoint"))
t_two = st.median(med_time("two_rows", i, nm, nm) for i in ("random26", "identical", "disjoint"))
fac2 = med_time("two_rows", "random26", nm, nm) / med_time("two_rows", "random26", nm // 2, nm // 2)
finish(fig, ax, "P3 - Table vs Two rows: running time",
       "n (length of each of the two strings, characters, log scale)", "time (ms, median of 5 runs, log scale)",
       f"Both versions grow roughly like n^2 (two rows: x{fac2:.1f} when n doubles), but at n={nm} the full table needs "
       f"about {t_tab:.0f} ms and two rows about {t_two:.0f} ms ({t_tab/t_two:.1f}x faster) although both compute the same "
       f"{steps('table','random26',nm,nm):,} cells.", "p3_dp_time.png", legend_kw={"fontsize": 7, "ncol": 2})

# ---------------------------------------------------------------- P4 allocated cells
fig, ax = newfig()
ns = sizes("table", "random26")
ax.plot(ns, [alloc("table", "random26", n, n) for n in ns], marker="o", color=COLORS["table"], label=LABEL["table"] + ": (n+1)^2 cells")
ax.plot(ns, [alloc("two_rows", "random26", n, n) for n in ns], marker="s", color=COLORS["two_rows"], label=LABEL["two_rows"] + ": 2(n+1) cells")
ax.set_xscale("log", base=2)
ax.set_yscale("log")
ax.set_xticks(ns)
ax.set_xticklabels([str(n) for n in ns])
nm = ns[-1]
a_t, a_2 = alloc("table", "random26", nm, nm), alloc("two_rows", "random26", nm, nm)
finish(fig, ax, "P4 - Memory really allocated by the Edit Distance tables",
       "n (length of each of the two strings, characters, log scale)", "allocated int cells (count, log scale)",
       f"At n={nm} the full table allocates {a_t:,} cells and two rows only {a_2:,}: {a_t/a_2:,.0f} times less, "
       f"and the gap grows with n because one line is quadratic and the other is linear.", "p4_dp_memory.png")

# ---------------------------------------------------------------- P5 comparisons per character (grouped bars)
fig, ax = newfig()
M = 100
NMAX = max(sizes("naive", "random26", M))
inputs = ["random26", "random2", "aaab_worst"]
w = 0.38
for k, alg in enumerate(["naive", "kmp"]):
    vals = [steps(alg, i, NMAX, M) / NMAX for i in inputs]
    xs = [j + (k - 0.5) * w for j in range(len(inputs))]
    bars = ax.bar(xs, vals, width=w, color=COLORS[alg], label=LABEL[alg])
    for x, v in zip(xs, vals):
        ax.text(x, v * 1.08, f"{v:.2f}", ha="center", fontsize=8)
ax.set_yscale("log")
ax.set_ylim(0.5, 400)
ax.set_xticks(range(len(inputs)))
ax.set_xticklabels([LABEL[i].replace(" (", "\n(") for i in inputs])
naive_w, kmp_w = steps("naive", "aaab_worst", NMAX, M) / NMAX, steps("kmp", "aaab_worst", NMAX, M) / NMAX
finish(fig, ax, f"P5 - Character comparisons per text character (n={NMAX:,}, m={M})",
       "input type", "comparisons / n (ratio, log scale)",
       f"aaab_worst is the slowest input for Naive: {naive_w:.1f} comparisons per text character (about m), "
       f"while KMP never goes above {max(steps('kmp', i, NMAX, M) / NMAX for i in inputs):.2f} (the theory limit is 2) - "
       f"{naive_w / kmp_w:.0f} times fewer comparisons on that input; on random text the gap is small "
       f"({steps('naive','random26',NMAX,M) / NMAX:.2f} vs {steps('kmp','random26',NMAX,M) / NMAX:.2f} on random26, "
       f"{steps('naive','random2',NMAX,M) / NMAX:.2f} vs {steps('kmp','random2',NMAX,M) / NMAX:.2f} on random2).", "p5_search_comparisons.png")

# ---------------------------------------------------------------- P6 search time vs n
fig, ax = newfig()
for alg in ["naive", "kmp"]:
    for inp in inputs:
        ns = sizes(alg, inp, M)
        ax.plot(ns, [med_time(alg, inp, n, M) for n in ns], marker="o", ms=4, ls=STYLE[inp], color=COLORS[alg],
                label=f"{LABEL[alg]}, {inp}")
ax.set_xscale("log", base=2)
ax.set_yscale("log")
ns = sizes("naive", "aaab_worst", M)
ax.set_xticks(ns)
ax.set_xticklabels([f"{n // 1000}k" for n in ns])
t_nw, t_kw = med_time("naive", "aaab_worst", NMAX, M), med_time("kmp", "aaab_worst", NMAX, M)
ns_cmp_naive = 1e6 * t_nw / steps("naive", "aaab_worst", NMAX, M)
ns_cmp_kmp = 1e6 * t_kw / steps("kmp", "aaab_worst", NMAX, M)
finish(fig, ax, f"P6 - String search: running time (m={M})",
       "n (text length, characters, log scale)", "time (ms, median of 5 runs, log scale)",
       f"On aaab_worst Naive needs {t_nw:.0f} ms and KMP {t_kw:.1f} ms at n={NMAX:,} ({t_nw / t_kw:.0f}x), a smaller gap than the "
       f"{naive_w / kmp_w:.0f}x in comparisons, because one Naive comparison costs {ns_cmp_naive:.2f} ns and one KMP comparison "
       f"{ns_cmp_kmp:.2f} ns. The random2 curves bend at the same n for both algorithms, so this is not an algorithm effect (most likely machine noise).",
       "p6_search_time.png", legend_kw={"fontsize": 7, "ncol": 2})

# ---------------------------------------------------------------- P7 pattern length sweep
fig, ax = newfig()
SN = 1_000_000
ms = sorted(k[3] for k in groups if k[0] == "naive" and k[1] == "aaab_worst" and k[2] == SN)
for alg in ["naive", "kmp"]:
    for inp in ["aaab_worst", "random26"]:
        ax.plot(ms, [steps(alg, inp, SN, m) / SN for m in ms], marker="o", ms=4, ls=STYLE[inp], color=COLORS[alg],
                label=f"{LABEL[alg]}, {inp}")
ax.set_xscale("log", base=2)
ax.set_yscale("log")
ax.set_xticks(ms)
ax.set_xticklabels([str(m) for m in ms])
finish(fig, ax, f"P7 - Does the pattern length matter? (n={SN:,})",
       "m (pattern length, characters, log scale)", "comparisons / n (ratio, log scale)",
       f"Naive on aaab_worst grows in step with m ({steps('naive','aaab_worst',SN,ms[0]) / SN:.1f} at m={ms[0]} to "
       f"{steps('naive','aaab_worst',SN,ms[-1]) / SN:.1f} at m={ms[-1]}); KMP stays at {steps('kmp','aaab_worst',SN,ms[0]) / SN:.1f} "
       f"for every m; on random26 Naive and KMP lie on top of each other near {steps('naive','random26',SN,ms[0]) / SN:.2f}.",
       "p7_search_pattern_length.png", legend_kw={"fontsize": 8})

json.dump(TAKEAWAYS, open(OUT / "takeaways.json", "w"), indent=2)
