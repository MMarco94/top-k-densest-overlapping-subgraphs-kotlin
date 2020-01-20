class VertexPenaltyCalculator(
    val graph: BaseGraph,
    val subGraphs: Set<SubGraph>
) {
    private val weights = DoubleArray(graph.size) { vertex ->
        subGraphs.count { vertex in it }.toDouble()
    }

    fun getPenalty(vertex: Vertex): Double {
        return weights[vertex]
    }

    fun remove(vertex: Vertex) {
        subGraphs.forIf({ vertex in it }) { g ->
            g.forEachVertex { v ->
                weights[v] -= 1.0 / g.size
            }
        }
    }
}