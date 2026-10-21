#!/usr/bin/env python3
"""Reads results/tables/results.csv, writes results/tables/tables.md and PNG plots to results/plots."""
import csv
import sys
from collections import defaultdict
from pathlib import Path

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt  # noqa: E402

out = Path(sys.argv[1] if len(sys.argv) > 1 else "results/tables")
plots = Path(sys.argv[2] if len(sys.argv) > 2 else "results/plots")
plots.mkdir(parents=True, exist_ok=True)
rows = list(csv.DictReader(open(out / "results.csv")))
for r in rows:
    for k in ("V", "E", "edge_exams", "comparisons", "dsu_find", "dsu_union",
              "dsu_parent_steps", "pq_ops", "relaxations"):
        r[k] = int(r[k])
    r["avg_time_us"] = float(r["avg_time_us"])
    r["median_time_us"] = float(r["median_time_us"])
    r["info"] = dict(kv.split("=", 1) for kv in r["info"].split(";") if "=" in kv)

idx = {(r["family"], r["graph"], r["algorithm"]): r for r in rows}


def fam(name):
    seen, res = set(), []
    for r in rows:
        if r["family"] == name and r["graph"] not in seen:
            seen.add(r["graph"])
            res.append(r)
    return res


def md(header, body):
    lines = ["| " + " | ".join(header) + " |", "|" + "|".join("---" for _ in header) + "|"]
    for b in body:
        lines.append("| " + " | ".join(str(x) for x in b) + " |")
    return "\n".join(lines)


def fmt_t(x):
    return f"{x:,.1f}"


def tm(r):
    """avg (median) in microseconds"""
    return f"{r['avg_time_us']:,.1f} ({r['median_time_us']:,.1f})"


req = fam("required")
T = []

# Table 1: Kruskal vs Prim
T.append("### Table 1 - Kruskal vs Prim (undirected, connected, seed 42)\n")
body = []
for g in req:
    for a in ("Kruskal", "Prim"):
        r = idx[("required", g["graph"], a)]
        body.append([g["graph"], g["V"], g["E"], a, tm(r), r["edge_exams"],
                     f"{r['comparisons']:,}", r["dsu_find"] or "-", r["dsu_union"] or "-",
                     r["pq_ops"] or "-", r["info"]["mst_weight"]])
T.append(md(["Graph", "V", "E", "Algorithm", "Avg (median) time, us", "Edge exams", "Comparisons",
             "DSU find", "DSU union", "PQ ops", "MST weight"], body))

# Table 2: DSU ablation
T.append("\n### Table 2 - Effect of path compression and union by rank inside Kruskal\n")
body = []
variants = [("Kruskal-naiveDSU", "none"), ("Kruskal-compressOnly", "compression"),
            ("Kruskal-rankOnly", "rank"), ("Kruskal", "compression + rank")]
for g in req:
    for a, label in variants:
        r = idx[("required", g["graph"], a)]
        body.append([g["graph"], label, tm(r), r["dsu_find"], r["dsu_union"],
                     r["dsu_parent_steps"], f"{r['dsu_parent_steps'] / max(r['dsu_find'], 1):.2f}"])
T.append(md(["Graph", "DSU optimisation", "Avg (median) time, us", "find() calls", "unions",
             "Parent steps", "Parent steps per find"], body))

# Table 3: topological ordering
T.append("\n### Table 3 - Topological ordering (Kahn): DAG vs graph with cycles\n")
body = []
for g in req:
    for a in ("Topo-DAG", "Topo-Cyclic"):
        r = idx[("required", g["graph"], a)]
        body.append([g["graph"], g["V"], g["E"], a.split("-")[1], tm(r), r["edge_exams"],
                     r["comparisons"], r["info"]["acyclic"], f"{r['info']['ordered']} / {g['V']}"])
T.append(md(["Graph", "V", "E", "Input", "Avg (median) time, us", "Edge exams", "In-degree tests",
             "Reported acyclic", "Vertices ordered"], body))

# Table 4: SCC
T.append("\n### Table 4 - Strongly connected components (Kosaraju)\n")
body = []
for g in req:
    for a in ("SCC-Cyclic", "SCC-DAG"):
        r = idx[("required", g["graph"], a)]
        body.append([g["graph"], g["V"], g["E"], a.split("-")[1], tm(r),
                     r["edge_exams"], r["info"]["components"], r["info"]["largest"]])
T.append(md(["Graph", "V", "E", "Input", "Avg (median) time, us", "Edge exams", "# SCCs", "Largest SCC"], body))

# Table 5: Dijkstra
T.append("\n### Table 5 - Dijkstra from source district 0 (directed, weights 1..100)\n")
body = []
for g in req:
    r = idx[("required", g["graph"], "Dijkstra")]
    body.append([g["graph"], g["V"], g["E"], tm(r), r["edge_exams"], r["relaxations"],
                 r["pq_ops"], f"{r['comparisons']:,}", r["info"]["reachable"], r["info"]["unreachable"]])
