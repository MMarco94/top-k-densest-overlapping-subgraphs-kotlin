class DegreeCalculator {
    val graph: BaseGraph
    private val degrees: IntArray

    constructor(graph: BaseGraph) {
        this.graph = graph
        this.degrees = IntArray(graph.size) { graph.edgesMap[it].size }
    }

    constructor(graph: BaseGraph, degrees: IntArray) {
        this.graph = graph
        this.degrees = degrees
    }

    fun degreeOf(vertex: Vertex): Int = degrees[vertex]

    fun clone() = DegreeCalculator(graph, degrees.clone())

    fun remove(vertex: Vertex) {
        graph.edgesMap[vertex].forEach { (a, b) ->
            degrees[a]--
            degrees[b]--
        }
    }

    fun add(vertex: Vertex) {
        graph.edgesMap[vertex].forEach { (a, b) ->
            degrees[a]++
            degrees[b]++
        }
    }
}
