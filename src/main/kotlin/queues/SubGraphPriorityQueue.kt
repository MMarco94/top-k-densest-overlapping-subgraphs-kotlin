package queues

import SubGraph
import Vertex

class SubGraphPriorityQueue(
    private val subGraph: SubGraph,
    private val backingWeights: IntArray,
    private val isNotRemoved: BooleanArray
) : IntMinHeap(subGraph.parent.size) {

    init {
        minHeapify()
    }

    override fun getWeight(pos: Vertex): Int {
        return if (subGraph.contains(pos) && isNotRemoved[pos]) {
            backingWeights[pos]
        } else Int.MAX_VALUE
    }

    fun notifyVertexWeightChanged(v: Vertex) {
        if (subGraph.contains(v)) {
            notifyComparisonChanged(v)
        }
    }
}