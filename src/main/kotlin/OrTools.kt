import it.unibo.tuprolog.solve.library.AliasedLibrary
import it.unibo.tuprolog.solve.library.Library

object OrTools : AliasedLibrary by Library.aliased(
    alias = "prolog.ortools",
    primitives = sequenceOf(
        Tsp
    ).map { it.descriptionPair }.toMap()
)