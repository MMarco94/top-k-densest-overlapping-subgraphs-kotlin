class DegreeCalculator {
    val graph: BaseGraph
    private val degrees: IntArray

    constructor(graph: BaseGraph) : this(
        graph,
        IntArray(graph.size) { graph.edgesMap[it].size }
    )

    private constructor(graph: BaseGraph, degrees: IntArray) {
        this.graph = graph
        this.degrees = degrees
    }

    fun degreeOf(vertex: Vertex): Int = degrees[vertex.id]

    fun clone() = DegreeCalculator(graph, degrees.clone())

    fun remove(vertex: Vertex) {
        graph.edgesMap[vertex.id].forEach { (a, b) ->
            degrees[a.id]--
            degrees[b.id]--
        }
    }
}
