### Table 1 - Kruskal vs Prim (undirected, connected, seed 42)

| Graph | V | E | Algorithm | Avg (median) time, us | Edge exams | Comparisons | DSU find | DSU union | PQ ops | MST weight |
|---|---|---|---|---|---|---|---|---|---|---|
| Small | 10 | 30 | Kruskal | 6.8 (6.9) | 15 | 109 | 30 | 9 | - | 134 |
| Small | 10 | 30 | Prim | 2.6 (2.3) | 60 | 204 | - | - | 60 | 134 |
| Medium | 100 | 500 | Kruskal | 74.1 (72.2) | 180 | 3,839 | 360 | 99 | - | 1153 |
| Medium | 100 | 500 | Prim | 109.0 (108.4) | 1000 | 7,770 | - | - | 1000 | 1153 |
| Large | 1000 | 5000 | Kruskal | 2,837.2 (1,470.1) | 3271 | 50,394 | 6542 | 999 | - | 12900 |
| Large | 1000 | 5000 | Prim | 2,340.0 (2,570.5) | 10000 | 111,028 | - | - | 10000 | 12900 |

### Table 2 - Effect of path compression and union by rank inside Kruskal

| Graph | DSU optimisation | Avg (median) time, us | find() calls | unions | Parent steps | Parent steps per find |
|---|---|---|---|---|---|---|
| Small | none | 8.2 (7.8) | 30 | 9 | 22 | 0.73 |
| Small | compression | 3.1 (3.1) | 30 | 9 | 19 | 0.63 |
| Small | rank | 3.1 (3.1) | 30 | 9 | 20 | 0.67 |
| Small | compression + rank | 6.8 (6.9) | 30 | 9 | 17 | 0.57 |
| Medium | none | 110.3 (110.1) | 360 | 99 | 2321 | 6.45 |
| Medium | compression | 102.4 (101.1) | 360 | 99 | 489 | 1.36 |
| Medium | rank | 66.6 (66.0) | 360 | 99 | 438 | 1.22 |
| Medium | compression + rank | 74.1 (72.2) | 360 | 99 | 290 | 0.81 |
| Large | none | 4,165.9 (3,955.6) | 6542 | 999 | 543294 | 83.05 |
| Large | compression | 2,317.9 (2,359.7) | 6542 | 999 | 12192 | 1.86 |
| Large | rank | 1,550.1 (1,579.7) | 6542 | 999 | 10625 | 1.62 |
| Large | compression + rank | 2,837.2 (1,470.1) | 6542 | 999 | 6045 | 0.92 |

### Table 3 - Topological ordering (Kahn): DAG vs graph with cycles

| Graph | V | E | Input | Avg (median) time, us | Edge exams | In-degree tests | Reported acyclic | Vertices ordered |
|---|---|---|---|---|---|---|---|---|
| Small | 10 | 30 | DAG | 0.6 (0.6) | 60 | 30 | true | 10 / 10 |
| Small | 10 | 30 | Cyclic | 0.6 (0.6) | 30 | 0 | false | 0 / 10 |
| Medium | 100 | 500 | DAG | 14.1 (7.3) | 1000 | 500 | true | 100 / 100 |
| Medium | 100 | 500 | Cyclic | 2.7 (2.6) | 500 | 0 | false | 0 / 100 |
| Large | 1000 | 5000 | DAG | 197.7 (113.9) | 10000 | 5000 | true | 1000 / 1000 |
| Large | 1000 | 5000 | Cyclic | 63.9 (30.8) | 5054 | 54 | false | 10 / 1000 |

### Table 4 - Strongly connected components (Kosaraju)

| Graph | V | E | Input | Avg (median) time, us | Edge exams | # SCCs | Largest SCC |
|---|---|---|---|---|---|---|---|
| Small | 10 | 30 | Cyclic | 1.8 (1.8) | 90 | 1 | 10 |
| Small | 10 | 30 | DAG | 3.3 (2.7) | 90 | 10 | 1 |
| Medium | 100 | 500 | Cyclic | 27.9 (28.0) | 1500 | 1 | 100 |
| Medium | 100 | 500 | DAG | 25.7 (29.2) | 1500 | 100 | 1 |
| Large | 1000 | 5000 | Cyclic | 949.0 (476.2) | 15000 | 18 | 983 |
| Large | 1000 | 5000 | DAG | 799.7 (619.7) | 15000 | 1000 | 1 |

