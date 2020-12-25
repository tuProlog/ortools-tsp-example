import com.github.ajalt.clikt.core.subcommands
import it.unibo.tuprolog.ui.repl.*

object Repl {
    @JvmStatic
    fun main(args: Array<String>) {
        TuPrologCmd(OrTools).subcommands(TuPrologSolveQuery()).main(args)
    }
}