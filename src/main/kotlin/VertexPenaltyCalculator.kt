class VertexPenaltyCalculator(
    val graph: BaseGraph,
    val subGraphs: Set<Graph>
) {
    private val weights = graph.vertexList.mapTo(ArrayList(graph.size)) { vertex ->
        subGraphs.count { vertex in it }.toDouble()
    }

    fun getPenalty(vertex: Vertex): Double {
        return weights[vertex.id]
    }

    fun remove(vertex: Vertex) {
        subGraphs.forIf({ vertex in it }) { g ->
            g.vertices.forEach { v ->
                weights[v.id] -= 1.0 / g.size
            }
        }
    }
}