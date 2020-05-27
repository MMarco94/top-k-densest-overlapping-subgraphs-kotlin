typealias Vertex = Int

data class Edge(val a: Vertex, val b: Vertex)

class Graph(val size: Int, val edges: Set<Edge>) {
    val vertices = (0 until size).asSequence()
    val connectionsMap: List<List<Vertex>> = List(size) { mutableListOf<Vertex>() }.also { arr ->
        edges.forEach { e ->
            arr[e.a].add(e.b)
            arr[e.b].add(e.a)
        }
    }
    val maxDegree = connectionsMap.maxBy { it.size }?.size ?: 0
    val allWedges = sequence {
        connectionsMap.forEachIndexed { v1, edges ->
            edges.forEach { v2 ->
                connectionsMap[v2].forEach { v3 ->
                    if (v3 != v1) {
                        yield(setOf(v1, v2, v3))
                    }
                }
            }
        }
    }.map { subGraph(it) }

    fun toSubGraph() = SubGraph(this)
    fun toEmptySubGraph() = SubGraph(0, BooleanArray(size), this)

    private fun subGraph(vertices: Set<Vertex>) = SubGraph(
        vertices.size,
        BooleanArray(vertices.size) { it in vertices },
        this
    )
}

class SubGraph {
    val parent: Graph

    val verticesMask: BooleanArray
    val vertices get() = parent.vertices.filter { verticesMask[it] }
    var size: Int

    constructor(parent: Graph) {
        this.verticesMask = BooleanArray(parent.size) { true }
        this.parent = parent
        this.size = parent.size
    }

    constructor(size: Int, verticesMask: BooleanArray, parent: Graph) {
        this.verticesMask = verticesMask
        this.parent = parent
        this.size = size
    }

    operator fun contains(vertex: Vertex): Boolean {
        return verticesMask[vertex]
    }

    fun remove(vertex: Vertex) {
        require(verticesMask[vertex])
        verticesMask[vertex] = false
        size--
    }

    fun add(vertex: Vertex) {
        require(!verticesMask[vertex])
        verticesMask[vertex] = true
        size++
    }

    override fun hashCode() = throw UnsupportedOperationException()
    override fun equals(other: Any?): Boolean {
        return other is SubGraph && other.parent == parent && other.size == size && other.verticesMask.contentEquals(verticesMask)
    }

    override fun toString(): String {
        return vertices.joinToString { it.toString() }
    }
}

class SnapshottableSubGraph(
    private val subGraph: SubGraph
) {
    val size: Int get() = subGraph.size
    private val removals = mutableListOf<Vertex>()

    init {
        check(subGraph.size == subGraph.parent.size)
    }

    operator fun contains(vertex: Vertex): Boolean {
        return subGraph.contains(vertex)
    }

    fun remove(vertex: Vertex) {
        removals.add(vertex)
        subGraph.remove(vertex)
    }

    fun snapshot(): GraphSnapshot {
        return GraphSnapshot(removals.size)
    }

    override fun hashCode() = throw UnsupportedOperationException()
    override fun equals(other: Any?) = throw UnsupportedOperationException()

    inner class GraphSnapshot(
        private val removalCount: Int
    ) {
        fun toSubGraph(): SubGraph {
            return subGraph.parent.toSubGraph().also { sg ->
                for (i in 0 until removalCount) {
                    sg.remove(removals[i])
                }
            }
        }
    }

}
