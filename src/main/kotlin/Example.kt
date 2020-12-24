import it.unibo.tuprolog.core.TermFormatter
import it.unibo.tuprolog.core.format
import it.unibo.tuprolog.dsl.solve.prolog

fun main(args: Array<String>) {
    prolog {
        loadLibrary(OrTools)

        staticKb(
            fact { "path"("roma", "napoli", 45) },
            fact { "path"("roma", "firenze", 60) },
            fact { "path"("firenze", "bologna", 60) },
            fact { "path"("bologna", "ancona", 180) },
            fact { "path"("ancona", "pescara", 60) },
            fact { "path"("pescara", "bari", 120) },
            fact { "path"("bari", "napoli", 150) }
        )

        val query = "tsp"(
            setOf("roma", "bologna", "napoli", "firenze", "ancona", "pescara", "bari"),
            consOf("roma", T),
            C
        )

        solve(query).forEach {
            println(it.solvedQuery?.format(TermFormatter.prettyVariables()))
        }
    }
}