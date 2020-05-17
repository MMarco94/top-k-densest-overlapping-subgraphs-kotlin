class DOS(
    val graph: Graph,
    val lambda: Double,
    val distance: Distance = MetricDistance
) {

    /**
     * Pag. 31, Algorithm 1
     */
    fun getDenseOverlappingSubGraphs(): Sequence<DOSResult> {
        return generateSequence(DOSResult(this, emptyList())) {
            it.add(peel(it))
        }
    }

    /**
     * Pag. 12, problem 4
     */
    private fun Peeler.marginalGain(subGraphs: List<SubGraph>): Double {
        return candidateDensity / 2 + lambda * subGraphs.sumByDoubleIndexed { g, index ->
            distance(candidate, g, getIntersectionSize(index))
        }
    }

    /**
     * Pag. 14, algorithm 2
     */
    private fun peel(currentState: DOSResult): SubGraph {
        val peeler = Peeler(graph, currentState, lambda)

        val best = findBestSubGraph(currentState.subGraphs) { consumer ->
            while (peeler.candidate.size > 3) {
                peeler.removeWorstVertex()
                val pair = peeler.marginalGainModified(currentState.subGraphs)
                consumer(pair)

                //I already encountered this subgraph, and all its trivial modifications. I can break
                if (pair == null) break
            }
        }
        return best?.first ?: findWedge(currentState.subGraphs)
    }

    /**
     * Pag. 14, algorithm 3
     */
    private fun Peeler.marginalGainModified(subGraphs: List<SubGraph>): Pair<SubGraph, Double>? {
        return if (candidate in subGraphs) {
            findBestSubGraph(subGraphs) { consumer ->
                graph.forEachVertex { v ->
                    if (v !in this.candidate) {
                        addTemporary(v)
                        consumer(candidate to marginalGain(subGraphs))
                        restoreTemporary()
                    }
                }
                this.candidate.forEachVertex { v ->
                    removeTemporary(v)
                    consumer(candidate to marginalGain(subGraphs))
                    restoreTemporary()
                }
            }
        } else candidate to marginalGain(subGraphs)
    }

    /**
     * Pag.13, differences between Peel and Charikar
     * Replace U with a trivial subgraph of size 3.
     * Note: The wedge has nothing to do with candidate
     */
    private fun findWedge(subGraphs: List<SubGraph>): SubGraph {
        return graph.allWedges.first { it !in subGraphs }
    }

    private inline fun findBestSubGraph(subGraphs: List<SubGraph>, producer: (consumer: (Pair<SubGraph, Double>?) -> Unit) -> Unit): Pair<SubGraph, Double>? {
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