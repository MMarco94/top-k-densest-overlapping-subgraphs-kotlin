import queues.SubGraphPriorityQueue

class Peeler(
    val graph: Graph,
    val currentResult: DOSResult,
    val lambda: Double
) {
    val candidate = graph.toSubGraph()

    private val subGraphs get() = currentResult.subGraphs
    private var candidateEdges = graph.edges.size
    val candidateDensity get() = candidateEdges.toDouble() / candidate.size

    private val degrees = IntArray(graph.size) { graph.edgesMap[it].size }
    private val intersectionSize = subGraphs.mapTo(ArrayList(subGraphs.size)) { it.size }

    private var temporaryVertex: Vertex = -1
    private var temporaryIsAdd = false

    private val partitionsWeight: MutableMap<PartitionKey, Double> = currentResult.partitions.mapValuesTo(HashMap()) { (pk, _) ->
        -4 * lambda * subGraphs.indices.count { pk.inSubGraph(it) }
    }
    private val partitionsQueue: Map<PartitionKey, SubGraphPriorityQueue> = currentResult.partitions.mapValues { (_, subGraph) ->
        SubGraphPriorityQueue(subGraph, degrees, candidate.verticesMask)
    }

    fun getIntersectionSize(subGraphIndex: Int): Int {
        return intersectionSize[subGraphIndex]
    }

    private fun checkNoTemporaryOperation() = check(temporaryVertex < 0)

    fun removeWorstVertex() {
        checkNoTemporaryOperation()
        val minUsingPartitions = partitionsQueue.entries.minBy { (pk, queue) ->
            partitionsWeight.getValue(pk) + queue.getWeight(queue.head())
        }!!
        remove(minUsingPartitions.value.head())
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
            degrees[v] -= count
        }
        forEachSubGraphs(vertex) { _, sgi ->
            intersectionSize[sgi]--
        }
        updatePartitionsWeights { pk, w ->
            var newW = w
            forEachSubGraphs(vertex) { sg, index ->
                if (pk.inSubGraph(index)) {
                    newW += 4 * lambda / sg.size
                }
            }
            newW
        }
    }

    private fun add(vertex: Vertex, updateQueue: Boolean = true) {
        editWeight(vertex, updateQueue) {
            candidate.add(vertex)
        }
        forEachConnectedVertex(vertex, updateQueue) { v, count ->
            degrees[v] += count
        }
        candidateEdges += degrees[vertex]
        forEachSubGraphs(vertex) { _, sgi ->
            intersectionSize[sgi]++
        }
        updatePartitionsWeights { pk, w ->
            var newW = w
            forEachSubGraphs(vertex) { sg, index ->
                if (pk.inSubGraph(index)) {
                    newW -= 4 * lambda / sg.size
                }
            }
            newW
        }
    }

    private inline fun updatePartitionsWeights(newWeightComputer: (PartitionKey, oldWeight: Double) -> Double) {
        partitionsWeight.iterator().forEach {
            it.setValue(newWeightComputer(it.key, it.value))
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

    private inline fun forEachSubGraphs(vertex: Vertex, sg: (SubGraph, subGraphIndex: Int) -> Unit) {
        subGraphs.forIf({ vertex in it }) { g, index ->
            sg(g, index)
        }
    }

    private inline fun editWeight(v: Vertex, updateQueue: Boolean, f: () -> Unit) {
        f()
        if (updateQueue) {
            partitionsQueue.forEach { it.value.notifyVertexWeightChanged(v) }
        }
    }
}