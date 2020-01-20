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

        var candidate = graph.toSubGraph()
        return findBestSubGraph(subGraphs) { consumer ->
            while (candidate.size > 2) {
                val minVertex = candidate.minVertexBy { v ->
                    candidate.degreeOf(v) - 4 * lambda * penaltyCalculator.getPenalty(v)
                }
                penaltyCalculator.remove(minVertex)
                candidate.remove(minVertex)

                candidate = candidate.modifyIfNeeded(subGraphs)
                consumer(candidate)
            }
        }!!
    }

    /**
     * Pag. 14, algorithm 3
     */
    private fun SubGraph.modifyIfNeeded(subGraphs: Set<Graph>): SubGraph {
        return if (this in subGraphs) {
            modify(subGraphs)
        } else this
    }

    private fun SubGraph.modify(subGraphs: Set<Graph>): SubGraph {
        val candidate = findBestSubGraph(subGraphs) { consumer ->
            val nodesToAdd = graph.vertices.filter { it !in this }
            nodesToAdd.forEach {
                add(it)
                consumer(this)
                remove(it)
            }

            val nodesToRemove = vertices
            nodesToRemove.forEach {
                add(it)
                consumer(this)
                remove(it)
            }

        }
        /**
         * TODO:
         * in their paper they also add the condition density <= 5/3 (that in their code is <= 7/6)
         */
        if (candidate == null) {
            /**
             * Pag.13, differences between Peel and Charikar
             * Replace U with a trivial subgraph of size 3.
             * Note: The wedge has nothing to do with candidate
             */
            return graph.allWedges.first { it !in subGraphs }
        } else return candidate
    }

    private inline fun findBestSubGraph(subGraphs: Set<Graph>, f: (consumer: (SubGraph) -> Unit) -> Unit): SubGraph? {
        var bestCandidate: SubGraph? = null
        var bestCandidateScore = Double.MIN_VALUE

        f { sg ->
            if (sg !in subGraphs) {
                val score = sg.marginalGain(subGraphs)
                if (score > bestCandidateScore) {
                    bestCandidate = sg.clone()
                    bestCandidateScore = score
                }
            }
        }

        return bestCandidate
    }
}