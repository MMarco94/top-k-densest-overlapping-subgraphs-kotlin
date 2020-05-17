data class DOSResult(val dos: DOS, val subGraphs: List<SubGraph>) {

    val partitions: Map<PartitionKey, SubGraph> = HashMap<PartitionKey, SubGraph>().apply {
        dos.graph.forEachVertex { node ->
            val partitionKey = PartitionUtils.getPartitionKey(node, subGraphs)
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