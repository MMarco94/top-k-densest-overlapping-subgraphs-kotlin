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

    inner class PartitionInfo(
        val key: PartitionKey,
        val partition: SubGraph,
        var weight: Double = -4 * lambda * subGraphs.indices.count { key.inSubGraph(it) }
    ) {
        val queue = SubGraphPriorityQueue(partition, degrees, candidate.verticesMask)
        fun getWeight(vertex: Vertex): Double {
            return weight + queue.getWeight(vertex)
        }
    }

    private val partitionsInfo: List<PartitionInfo> = currentResult.partitions.map { (pk, subGraph) ->
        PartitionInfo(pk, subGraph)
    }

    fun getIntersectionSize(subGraphIndex: Int): Int {
        return intersectionSize[subGraphIndex]
    }

    private fun checkNoTemporaryOperation() = check(temporaryVertex < 0)

    fun removeWorstVertex() {
        checkNoTemporaryOperation()
        val minUsingPartitions = partitionsInfo.minBy { info ->
            info.getWeight(info.queue.head())
        }!!
        remove(minUsingPartitions.queue.head())
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
        updatePartitionsWeights { info ->
            var newW = info.weight
            forEachSubGraphs(vertex) { sg, index ->
                if (info.key.inSubGraph(index)) {
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
        updatePartitionsWeights { info ->
            var newW = info.weight
            forEachSubGraphs(vertex) { sg, index ->
                if (info.key.inSubGraph(index)) {
                    newW -= 4 * lambda / sg.size
                }
            }
            newW
        }
    }

    private inline fun updatePartitionsWeights(newWeightComputer: (PartitionInfo) -> Double) {
        partitionsInfo.forEach {
            it.weight = newWeightComputer(it)
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
            partitionsInfo.forEach {
                it.queue.notifyVertexWeightChanged(v)
            }
        }
    }
}