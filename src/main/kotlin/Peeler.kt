class Peeler(
    val graph: BaseGraph,
    val subGraphs: List<SubGraph>,
    val lambda: Double
) {
    val candidate = graph.toSubGraph()

    private var candidateEdges = graph.edges.size
    val candidateDensity get() = candidateEdges.toDouble() / candidate.size
    private val degrees = IntArray(graph.size) { graph.edgesMap[it].size }
    private val weights = DoubleArray(graph.size) { vertex ->
        degrees[vertex] - 4 * lambda * subGraphs.count { vertex in it }
    }
    private val intersectionCount = subGraphs.mapTo(ArrayList(subGraphs.size)) { it.size }

    //TODO: improve heusristic
    /**
     * Using the min heap is not worth it if n of the other subgraph has "too many" vertices.
     * It that case, updating the heap is far worst than just doing a linear scan
     */
    private val vertexPriorityQueue = if (subGraphs.none { it.size > graph.size / 2 }) {
        VertexPriorityQueue(graph.size, weights, candidate.verticesMask)
    } else null

    fun getIntersectionSize(subGraphIndex: Int): Int {
        return intersectionCount[subGraphIndex]
    }

    fun removeWorstVertex() {
        if (vertexPriorityQueue == null) {
            remove(candidate.minVertexBy { weights[it] })
        } else {
            remove(vertexPriorityQueue.head())
        }
    }

    fun remove(vertex: Vertex) {
        candidateEdges -= degrees[vertex]
        editWeight(vertex) {
            candidate.remove(vertex)
        }
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
        subGraphs.forIf({ vertex in it }) { g, index ->
            intersectionCount[index]--
            g.forEachVertex { v ->
                editWeight(v) {
                    weights[v] += 4 * lambda / g.size
                }
            }
        }
    }

    fun add(vertex: Vertex) {
        editWeight(vertex) {
            candidate.add(vertex)
        }
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
        subGraphs.forIf({ vertex in it }) { g, index ->
            intersectionCount[index]++
            g.forEachVertex { v ->
                editWeight(v) {
                    weights[v] -= 4 * lambda / g.size
                }
            }
        }
    }

    private inline fun editWeight(v: Vertex, f: () -> Unit) {
        f()
        vertexPriorityQueue?.notifyVertexWeightChanged(v)
    }
}