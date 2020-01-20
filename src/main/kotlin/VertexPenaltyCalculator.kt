class VertexPenaltyCalculator(
    val graph: BaseGraph,
    val subGraphs: Set<Graph>
) {
    private val weights = graph.vertices.associateWithTo(HashMap(graph.vertices.size)) { v ->
        subGraphs.sumIf({ v in it.vertices }) {
            intersectionCount(graph, it) / it.size
        }
    }

    fun getPenalty(vertex: Vertex): Double {
        return weights.getValue(vertex)
    }

    fun remove(vertex: Vertex) {
        subGraphs.forIf({ vertex in it.vertices }) { g ->
            g.vertices.forEach { v ->
                weights.computeIfPresent(v) { _, w ->
                    w - 1.0 / g.size
                }
            }
        }
    }
}