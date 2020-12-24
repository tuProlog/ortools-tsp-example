import com.google.ortools.Loader
import com.google.ortools.constraintsolver.Assignment
import com.google.ortools.constraintsolver.FirstSolutionStrategy
import com.google.ortools.constraintsolver.RoutingIndexManager
import com.google.ortools.constraintsolver.RoutingModel
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

    init {
        Loader.loadNativeLibraries()
    }

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

    private fun Assignment.toCircuit(model: RoutingModel, indexer: CitiesIndexer): Pair<LogicList, Integer> {
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

    override fun Request<ExecutionContext>.computeAll(first: Term, second: Term, third: Term): Sequence<Response> {
        val allCities = solve(Struct.template("path", 3))
            .filterIsInstance<Solution.Yes>()
            .map { it.solvedQuery }
            .flatMap { sequenceOf(it[0], it[1]) }
            .toSet()

        return allCities.subsets().flatMap { it.permutations() }
            .map { it to (Set.of(it) mguWith first) }
            .filter { (cities, substitution) -> cities.isNotEmpty() && substitution is Unifier }
            .map { (cities, substitution) ->
                val indexer = CitiesIndexer(cities)
                val model = RoutingModel(indexer)
                model.setArcCostEvaluatorOfAllVehicles(model.registerTransitCallback(distances(indexer)))
                val searchParameters = defaultRoutingSearchParameters().toBuilder()
                    .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
                    .build()
                val solution = model.solveWithParameters(searchParameters)
                val (circuit, cost) = solution?.toCircuit(model, indexer) ?: (LogicList.empty() to Integer.MINUS_ONE)
                Triple(substitution, circuit, cost)
            }.filter { (_, _, cost) -> cost.value >= BigInteger.ZERO }
            .map { (substitution, circuit, cost) ->
                replyWith(substitution + (second mguWith circuit) + (third mguWith cost))
            }
    }
}