T.append(md(["Graph", "V", "E", "Avg (median) time, us", "Edge exams", "Relaxations", "PQ ops", "Comparisons",
             "Reachable", "Unreachable"], body))

# Supplementary tables (scale families)
main_algos = ["Kruskal", "Prim", "Topo-DAG", "Topo-Cyclic", "SCC-Cyclic", "SCC-DAG", "Dijkstra"]
for fname, title, key in (("scaleV", "Table 6 - Average time (us) as V grows, E = 5V", "V"),
                          ("scaleE", "Table 7 - Average time (us) as E grows, V = 1000", "E")):
    T.append(f"\n### {title}\n")
    body = []
    for g in fam(fname):
        body.append([g["V"] if key == "V" else g["E"]]
                    + [fmt_t(idx[(fname, g["graph"], a)]["avg_time_us"]) for a in main_algos])
    T.append(md([key] + main_algos, body))

T.append("\n### Table 8 - Operation counts as V grows (E = 5V)\n")
body = []
for g in fam("scaleV"):
    k, p = idx[("scaleV", g["graph"], "Kruskal")], idx[("scaleV", g["graph"], "Prim")]
    body.append([g["V"], g["E"], k["edge_exams"], f"{k['comparisons']:,}", p["edge_exams"],
                 f"{p['comparisons']:,}", p["pq_ops"]])
T.append(md(["V", "E", "Kruskal edge exams", "Kruskal comparisons", "Prim edge exams",
             "Prim comparisons", "Prim PQ ops"], body))

(out / "tables.md").write_text("\n".join(T) + "\n")

# ------------------------------------------------------------------ plots
plt.rcParams.update({"figure.dpi": 140, "axes.grid": True, "grid.alpha": 0.3})
style = {
    "Kruskal": ("tab:blue", "o"), "Prim": ("tab:orange", "s"), "Topo-DAG": ("tab:green", "^"),
    "Topo-Cyclic": ("tab:olive", "v"), "SCC-Cyclic": ("tab:red", "D"), "SCC-DAG": ("tab:pink", "x"),
    "Dijkstra": ("tab:purple", "P"),
}


def line_plot(fname, xkey, xlabel, title, filename):
    gs = fam(fname)
    xs = [g[xkey] for g in gs]
    plt.figure(figsize=(7.5, 4.8))
    for a in main_algos:
        ys = [idx[(fname, g["graph"], a)]["avg_time_us"] for g in gs]
        c, m = style[a]
        plt.plot(xs, ys, marker=m, color=c, label=a)
    plt.xscale("log")
    plt.yscale("log")
    plt.xlabel(xlabel)
    plt.ylabel("average execution time (microseconds, log scale)")
    plt.title(title)
    plt.legend(fontsize=8)
    plt.tight_layout()
    plt.savefig(plots / filename)
    plt.close()


line_plot("scaleV", "V", "number of vertices V (log scale), E = 5V",
          "Execution time vs number of vertices", "time_vs_vertices.png")
line_plot("scaleE", "E", "number of edges E (log scale), V = 1000",
          "Execution time vs number of edges", "time_vs_edges.png")

# Kruskal vs Prim operation counts
gs = fam("scaleV")
plt.figure(figsize=(7.5, 4.8))
for a, c in (("Kruskal", "tab:blue"), ("Prim", "tab:orange")):
    plt.plot([g["V"] for g in gs], [idx[("scaleV", g["graph"], a)]["comparisons"] for g in gs],
             marker="o", color=c, label=f"{a} comparisons")
plt.xscale("log")
plt.yscale("log")
plt.xlabel("number of vertices V (log scale), E = 5V")
plt.ylabel("comparisons (log scale)")
plt.title("Kruskal vs Prim: weight comparisons")
plt.legend()
plt.tight_layout()
plt.savefig(plots / "kruskal_vs_prim_ops.png")
plt.close()

# DSU ablation
plt.figure(figsize=(7.5, 4.8))
for a, label, c in (("Kruskal-naiveDSU", "no optimisation", "tab:gray"),
                    ("Kruskal-rankOnly", "union by rank only", "tab:green"),
                    ("Kruskal-compressOnly", "path compression only", "tab:orange"),
                    ("Kruskal", "compression + rank", "tab:blue")):
    plt.plot([g["V"] for g in gs],
             [idx[("scaleV", g["graph"], a)]["dsu_parent_steps"] / idx[("scaleV", g["graph"], a)]["dsu_find"]
              for g in gs], marker="o", color=c, label=label)
plt.xscale("log")
plt.xlabel("number of vertices V (log scale), E = 5V")
plt.yscale("log")
plt.ylabel("parent pointers followed per find() (log scale)")
plt.title("DSU: effect of path compression and union by rank")
plt.legend()
plt.tight_layout()
plt.savefig(plots / "dsu_variants.png")
plt.close()


def print(param, param1, param2, plots):
    pass


print("wrote", out / "tables.md", "and 4 plots in", plots)