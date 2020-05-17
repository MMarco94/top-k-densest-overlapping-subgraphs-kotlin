package queues

import SubGraph
import Vertex

class SubGraphPriorityQueue(
    private val subGraph: SubGraph,
    private val backingWeights: DoubleArray,
    private val isNotRemoved: BooleanArray
) : MinHeap(subGraph.parent.size) {

    init {
        minHeapify()
    }

    override fun getWeight(pos: Vertex): Double {
        return if (subGraph.contains(pos) && isNotRemoved[pos]) {
            backingWeights[pos]
        } else Double.MAX_VALUE
    }

    fun notifyVertexWeightChanged(v: Vertex) {
        if (subGraph.contains(v)) {
            notifyComparisonChanged(v)
        }
    }
}