fun <T> Set<T>.subsets(): Sequence<Set<T>> = sequence {
    when (size) {
        0 -> yield(emptySet<T>())
        else -> {
            val head = first()
            val tail = this@subsets - head
            yieldAll(tail.subsets())
            for (subset in tail.subsets()) {
                yield(setOf(head) + subset)
            }
        }
    }
}

fun main(args: Array<String>) {
    setOf(emptySet<Any>()).subsets().forEach(::println)
}