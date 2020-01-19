class DOM(
    val graph: Graph,
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
        val candidates = generateSequence(graph) { prev ->
            if (prev.size > 2) {
                val minVertex = prev.vertices.minBy { v ->
                    prev.degreeOf(v) - 4 * lambda * subGraphs.sumIf({ v in it.vertices }) { intersectionCount(prev, it) / it.size }
                }!!
                prev.minus(minVertex)
            } else null
        }
        return candidates.maxBy {
            it
                .modifyIfNeeded(subGraphs)
                .marginalGain(subGraphs)
        }!!
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
        val x = graph.vertices.filter { it !in vertices }.map { plus(it) }.filter { it !in subGraphs }
        val y = vertices.map { minus(it) }.filter { it !in subGraphs }
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