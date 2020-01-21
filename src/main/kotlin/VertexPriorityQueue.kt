class VertexPriorityQueue(
    size: Int,
    private val backingWeights: DoubleArray,
    private val isNotRemoved: BooleanArray
) : MinHeap(size) {

    init {
        minHeapify()
    }

    override fun getWeight(pos: Vertex): Double {
        return if (isNotRemoved[pos]) {
            backingWeights[pos]
        } else Double.MAX_VALUE
    }

    fun notifyVertexWeightChanged(v: Vertex) {
        notifyComparisonChanged(v)
    }
}