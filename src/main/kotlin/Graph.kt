data class Vertex(val id: Int)
data class Edge(val a: Vertex, val b: Vertex)

interface Graph {
    val vertices: Set<Vertex>
    val size: Int get() = vertices.size
    /**
     * The density of a graph. See pag. 6, definition 1
     */
    val density: Double

    fun subGraph(vertices: Set<Vertex>): Graph
    fun without(v: Vertex): Graph = subGraph(vertices.minus(v))
    fun toImmutable(): Graph

    operator fun contains(vertex: Vertex) = vertex in vertices
}

fun intersectionCount(a: Graph, b: Graph): Double {
    return if (a.size < b.size) {
        a.vertices.count { it in b }
    } else {
        b.vertices.count { it in a }
    }.toDouble()
}

abstract class AbsGraph : Graph {
    override fun equals(other: Any?): Boolean {
        return other is Graph && other.vertices == vertices
    }

    protected var hashCache = -1
    override fun hashCode(): Int {
        if (hashCache == -1) {
            hashCache = vertices.hashCode()
        }
        return hashCache
    }
}

class BaseGraph(val vertexList: List<Vertex>, val edges: Set<Edge>) : AbsGraph() {

    init {
        //Ids should be sequential. Use GraphTranslator to be sure of that
        require(vertexList.indices.toList() == vertexList.map { it.id })
    }

    override val vertices = vertexList.toSet()
    val edgesMap = edges.groupBy { it.a.id } + edges.groupBy { it.b.id }
    val degreeCalculator = DegreeCalculator(this)
    val allWedges: Sequence<Graph>
        get() = edges.asSequence().flatMap { e1 ->
            edges.asSequence().map { e2 ->
                subGraph(setOf(e1.a, e1.b, e2.a, e2.b))
            }
        }.filter { it.vertices.size == 3 }

    override fun subGraph(vertices: Set<Vertex>): Graph = SubGraph(vertices, this)
    override val density: Double get() = edges.size.toDouble() / vertices.size
    override fun toImmutable(): Graph = this
}

open class SubGraph(override val vertices: Set<Vertex>, val parent: BaseGraph) : AbsGraph() {
    protected open val edgesCount get() = parent.edges.count { (a, b) -> a in vertices && b in vertices }
    override val density: Double get() = edgesCount.toDouble() / vertices.size
    override fun subGraph(vertices: Set<Vertex>): Graph = parent.subGraph(vertices)
    override fun toImmutable(): Graph = this
}

class MutableSubGraph(
    parent: BaseGraph,
    private val mutableVertices: MutableSet<Vertex> = parent.vertices.toMutableSet()
) : SubGraph(mutableVertices, parent) {

    override var edgesCount: Int = parent.edges.size
    private val degreeCalculator = parent.degreeCalculator.toMutable()

    fun degreeOf(vertex: Vertex) = degreeCalculator.degreeOf(vertex)

    fun remove(vertex: Vertex) {
        mutableVertices.remove(vertex)
        edgesCount -= degreeCalculator.degreeOf(vertex)
        degreeCalculator.remove(vertex)
        hashCache = -1
    }

    override fun toImmutable(): Graph = SubGraph(mutableVertices.toSet(), parent)
}