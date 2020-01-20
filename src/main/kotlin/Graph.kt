data class Vertex(val id: Int)
data class Edge(val a: Vertex, val b: Vertex)

interface Graph {
    val vertices: Sequence<Vertex>
    val size: Int
    /**
     * The density of a graph. See pag. 6, definition 1
     */
    val density: Double

    fun subGraph(vertices: Set<Vertex>): Graph
    fun toImmutable(): Graph
    fun intersectionCount(another: Graph): Int
    operator fun contains(vertex: Vertex): Boolean
}

abstract class AbsGraph : Graph {
    abstract override fun equals(other: Any?): Boolean
    abstract override fun hashCode(): Int
}

class BaseGraph(val vertexList: Array<Vertex>, val edges: Set<Edge>) : AbsGraph() {

    init {
        //Ids should be sequential. Use GraphTranslator to be sure of that
        require(vertexList.indices.toList() == vertexList.map { it.id })
    }

    override val size: Int get() = vertexList.size
    override val vertices = vertexList.asSequence()
    val edgesMap = Array(size) { mutableListOf<Edge>() }.also { arr ->
        edges.forEach { e ->
            arr[e.a.id].add(e)
            arr[e.b.id].add(e)
        }
    }
    val degreeCalculator = DegreeCalculator(this)
    val allWedges: Sequence<Graph>
        get() = edges.asSequence().flatMap { e1 ->
            edges.asSequence().mapNotNull { e2 ->
                val vertices = setOf(e1.a, e1.b, e2.a, e2.b)
                if (vertices.size == 3) {
                    subGraph(vertices)
                } else null
            }
        }

    override fun subGraph(vertices: Set<Vertex>): Graph {
        val mask = BooleanArray(this.size) { vertices.contains(vertexList[it]) }
        return SubGraph(vertices.size, mask, this)
    }

    override val density: Double get() = edges.size.toDouble() / size
    override fun toImmutable(): Graph = this
    fun toMutable() = MutableSubGraph(size, BooleanArray(size) { true }, this)
    override fun contains(vertex: Vertex) = vertex.id < size

    override fun equals(other: Any?): Boolean {
        return other === this
    }

    override fun hashCode(): Int = 0
    override fun intersectionCount(another: Graph): Int {
        if (another is SubGraph && another.parent == this) {
            return another.size
        } else throw UnsupportedOperationException("Complicated and not needed")
    }

}

open class SubGraph(
    size: Int,
    val verticesMask: BooleanArray,
    val parent: BaseGraph
) : AbsGraph() {
    override val vertices get() = parent.vertices.filter { verticesMask[it.id] }
    override var size: Int = size
        protected set
    protected open val edgesCount get() = parent.edges.count { (a, b) -> a in this && b in this }
    override val density: Double get() = edgesCount.toDouble() / size
    override fun subGraph(vertices: Set<Vertex>): Graph = parent.subGraph(vertices)
    override fun toImmutable(): Graph = this
    override fun contains(vertex: Vertex): Boolean {
        return verticesMask[vertex.id]
    }

    override fun equals(other: Any?): Boolean {
        return other is SubGraph && other.verticesMask.contentEquals(verticesMask)
    }

    protected var hashCache = -1
    override fun hashCode(): Int {
        if (hashCache == -1) {
            hashCache = verticesMask.hashCode()
        }
        return hashCache
    }

    override fun intersectionCount(another: Graph): Int {
        return when {
            another == parent -> size
            another is SubGraph && another.parent == parent -> {
                var sum = 0
                verticesMask.forEachIndexed { index, b ->
                    if (b && another.verticesMask[index]) {
                        sum++
                    }
                }
                sum
            }
            else -> throw UnsupportedOperationException("Complicated and not needed")
        }
    }
}

class MutableSubGraph(
    size: Int,
    verticesMask: BooleanArray,
    parent: BaseGraph
) : SubGraph(size, verticesMask, parent) {

    override var edgesCount: Int = parent.edges.size
    private val degreeCalculator = parent.degreeCalculator.clone()

    fun degreeOf(vertex: Vertex) = degreeCalculator.degreeOf(vertex)

    fun remove(vertex: Vertex) {
        if (verticesMask[vertex.id]) {
            verticesMask[vertex.id] = false
            size--
            edgesCount -= degreeCalculator.degreeOf(vertex)
            degreeCalculator.remove(vertex)
            hashCache = -1
        }
    }

    override fun toImmutable(): Graph = SubGraph(size, verticesMask.clone(), parent)
}