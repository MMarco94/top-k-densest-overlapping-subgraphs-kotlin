class VertexPenaltyCalculator(
    val graph: BaseGraph,
    val subGraphs: Set<Graph>
) {
    private val weights = DoubleArray(graph.size) { index ->
        val vertex = graph.vertexList[index]
        subGraphs.count { vertex in it }.toDouble()
    }

    fun getPenalty(vertex: Vertex): Double {
        return weights[vertex.id]
    }

    fun remove(vertex: Vertex) {
        subGraphs.forIf({ vertex in it }) { g ->
            g.forEachVertex { v ->
                weights[v.id] -= 1.0 / g.size
            }
        }
    }
}