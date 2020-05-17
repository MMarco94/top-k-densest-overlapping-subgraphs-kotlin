data class DOSResult(val dos: DOS, val subGraphs: List<SubGraph>) {

    val partitions: Map<PartitionKey, SubGraph> = HashMap<PartitionKey, SubGraph>().apply {
        dos.graph.forEachVertex { node ->
            val partitionKey = getPartitionKey(subGraphs.size) { sg ->
                subGraphs[sg].contains(node)
            }
            getOrPut(partitionKey) {
                dos.graph.toEmptySubGraph()
            }.add(node)
        }
    }

    init {
        println("Result #${subGraphs.size} has ${partitions.size} vertex partitions")
        //println("\n\npartitions = " + partitions.entries.joinToString("\n") { it.key.toString() + ": " + it.value })
    }

    fun add(subGraph: SubGraph): DOSResult {
        return DOSResult(dos, subGraphs.plus(subGraph))
    }
}

typealias PartitionKey = Long

fun PartitionKey.inSubGraph(subGraphIndex: Int): Boolean {
    return this.shr(subGraphIndex).rem(2) == 1L
}

private inline fun getPartitionKey(subGraphsCount: Int, isInSubGraph: (subGraphIndex: Int) -> Boolean): PartitionKey {
    if (subGraphsCount > 64) throw UnsupportedOperationException()
    var ret = 0L
    for (sg in 0 until subGraphsCount) {
        ret = ret.shl(1)
        if (isInSubGraph(sg)) ret++
    }
    return ret
}