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
    val candidateDensity get() = candidateEdges.toDouble() / candidate.size

    fun getWorstVertex(): Vertex {
        return candidate.minVertexBy { vertex ->
            weights[vertex]
        }
    }

    fun remove(vertex: Vertex) {
        candidateEdges -= degrees[vertex]
        candidate.remove(vertex)
        graph.edgesMap[vertex].forEach { e ->
            if (e.otherVertex(vertex) in candidate) {
                degrees[e.a]--
                degrees[e.b]--
                weights[e.a]--
                weights[e.b]--
            }
        }
        subGraphs.forIf({ vertex in it }) { g ->
            g.forEachVertex { v ->
                weights[v] += 4 * lambda / g.size
            }
        }
    }

    fun add(vertex: Vertex) {
        candidate.add(vertex)
        graph.edgesMap[vertex].forEach { e ->
            if (e.otherVertex(vertex) in candidate) {
                degrees[e.a]++
                degrees[e.b]++
                weights[e.b]++
                weights[e.a]++
            }
        }
        candidateEdges += degrees[vertex]
        subGraphs.forIf({ vertex in it }) { g ->
            g.forEachVertex { v ->
                weights[v] -= 4 * lambda / g.size
            }
        }
    }
}