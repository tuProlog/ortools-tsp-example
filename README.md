# Travelling Salesman Problem (TSP) in 2P-Kt

Brief example showing how [Google's OR-Tools](https://developers.google.com/optimization) can exploited in Prolog, to solve the TSP problem. Our solution leverages the notion of __primitives__ from the [2P-Kt](https://github.com/tuProlog/2p-kt) Prolog implementation.

A standard Prolog solver can be extended with a predicate of the form:
```prolog
tsp(?SetOfCities, ?Circuit, ?Cost)
```
which enumerates all minimally-`Cost`ly `Circuit`s for any possible `SetOfCities`, provided that the Prolog solver's KB contains a number of `path/3` facts describing the undirected edges of a city graph, like, e.g.:
```prolog
path(city1, city2, cost12).
path(city2, city3, cost23).
path(city3, city1, cost31).
% etc...
```

To solve the TSP, 2P-Kt solvers treat Google OR-Tools solvers as producers of __streams__ of solutions, to be _lazily_ enumerated as part of a standard Prolog resolution strategy.
Thus, users as well may lazily consume solutions to the TSP, via backtracking.

Furthermore, the `tsp/3` predicate is fully relational, thus users may perform a wide range of queries.

For example, users may be willing to solve a particular instance of the TSP, involving e.g. cities `city1`, `city2`, and `city3`:
```prolog
tsp({city1, city2, city3}, Circuit, Cost), Circuit = [city2 | OtherCities].
```
The query above enumerates all the minimally-`Cost`ly `Circuit`s starting from `city2` and involving exactly those 3 cities.

Conversely, the following query
```prolog
tsp({city1, city2, city3, MoreCities}, Circuit, Cost), Circuit = [city2 | OtherCities].
```
enumerates all the the minimally-`Cost`ly `Circuit`s starting from `city2` and stepping through _at least_ 3 cities, namely `city1`, `city2`, and `city3`.

Finally, the general query
```prolog
tsp(Cities, Circuit, Cost).
```
enumerates all the the minimally-`Cost`ly `Circuit`s involving all non-empty subsets of cities mentioned in the KB.

## How to run the example

### Prerequisites

- JDK 11+
- \[Optionally\] Gradle 6.7+
- \[Alternatively\] Docker

### Walkthrough

1. Start a new command line Prolog interpreter by running
```bash
gradlew -q run --args="-T /path/to/your/map-theory.pl"`
```
for instance, you may use `./src/test/resource/mini-map.pl`

0. Alternatively, you may start the interpreter by simply running
```bash
gradlew -q run
```
and later _consult_ a graph theory via the query:
```prolog
consult('file:/path/to/your/map-theory.pl').
```
for instance, you may write
```prolog
consult('file:./src/test/resource/mini-map.pl').
```
    
0. Write your `.`-terminated Prolog query of the form `tsp(Cities, Circuit, Cost).` and press <kbd>Enter</kbd>

0. The first solution will appear: press <kbd>;</kbd> and then <kbd>Enter</kbd> to show the next one
    + or just <kbd>Enter</kbd> to interrupt the solution stream and provide a new query

0. You may add new edges to the map graph by asserting new facts in the theory, e.g.:
```prolog
assert(path(city3, city4, cost34)).
```

0. Terminate the interpreter by querying `halt.`, or simply by pressing  <kbd>Ctrl</kbd>+ <kbd>D</kbd>

### In case of issues, try using Docker

1. You may dockerify this demo on your own machine by running:
```bash
docker build . -t pikalab/demos:2p-kt-tsp  
```

2. Then you may then start the dockerified interpreter by running:
```bash
docker run -it --rm pikalab/demos:2p-kt-tsp
```
(In presence of an Internet connection this step may also work without requiring the previous one, as the image `pikalab/demos:2p-kt-tsp` is publicly available on DockerHub)