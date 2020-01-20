class Peeler(
    val graph: BaseGraph,
    val subGraphs: Set<SubGraph>,
    val lambda: Double
) {
    val candidate = graph.toSubGraph()

    private var edges = graph.edges.size
    private val degrees = IntArray(graph.size) { graph.edgesMap[it].size }
    private val penalties = DoubleArray(graph.size) { vertex ->
        -4 * lambda * subGraphs.count { vertex in it }
    }
    val candidateDensity get() = edges.toDouble() / candidate.size

    fun getWorstVertex(): Vertex {
        return candidate.minVertexBy { vertex ->
            degrees[vertex] + penalties[vertex]
        }
    }

    fun remove(vertex: Vertex) {
        edges -= degrees[vertex]
        candidate.remove(vertex)
        graph.edgesMap[vertex].forEach { (a, b) ->
            degrees[a]--
            degrees[b]--
        }
        subGraphs.forIf({ vertex in it }) { g ->
            g.forEachVertex { v ->
                penalties[v] += 4 * lambda / g.size
            }
        }
    }

    fun add(vertex: Vertex) {
        graph.edgesMap[vertex].forEach { (a, b) ->
            degrees[a]++
            degrees[b]++
        }
        candidate.add(vertex)
        edges += degrees[vertex]
        subGraphs.forIf({ vertex in it }) { g ->
            g.forEachVertex { v ->
                penalties[v] -= 4 * lambda / g.size
            }
        }
    }
}