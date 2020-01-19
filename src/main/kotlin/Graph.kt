data class Vertex(val id: Long)
data class Edge(val a: Vertex, val b: Vertex)

interface Graph {
    val vertices: Set<Vertex>
    val edgeSequence: Sequence<Edge>
    val size: Int get() = vertices.size
    /**
     * The density of a graph. See pag. 6, definition 1
     */
    val density get() = edgeSequence.count().toDouble() / vertices.size

    fun subGraph(vertices: Set<Vertex>): Graph
    fun minus(v: Vertex): Graph = subGraph(vertices.minus(v))
    fun plus(v: Vertex): Graph = subGraph(vertices.plus(v))
    fun degreeOf(vertex: Vertex): Int

    val allWedges: Sequence<Graph>
        get() = edgeSequence.flatMap { e1 ->
            edgeSequence.map { e2 ->
                subGraph(setOf(e1.a, e1.b, e2.a, e2.b))
            }
        }.filter { it.vertices.size == 3 }
}

fun intersectionCount(a: Graph, b: Graph): Double {
    return a.vertices.count { it in b.vertices }.toDouble()//TODO: iterate on the smaller set
}

data class BaseGraph(override val vertices: Set<Vertex>, val edges: Set<Edge>) : Graph {
    override val edgeSequence = edges.asSequence()
    val edgesForVertex = edges.groupBy { it.a } + edges.groupBy { it.b }

    override fun subGraph(vertices: Set<Vertex>): Graph = SubGraph(vertices, this)
    override val density: Double get() = edges.size.toDouble() / vertices.size

    override fun degreeOf(vertex: Vertex): Int {
        return edgesForVertex[vertex]?.size ?: 0
    }
}

data class SubGraph(override val vertices: Set<Vertex>, val parent: BaseGraph) : Graph {
    override val edgeSequence = parent.edgeSequence.filter { (a, b) -> a in vertices && b in vertices }
    override fun degreeOf(vertex: Vertex): Int {
        return parent.edgesForVertex[vertex]
            ?.count { (a, b) -> (a == vertex || a in vertices) && (b == vertex || b in vertices) }
            ?: 0
    }

    override fun subGraph(vertices: Set<Vertex>): Graph = parent.subGraph(vertices)
}