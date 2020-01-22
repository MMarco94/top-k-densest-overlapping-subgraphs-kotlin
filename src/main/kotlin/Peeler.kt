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

    private var temporaryVertex: Vertex = -1
    private var temporaryIsAdd = false

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

    private fun checkNoTemporaryOperation() = check(temporaryVertex < 0)

    fun removeWorstVertex() {
        checkNoTemporaryOperation()
        if (vertexPriorityQueue == null) {
            remove(candidate.minVertexBy { weights[it] })
        } else {
            remove(vertexPriorityQueue.head())
        }
    }

    fun removeTemporary(vertex: Vertex) {
        checkNoTemporaryOperation()
        temporaryVertex = vertex
        temporaryIsAdd = false
        remove(vertex, false)
    }


    fun addTemporary(vertex: Vertex) {
        checkNoTemporaryOperation()
        temporaryVertex = vertex
        temporaryIsAdd = true
        add(vertex, false)
    }

    fun restoreTemporary() {
        check(temporaryVertex >= 0)
        if (temporaryIsAdd) {
            remove(temporaryVertex, false)
        } else {
            add(temporaryVertex, false)
        }
        temporaryVertex = -1
    }

    private fun remove(vertex: Vertex, updateQueue: Boolean = true) {
        candidateEdges -= degrees[vertex]
        editWeight(vertex, updateQueue) {
            candidate.remove(vertex)
        }
        forEachConnectedVertex(vertex, updateQueue) { v, count ->
            weights[v] -= count.toDouble()
            degrees[v] -= count
        }
        forEachSubGraphs(vertex, updateQueue, { _, sgi -> intersectionCount[sgi]-- }) { g, v ->
            weights[v] += 4 * lambda / g.size
        }
    }

    private fun add(vertex: Vertex, updateQueue: Boolean = true) {
        editWeight(vertex, updateQueue) {
            candidate.add(vertex)
        }
        forEachConnectedVertex(vertex, updateQueue) { v, count ->
            weights[v] += count.toDouble()
            degrees[v] += count
        }
        candidateEdges += degrees[vertex]
        forEachSubGraphs(vertex, updateQueue, { _, sgi -> intersectionCount[sgi]++ }) { g, v ->
            weights[v] -= 4 * lambda / g.size
        }
    }

    private inline fun forEachConnectedVertex(vertex: Vertex, updateQueue: Boolean, f: (v: Vertex, count: Int) -> Unit) {
        var vertexCount = 0
        graph.edgesMap[vertex].forEach { e ->
            val other = e.otherVertex(vertex)
            if (other in candidate) {
                editWeight(other, updateQueue) {
                    f(other, 1)
                }
                vertexCount++
            }
        }
        if (vertexCount > 0) {
            editWeight(vertex, updateQueue) {
                f(vertex, vertexCount)
            }
        }
    }

    private inline fun forEachSubGraphs(vertex: Vertex, updateQueue: Boolean, sg: (SubGraph, subGraphIndex: Int) -> Unit, f: (subGraph: SubGraph, v: Vertex) -> Unit) {
        subGraphs.forIf({ vertex in it }) { g, index ->
            sg(g, index)
            g.forEachVertex { v ->
                editWeight(v, updateQueue) {
                    f(g, v)
                }
            }
        }
    }

    private inline fun editWeight(v: Vertex, updateQueue: Boolean, f: () -> Unit) {
        f()
        if (updateQueue) {
            vertexPriorityQueue?.notifyVertexWeightChanged(v)
        }
    }
}