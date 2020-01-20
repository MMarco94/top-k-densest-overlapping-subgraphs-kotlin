class DOM(
    val graph: BaseGraph,
    val lambda: Double,
    val distance: Distance = MetricDistance
) {

    /**
     * Pag. 31, Algorithm 1
     */
    fun getDenseOverlappingSubGraphs(): Sequence<Graph> {
        return generateSequence(setOf<Graph>()) {
            it.plus(peel(it))
        }
            .drop(1)
            .map { it.last() }
    }
    /**
     * Pag. 12, problem 4
     */
    private fun Graph.marginalGain(subGraphs: Set<Graph>): Double {
        return density / 2 + lambda * subGraphs.sumByDouble { distance(this, it) }
    }

    /**
     * Pag. 14, algorithm 2
     */
    private fun peel(subGraphs: Set<Graph>): Graph {
        val penaltyCalculator = VertexPenaltyCalculator(graph, subGraphs)

        val candidatesCount = graph.size - 1 //n to 2 inclusive


        var bestCandidate: Graph? = null
        var bestCandidateScore = Double.MIN_VALUE

        val candidate = MutableSubGraph(graph)
        for (i in 1 until candidatesCount) {
            val minVertex = candidate.vertices.minBy { v ->
                candidate.degreeOf(v) - 4 * lambda * penaltyCalculator.getPenalty(v)
            }!!
            penaltyCalculator.remove(minVertex)
            candidate.remove(minVertex)

            val score = candidate
                .modifyIfNeeded(subGraphs)
                .marginalGain(subGraphs)
            if (score > bestCandidateScore) {
                bestCandidate = candidate.toImmutable()
                bestCandidateScore = score
            }
        }

        return bestCandidate!!
    }

    /**
     * Pag. 14, algorithm 3
     */
    private fun Graph.modifyIfNeeded(subGraphs: Set<Graph>): Graph {
        return if (this in subGraphs) {
            modify(subGraphs)
        } else this
    }

    private fun Graph.modify(subGraphs: Set<Graph>): Graph {
        val x = graph.vertices.filter { it !in vertices }.map { subGraph(vertices.plus(it)) }.filter { it !in subGraphs }
        val y = vertices.map { without(it) }.filter { it !in subGraphs }
        return if (x.isEmpty() && density <= 5.0 / 3.0) {//TODO: in their code is 7/6
            /**
             * Pag.13, differences between Peel and Charikar
             * Replace U with a trivial subgraph of size 3.
             * Note: The wedge has nothing to do with candidate
             */
            graph.allWedges.first { it !in subGraphs }
        } else {
            (x + y).maxBy { it.marginalGain(subGraphs) }!!
        }
    }
}