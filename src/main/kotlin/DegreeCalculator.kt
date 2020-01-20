open class DegreeCalculator {
    val graph: BaseGraph
    private val degrees: List<Int>

    constructor(graph: BaseGraph) : this(
        graph,
        List(graph.size) { graph.edgesMap[it]?.size ?: 0 }
    )

    protected constructor(graph: BaseGraph, degrees: List<Int>) {
        this.graph = graph
        this.degrees = degrees
    }

    fun degreeOf(vertex: Vertex): Int = degrees[vertex.id]

    fun toMutable() = MutableDegreeCalculator(graph, degrees.toMutableList())

}

class MutableDegreeCalculator constructor(
    graph: BaseGraph,
    private val mutableDegrees: MutableList<Int>
) : DegreeCalculator(graph, mutableDegrees) {

    fun remove(vertex: Vertex) {
        graph.edgesMap[vertex.id]?.forEach { (a, b) ->
            mutableDegrees[a.id]--
            mutableDegrees[b.id]--
        }
    }
}