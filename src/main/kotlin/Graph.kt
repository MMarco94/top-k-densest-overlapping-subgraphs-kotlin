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

interface Graph {
    val vertices: Sequence<Vertex>
    val size: Int
    operator fun contains(vertex: Vertex): Boolean
}

abstract class AbsGraph : Graph {
    abstract override fun equals(other: Any?): Boolean
    override fun hashCode(): Int = size
}

class BaseGraph(override val size: Int, val edges: Set<Edge>) : AbsGraph() {
    override val vertices = (0 until size).asSequence()
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
    private fun subGraph(vertices: Set<Vertex>) = SubGraph(
        vertices.size,
        BooleanArray(vertices.size) { it in vertices },
        this
    )

    override fun contains(vertex: Vertex) = vertex < size

    override fun equals(other: Any?): Boolean {
        return other === this
    }
}

class SubGraph : AbsGraph {
    val parent: BaseGraph

    val verticesMask: BooleanArray
    override val vertices get() = parent.vertices.filter { verticesMask[it] }
    override var size: Int

    constructor(parent: BaseGraph) {
        this.verticesMask = BooleanArray(parent.size) { true }
        this.parent = parent
        this.size = parent.size
    }

    constructor(size: Int, verticesMask: BooleanArray, parent: BaseGraph) {
        this.verticesMask = verticesMask
        this.parent = parent
        this.size = size
    }

    fun clone() = SubGraph(size, verticesMask.clone(), parent)
    override fun contains(vertex: Vertex): Boolean {
        return verticesMask[vertex]
    }

    fun remove(vertex: Vertex) {
        if (verticesMask[vertex]) {
            verticesMask[vertex] = false
            size--
        } else throw IllegalStateException()
    }

    fun add(vertex: Vertex) {
        if (!verticesMask[vertex]) {
            verticesMask[vertex] = true
            size++
        } else throw IllegalStateException()
    }

    override fun equals(other: Any?): Boolean {
        return other is SubGraph && other.parent == parent && other.size == size && other.verticesMask.contentEquals(verticesMask)
    }
}
