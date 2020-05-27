import datastructure.VerticesByDegreeQueue
import datastructure.VerticesLinkedList

/**
 * A class to compute the Densest Overlapping SubGraphs
 */
class DOSComputer(val graph: Graph, val lambda: Double, val distance: Distance = MetricDistance) {

    private val partitions = ArrayList<Partition>()
    private val subGraphs = ArrayList<SubGraph>()
    private val nodes: Array<VerticesLinkedList.Node> = Array(graph.size) { VerticesLinkedList.Node(it) }

    init {
        val vertices = VerticesByDegreeQueue(graph.maxDegree)
        nodes.forEach { n ->
            n.resetDegree(graph)
            vertices.add(n)
        }
        val emptyPartition = Partition(emptyList(), vertices)
        partitions.add(emptyPartition)
    }

    /**
     * The [SubGraph] already peeled
     */
    fun subGraphs(): List<SubGraph> = subGraphs

    /**
     * Do a peeling step. See page 14, algorithm 2
     */
    fun peel(): SubGraph {
        return Peeler().peel()
    }

    /**
     * A partition of [Graph]. Contains all the [Vertex] that belong in [partitionKey].
     */
    inner class Partition(
        val partitionKey: List<SubGraph>,
        var vertices: VerticesByDegreeQueue
    )

    /**
     * Class that handles the peel step. See page 14, algorithm 2
     */
    private inner class Peeler {

        private val candidate = SnapshottableSubGraph(graph.toSubGraph())
        private var candidateEdges = graph.edges.size
        private val intersections: MutableList<Int> = subGraphs.mapTo(ArrayList()) { it.size }
        val partitionsForPeeling = partitions.map {
            PartitionForPeeling(it)
        }

        /**
         * Does a peeling step. In particular:
         * - Initially, the candidate sub graph spans the entire graph
         * - Iteratively, the vertex that minimizes a weight function is removed
         * - The process is repeated until candidate is empty
         * While iterating, the marginalGain is computed for candidate. The final subgraph
         * is the sub graph that maximized the marginalGain

         */
        fun peel(): SubGraph {
            var best: SnapshottableSubGraph.GraphSnapshot = candidate.snapshot()
            var bestGain = marginalGain()
            do {
                val worst = getMinimum(partitionsForPeeling)
                if (worst != null) {
                    candidateEdges -= worst.min().degree
                    remove(worst)
                    val marginalGain = marginalGain()
                    if (marginalGain >= bestGain) {
                        best = candidate.snapshot()
                        bestGain = marginalGain
                    }
                    //TODO: do things if candidate in subGraphs
                }
            } while (worst != null)
            check(candidateEdges == 0)

            val bestSubGraph = best.toSubGraph()

            partitionsForPeeling.mapTo(partitions) { p ->
                p.createPartition(bestSubGraph)
            }
            partitions.removeAll { it.vertices.isEmpty() }
            nodes.forEach { it.resetDegree(graph) }

            subGraphs.add(bestSubGraph)
            return bestSubGraph
        }

        /**
         * Computes the marginal gain for [candidate]
         */
        private fun marginalGain(): Double {
            return if (candidate.size == 0) {
                Double.MIN_VALUE
            } else {
                candidateEdges.toDouble() / candidate.size / 2 + lambda * subGraphs.sumByDoubleIndexed { g, index ->
                    distance(candidate.size, g.size, intersections[index])
                }
            }
        }

        /**
         * Returns the partition that contains the vertex that minimizes the weight function.
         */
        private fun getMinimum(partitionsForCandidate: List<PartitionForPeeling>): PartitionForPeeling? {
            var min: PartitionForPeeling? = null
            for (p in partitionsForCandidate) {
                if (p.isNotEmpty() && (min == null || p.minWeight() < min.minWeight())) {
                    min = p
                }
            }
            return min
        }

        /**
         * Removes [minimum] from [candidate]. Also updates all the variables to keep things consistent
         */
        private fun remove(minimum: PartitionForPeeling) {
            val removed = minimum.removeMin()
            forEachConnectedVertex(removed.vertex) { connected ->
                val node = nodes[connected]
                node.changeDegree(node.degree - 1)
            }
            removed.changeDegree(0)
            minimum.oldPartition.vertices.add(removed)
            candidate.remove(removed.vertex)
            forEachSubGraphs(removed.vertex) { _, index ->
                intersections[index]--
            }
            partitionsForPeeling.noIteratorForEach { p ->
                p.oldPartition.partitionKey.noIteratorForEach { sg ->
                    if (sg.contains(removed.vertex)) {
                        p.peelWeight += 4 * lambda / sg.size
                    }
                }
            }
        }

        /**
         * Executes [f] for each [SubGraph] that contains [vertex]
         */
        private inline fun forEachSubGraphs(vertex: Vertex, f: (SubGraph, subGraphIndex: Int) -> Unit) {
            subGraphs.forIf({ vertex in it }) { g, index ->
                f(g, index)
            }
        }

        /**
         * Executes [f] for each [Vertex] that is connected to [vertex], considering only the edges in [candidate]
         */
        private inline fun forEachConnectedVertex(vertex: Vertex, f: (connected: Vertex) -> Unit) {
            graph.edgesMap[vertex].noIteratorForEach { e ->
                val other = e.otherVertex(vertex)
                if (other in candidate) {
                    f(other)
                }
            }
        }

        /**
         * An utility class to create a [Partition] after the peel.
         * It represent a partition that contains all the [Vertex] that are
         * in all the [oldPartition].partitionKey and in [candidate]
         */
        private inner class PartitionForPeeling(
            val oldPartition: DOSComputer.Partition
        ) {
            val verticesByDegree = oldPartition.vertices
            var peelWeight: Double = -4 * lambda * oldPartition.partitionKey.size

            init {
                oldPartition.vertices = VerticesByDegreeQueue(graph.maxDegree)
            }

            fun isEmpty() = verticesByDegree.isEmpty()
            fun isNotEmpty() = verticesByDegree.isNotEmpty()
            fun minWeight() = peelWeight + verticesByDegree.min().degree
            fun min() = verticesByDegree.min()
            fun removeMin() = verticesByDegree.removeMin()

            fun createPartition(best: SubGraph): DOSComputer.Partition {
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
}