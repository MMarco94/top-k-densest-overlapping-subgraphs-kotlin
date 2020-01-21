class Peeler(
    val graph: BaseGraph,
    val subGraphs: Set<SubGraph>,
    val lambda: Double
) {
    val candidate = graph.toSubGraph()

    private var candidateEdges = graph.edges.size
    private val degrees = IntArray(graph.size) { graph.edgesMap[it].size }
    private val weights = DoubleArray(graph.size) { vertex ->
        degrees[vertex] - 4 * lambda * subGraphs.count { vertex in it }
    }
    private val intersectionCount = subGraphs.associateWithTo(HashMap()) { it.size }
    private val vertexPriorityQueue = VertexPriorityQueue(graph.size, weights, candidate.verticesMask)
    val candidateDensity get() = candidateEdges.toDouble() / candidate.size

    fun getIntersectionSize(another: SubGraph): Int {
        return intersectionCount.getValue(another)
    }

    fun removeWorstVertex() {
        remove(vertexPriorityQueue.head())
    }

    fun remove(vertex: Vertex) {
        candidateEdges -= degrees[vertex]
        candidate.remove(vertex)
        vertexPriorityQueue.notifyVertexWeightChanged(vertex)
        graph.edgesMap[vertex].forEach { e ->
            if (e.otherVertex(vertex) in candidate) {
                degrees[e.a]--
                degrees[e.b]--
                editWeight(e.a) {
                    weights[e.a]--
                }
                editWeight(e.b) {
                    weights[e.b]--
                }
            }
        }
        subGraphs.forIf({ vertex in it }) { g ->
            intersectionCount.computeIfPresent(g) { _, v -> v - 1 }
            g.forEachVertex { v ->
                editWeight(v) {
                    weights[v] += 4 * lambda / g.size
                }
            }
        }
    }

    fun add(vertex: Vertex) {
        candidate.add(vertex)
        vertexPriorityQueue.notifyVertexWeightChanged(vertex)
        graph.edgesMap[vertex].forEach { e ->
            if (e.otherVertex(vertex) in candidate) {
                degrees[e.a]++
                degrees[e.b]++
                editWeight(e.b) {
                    weights[e.b]++
                }
                editWeight(e.a) {
                    weights[e.a]++
                }
            }
        }
        candidateEdges += degrees[vertex]
        subGraphs.forIf({ vertex in it }) { g ->
            intersectionCount.computeIfPresent(g) { _, v -> v + 1 }
            g.forEachVertex { v ->
                editWeight(v) {
                    weights[v] -= 4 * lambda / g.size
                }
            }
        }
    }

    private inline fun editWeight(v: Vertex, f: () -> Unit) {
        f()
        vertexPriorityQueue.notifyVertexWeightChanged(v)
    }
}