typealias PartitionKey = Long

fun PartitionKey.inSubGraph(subGraphIndex: Int): Boolean {
    return this.shr(subGraphIndex).rem(2) == 1L
}

object PartitionUtils {

    inline fun getPartitionKey(subGraphsCount: Int, isInSubGraph: (subGraphIndex: Int) -> Boolean): PartitionKey {
        if (subGraphsCount > 64) throw UnsupportedOperationException()
        var ret = 0L
        for (sg in 0 until subGraphsCount) {
            ret = ret.shl(1)
            if (isInSubGraph(sg)) ret++
        }
        return ret
    }

    fun getPartitionKey(vertex: Vertex, subGraphs: List<SubGraph>): PartitionKey {
        return getPartitionKey(subGraphs.size) { sg ->
            subGraphs[sg].contains(vertex)
        }
    }
}