### Table 5 - Dijkstra from source district 0 (directed, weights 1..100)

| Graph | V | E | Avg (median) time, us | Edge exams | Relaxations | PQ ops | Comparisons | Reachable | Unreachable |
|---|---|---|---|---|---|---|---|---|---|
| Small | 10 | 30 | 3.0 (2.9) | 30 | 11 | 24 | 63 | 10 | 0 |
| Medium | 100 | 500 | 22.6 (22.3) | 500 | 149 | 300 | 2,050 | 100 | 0 |
| Large | 1000 | 5000 | 744.1 (701.7) | 4946 | 1419 | 2840 | 28,520 | 990 | 10 |

### Table 6 - Average time (us) as V grows, E = 5V

| V | Kruskal | Prim | Topo-DAG | Topo-Cyclic | SCC-Cyclic | SCC-DAG | Dijkstra |
|---|---|---|---|---|---|---|---|
| 20 | 10.7 | 8.3 | 2.2 | 1.1 | 4.5 | 4.7 | 4.8 |
| 50 | 29.6 | 45.3 | 3.1 | 1.3 | 12.2 | 8.6 | 14.5 |
| 100 | 117.6 | 122.1 | 6.9 | 3.6 | 24.3 | 17.0 | 33.1 |
| 250 | 219.9 | 404.8 | 26.2 | 9.6 | 73.5 | 73.6 | 95.2 |
| 500 | 622.2 | 1,199.6 | 115.0 | 18.6 | 646.3 | 320.8 | 474.0 |
| 1000 | 1,807.1 | 3,059.9 | 91.7 | 14.3 | 199.8 | 424.3 | 368.0 |
| 2000 | 2,214.6 | 2,699.4 | 106.7 | 25.1 | 359.4 | 1,087.2 | 823.8 |
| 5000 | 6,189.4 | 6,827.9 | 462.0 | 77.1 | 1,124.2 | 2,527.5 | 3,891.8 |

### Table 7 - Average time (us) as E grows, V = 1000

| E | Kruskal | Prim | Topo-DAG | Topo-Cyclic | SCC-Cyclic | SCC-DAG | Dijkstra |
|---|---|---|---|---|---|---|---|
| 1000 | 172.5 | 67.7 | 31.6 | 16.6 | 76.7 | 59.2 | 3.5 |
| 2000 | 401.3 | 417.8 | 24.8 | 24.9 | 139.2 | 96.4 | 122.1 |
| 5000 | 1,105.1 | 1,722.0 | 43.3 | 13.1 | 156.2 | 272.0 | 286.2 |
| 10000 | 2,220.6 | 2,432.0 | 80.7 | 37.8 | 348.5 | 609.9 | 468.7 |
| 20000 | 4,360.7 | 5,388.3 | 315.6 | 58.2 | 1,880.8 | 1,683.5 | 2,019.2 |
| 50000 | 18,472.0 | 41,935.3 | 1,155.4 | 346.3 | 4,165.4 | 4,468.2 | 2,931.3 |

### Table 8 - Operation counts as V grows (E = 5V)

| V | E | Kruskal edge exams | Kruskal comparisons | Prim edge exams | Prim comparisons | Prim PQ ops |
|---|---|---|---|---|---|---|
| 20 | 100 | 28 | 546 | 200 | 1,034 | 200 |
| 50 | 250 | 91 | 1,668 | 500 | 3,306 | 500 |
| 100 | 500 | 180 | 3,839 | 1000 | 7,770 | 1000 |
| 250 | 1250 | 635 | 11,258 | 2500 | 22,459 | 2500 |
| 500 | 2500 | 1419 | 24,211 | 5000 | 50,256 | 5000 |
| 1000 | 5000 | 3271 | 50,394 | 10000 | 111,028 | 10000 |
| 2000 | 10000 | 7765 | 103,097 | 20000 | 242,900 | 20000 |
| 5000 | 25000 | 23431 | 262,838 | 50000 | 677,785 | 50000 |
