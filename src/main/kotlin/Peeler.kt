import datastructure.VerticesByDegreeQueue
import datastructure.VerticesLinkedList

class Peeler(val graph: Graph, val lambda: Double, val distance: Distance = MetricDistance) {

    private val partitions = ArrayList<Partition>()
    val subGraphs = ArrayList<SubGraph>()
    private val nodes: Array<VerticesLinkedList.Node> = Array(graph.size) { VerticesLinkedList.Node(it) }

    init {
        val vertices = VerticesByDegreeQueue(nodes)
        nodes.forEach { n ->
            n.resetDegree(graph)
            vertices.add(n)
        }
        val emptyPartition = Partition(emptyList(), vertices)
        partitions.add(emptyPartition)
    }

    fun peelNewSubGraph(): SubGraph {
        val candidate: SubGraph = graph.toSubGraph()
        var candidateEdges = graph.edges.size
        val intersections: MutableList<Int> = subGraphs.mapTo(ArrayList<Int>()) { it.size }

        val partitionsForPeeling = partitions.map {
            it.startNewPeeling()
        }

        var best: SubGraph = candidate.clone() //TODO: avoid cloning
        var bestGain = marginalGain(candidateEdges, candidate, intersections)
        do {
            val worst = getWorst(partitionsForPeeling)
            if (worst != null) {
                candidateEdges -= worst.min().degree
                val removed = removeWorst(candidate, partitionsForPeeling, intersections, worst)
                check(removed.degree == 0)
                val marginalGain = marginalGain(candidateEdges, candidate, intersections)
                if (marginalGain >= bestGain) {
                    best = candidate.clone() //TODO: avoid cloning
                    bestGain = marginalGain
                }
                //TODO: do things if candidate in subGraphs
            }
        } while (worst != null)
        check(candidateEdges == 0)

        partitionsForPeeling.mapTo(partitions) { p ->
            p.createPartition(best)
        }
        partitions.removeAll { it.vertices.isEmpty() }
        nodes.forEach { it.resetDegree(graph) }

        subGraphs.add(best)
        return best
    }

    private fun marginalGain(candidateEdges: Int, candidate: SubGraph, intersections: MutableList<Int>): Double {
        return if (candidate.size == 0) {
            Double.MIN_VALUE
        } else {
            candidateEdges.toDouble() / candidate.size / 2 + lambda * subGraphs.sumByDoubleIndexed { g, index ->
                distance(candidate, g, intersections[index])
            }
        }
    }

    private fun removeWorst(candidate: SubGraph, partitionsForCandidate: List<PartitionForPeeling>, intersections: MutableList<Int>, worst: PartitionForPeeling): VerticesLinkedList.Node {
        val removed = worst.removeMin()
        candidate.remove(removed.vertex)
        forEachConnectedVertex(candidate, removed.vertex) { connected, count ->
            val node = nodes[connected]
            node.changeDegree(node.degree - count)
        }
        forEachSubGraphs(removed.vertex) { _, index ->
            intersections[index]--
        }
        worst.oldPartition.vertices.add(removed)
        partitionsForCandidate.noIteratorForEach { p ->
            p.oldPartition.partitionKey.noIteratorForEach { sg ->
                if (sg.contains(removed.vertex)) {
                    p.peelWeight += 4 * lambda / sg.size
                }
            }
        }
        return removed
    }

    private fun getWorst(partitionsForCandidate: List<PartitionForPeeling>): PartitionForPeeling? {
        var min: PartitionForPeeling? = null
        for (p in partitionsForCandidate) {
            if (p.isNotEmpty() && (min == null || p.minWeight() < min.minWeight())) {
                min = p
            }
        }
        return min
    }

    private inline fun forEachSubGraphs(vertex: Vertex, sg: (SubGraph, subGraphIndex: Int) -> Unit) {
        subGraphs.forIf({ vertex in it }) { g, index ->
            sg(g, index)
        }
    }

    private inline fun forEachConnectedVertex(candidate: SubGraph, vertex: Vertex, f: (connected: Vertex, count: Int) -> Unit) {
        var vertexCount = 0
        graph.edgesMap[vertex].noIteratorForEach { e ->
            val other = e.otherVertex(vertex)
            if (other in candidate) {
                f(other, 1)
                vertexCount++
            }
        }
        if (vertexCount > 0) {
            f(vertex, vertexCount)
        }
    }

    private inner class Partition(
        val partitionKey: List<SubGraph>,
        var vertices: VerticesByDegreeQueue
    ) {

        fun startNewPeeling(): PartitionForPeeling {
            return PartitionForPeeling(
                this
            )
        }
    }

    private inner class PartitionForPeeling(
        val oldPartition: Partition
    ) {
        val verticesByDegree = oldPartition.vertices
        var peelWeight: Double = -4 * lambda * oldPartition.partitionKey.size

        init {
            oldPartition.vertices = VerticesByDegreeQueue(nodes)
        }

        fun isEmpty() = verticesByDegree.isEmpty()
        fun isNotEmpty() = verticesByDegree.isNotEmpty()
        fun minWeight() = peelWeight + verticesByDegree.min().degree
        fun min() = verticesByDegree.min()
        fun removeMin() = verticesByDegree.removeMin()

        fun createPartition(best: SubGraph): Partition {
            check(verticesByDegree.isEmpty())
            best.forEachVertex { v ->
                val node = nodes[v]
                if (node.queue == oldPartition.vertices) {
                    node.remove()
                    node.resetDegree(graph)
                    verticesByDegree.add(node)
                }
            }
            return Partition(oldPartition.partitionKey.plus(best), verticesByDegree)
        }
    }
}