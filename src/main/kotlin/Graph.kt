typealias Vertex = Int

data class Edge(val a: Vertex, val b: Vertex) {
    fun otherVertex(v: Vertex): Vertex {
        return when (v) {
            a -> b
            b -> a
            else -> throw IllegalArgumentException()
        }
    }
}

class Graph(val size: Int, val edges: Set<Edge>) {
    val vertices = (0 until size).asSequence()
    val edgesMap: Array<MutableList<Edge>> = Array(size) { mutableListOf<Edge>() }.also { arr ->
        edges.forEach { e ->
            arr[e.a].add(e)
            arr[e.b].add(e)
        }
    }
    val allWedges = sequence {
        edgesMap.forEachIndexed { v1, edges ->
            edges.forEach { e1 ->
                val v2 = e1.otherVertex(v1)
                edgesMap[v2].forEach { e2 ->
                    val v3 = e2.otherVertex(v2)
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

    fun clone(): SubGraph {
        return SubGraph(size, verticesMask.clone(), parent)
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

    override fun equals(other: Any?): Boolean {
        return other is SubGraph && other.parent == parent && other.size == size && other.verticesMask.contentEquals(verticesMask)
    }

    override fun toString(): String {
        return vertices.joinToString { it.toString() }
    }
}
