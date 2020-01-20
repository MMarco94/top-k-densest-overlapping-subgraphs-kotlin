open class DegreeCalculator {
    val graph: BaseGraph
    val degrees: Map<Vertex, Int>

    constructor(graph: BaseGraph) : this(
        graph,
        graph.edgesMap.mapValues { it.value.size }
    )

    protected constructor(graph: BaseGraph, degrees: Map<Vertex, Int>) {
        this.graph = graph
        this.degrees = degrees
    }

    fun degreeOf(vertex: Vertex): Int = degrees[vertex] ?: 0

    fun toMutable() = MutableDegreeCalculator(graph, degrees.toMutableMap())

}

class MutableDegreeCalculator(
    graph: BaseGraph,
    private val mutableDegrees: MutableMap<Vertex, Int>
) : DegreeCalculator(graph, mutableDegrees) {

    fun remove(vertex: Vertex) {
        mutableDegrees.remove(vertex)
        graph.edgesMap[vertex]?.forEach { (a, b) ->
            mutableDegrees.computeIfPresent(a) { _, v -> v - 1 }
            mutableDegrees.computeIfPresent(b) { _, v -> v - 1 }
        }
    }
}