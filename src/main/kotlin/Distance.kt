import kotlin.math.pow

/**
 * A distance function between two graphs. See pag. 7, definition 2.
 *
 * This distance should be c-metric (see pag. 8, definition 3)
 */
interface Distance {
    operator fun invoke(g1: SubGraph, g2: SubGraph, intersectionSize: Int): Double
}

/**
 * See page 11, definition 4
 */
object MetricDistance : Distance {
    override operator fun invoke(g1: SubGraph, g2: SubGraph, intersectionSize: Int): Double {
        val areEqual = g1.size == g2.size && g1.size == intersectionSize
        return if (!areEqual) {
            2 - intersectionSize.toDouble().pow(2) / (g1.size * g2.size)
        } else 0.0
    }
}