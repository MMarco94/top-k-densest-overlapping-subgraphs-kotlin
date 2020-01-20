class DegreeCalculator {
    val graph: BaseGraph
    private val degrees: IntArray
    private val edgesMap: Array<MutableList<Edge>>

    constructor(graph: BaseGraph) {
        this.graph = graph
        this.edgesMap = Array(graph.size) { mutableListOf<Edge>() }.also { arr ->
            graph.edges.forEach { e ->
                arr[e.a].add(e)
                arr[e.b].add(e)
            }
        }
        this.degrees = IntArray(graph.size) { edgesMap[it].size }
    }

    private constructor(graph: BaseGraph, degrees: IntArray, edgesMap: Array<MutableList<Edge>>) {
        this.graph = graph
        this.degrees = degrees
        this.edgesMap = edgesMap
    }

    fun degreeOf(vertex: Vertex): Int = degrees[vertex]

    fun clone() = DegreeCalculator(graph, degrees.clone(), edgesMap)

    fun remove(vertex: Vertex) {
        edgesMap[vertex].forEach { (a, b) ->
            degrees[a]--
            degrees[b]--
        }
    }
    fun add(vertex: Vertex) {
        edgesMap[vertex].forEach { (a, b) ->
            degrees[a]++
            degrees[b]++
        }
    }
}
