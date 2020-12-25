import com.google.ortools.Loader
import com.google.ortools.constraintsolver.*
import com.google.ortools.constraintsolver.main.defaultRoutingSearchParameters
import it.unibo.tuprolog.core.*
import it.unibo.tuprolog.core.List as LogicList
import it.unibo.tuprolog.core.Set
import it.unibo.tuprolog.core.Substitution.Unifier
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.primitive.Solve.Request
import it.unibo.tuprolog.solve.primitive.Solve.Response
import it.unibo.tuprolog.solve.primitive.TernaryRelation
import it.unibo.tuprolog.unify.Unificator.Companion.mguWith
import it.unibo.tuprolog.utils.permutations
import org.gciatto.kt.math.BigInteger
import java.util.function.LongBinaryOperator

object Tsp : TernaryRelation<ExecutionContext>("tsp") {

    private val ROUTING_PARAMS: RoutingSearchParameters

    init {
        Loader.loadNativeLibraries()
        ROUTING_PARAMS = defaultRoutingSearchParameters().toBuilder()
            .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
            .build()
    }

    private fun <A, B, C> Pair<B, C>.addLeft(item: A): Triple<A, B, C> = Triple(item, first, second)

    private class CitiesIndexer(private val cities: List<Term>) : RoutingIndexManager(cities.size, 1, 0) {
        operator fun get(index: Long): Term = cities[indexToNode(index)]
    }

    private fun Request<ExecutionContext>.distances(indexing: CitiesIndexer): LongBinaryOperator =
        LongBinaryOperator { source, destination ->
            val sourceCity = indexing[source]
            val destinationCity = indexing[destination]
            listOf(sourceCity to destinationCity, destinationCity to sourceCity)
                .flatMap { (src, dst) -> context.staticKb[Struct.of("path", src, dst, Var.anonymous())] }
                .firstOrNull()
                ?.let { it.head[2].castTo<Integer>().intValue.toLong() }
                ?: Long.MAX_VALUE
        }

    private fun Assignment?.toCircuit(model: RoutingModel, indexer: CitiesIndexer): Pair<LogicList, Integer> {
        if (this == null) return (LogicList.empty() to Integer.MINUS_ONE)
        var cost: Long = 0
        val circuit = mutableListOf<Term>()
        var index = model.start(0)
        while (!model.isEnd(index)) {
            circuit += indexer[index]
            val previousIndex = index
            index = value(model.nextVar(index))
            cost += model.getArcCostForVehicle(previousIndex, index, 0)
        }
        circuit += indexer[model.end(0)]
        return LogicList.of(circuit) to Integer.of(cost)
    }

    private fun Request<ExecutionContext>.newRoutingModel(indexer: CitiesIndexer): RoutingModel =
        RoutingModel(indexer).also {
            it.setArcCostEvaluatorOfAllVehicles(it.registerTransitCallback(distances(indexer)))
        }

    private fun Request<ExecutionContext>.tsp(cities: List<Term>): Sequence<Pair<LogicList, Integer>> = sequence {
        for (citiesPermutations in cities.permutations()) {
            val indexer = CitiesIndexer(citiesPermutations)
            val model = newRoutingModel(indexer)
            val solution = model.solveWithParameters(ROUTING_PARAMS)
            if (model.status() == RoutingModel.ROUTING_SUCCESS) {
                val (circuit, cost) = solution.toCircuit(model, indexer)
                yield(circuit to cost)
            }
        }
    }.distinct()

    override fun Request<ExecutionContext>.computeAll(first: Term, second: Term, third: Term): Sequence<Response> {
        val allCities = solve(Struct.template("path", 3))
            .filterIsInstance<Solution.Yes>()
            .map { it.solvedQuery }
            .flatMap { sequenceOf(it[0], it[1]) }
            .toSet()

        return allCities.subsets()
            .flatMap { it.permutations() }
            .map { it to (Set.of(it) mguWith first) }
            .filter { (cities, substitution) -> cities.isNotEmpty() && substitution is Unifier }
            .flatMap { (cities, substitution) -> tsp(cities).map { it.addLeft(substitution) } }
            .map { (substitution, circuit, cost) -> substitution + (second mguWith circuit) + (third mguWith cost) }
            .filterIsInstance<Unifier>()
            .map { replySuccess(it) } + replyFail()
    }
}