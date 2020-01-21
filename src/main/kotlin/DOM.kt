class DOM(
    val graph: BaseGraph,
    val lambda: Double,
    val distance: Distance = MetricDistance
) {

    /**
     * Pag. 31, Algorithm 1
     */
    fun getDenseOverlappingSubGraphs(): Sequence<Graph> {
        return generateSequence(setOf<SubGraph>()) {
            it.plus(peel(it))
        }
            .drop(1)
            .map { it.last() }
    }

    /**
     * Pag. 12, problem 4
     */
    private fun Peeler.marginalGain(subGraphs: Set<SubGraph>): Double {
        return candidateDensity / 2 + lambda * subGraphs.sumByDouble {
            distance(candidate, it) { getIntersectionSize(it) }
        }
    }

    /**
     * Pag. 14, algorithm 2
     */
    private fun peel(subGraphs: Set<SubGraph>): SubGraph {
        val peeler = Peeler(graph, subGraphs, lambda)

        return findBestSubGraph(subGraphs) { consumer ->
            while (peeler.candidate.size > 3) {
                val minVertex = peeler.getWorstVertex()
                peeler.remove(minVertex)
                val pair = peeler.marginalGainModified(subGraphs)
                consumer(pair)

                //I already encountered this subgraph, and all its trivial modifications. I can break
                if (pair == null) break
            }
        }?.first ?: findWedge(subGraphs)
    }

    /**
     * Pag. 14, algorithm 3
     */
    private fun Peeler.marginalGainModified(subGraphs: Set<SubGraph>): Pair<SubGraph, Double>? {
        return if (candidate in subGraphs) {
            findBestSubGraph(subGraphs) { consumer ->
                graph.vertices.filter { it !in this.candidate }.forEach {
                    add(it)
                    consumer(candidate to marginalGain(subGraphs))
                    remove(it)
                }

                this.candidate.vertices.forEach {
                    remove(it)
                    consumer(candidate to marginalGain(subGraphs))
                    add(it)
                }
            }
        } else candidate to marginalGain(subGraphs)
    }

    /**
     * Pag.13, differences between Peel and Charikar
     * Replace U with a trivial subgraph of size 3.
     * Note: The wedge has nothing to do with candidate
     */
    private fun findWedge(subGraphs: Set<Graph>): SubGraph {
        return graph.allWedges.first { it !in subGraphs }
    }

    private inline fun findBestSubGraph(subGraphs: Set<Graph>, producer: (consumer: (Pair<SubGraph, Double>?) -> Unit) -> Unit): Pair<SubGraph, Double>? {
        var bestCandidate: SubGraph? = null
        var bestCandidateScore = Double.MIN_VALUE

        producer { pair ->
            if (pair != null && pair.first !in subGraphs) {
                val score = pair.second
                if (score > bestCandidateScore) {
                    bestCandidate = pair.first.clone()
                    bestCandidateScore = score
                }
            }
        }

        return bestCandidate?.let { it to bestCandidateScore }
    }
}