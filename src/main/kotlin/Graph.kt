typealias Vertex = Int

data class Edge(val a: Vertex, val b: Vertex)

interface Graph {
    val vertices: Sequence<Vertex>
    val size: Int
    /**
     * The density of a graph. See pag. 6, definition 1
     */
    val density: Double

    fun intersectionCount(another: Graph): Int
    operator fun contains(vertex: Vertex): Boolean
}

abstract class AbsGraph : Graph {
    abstract override fun equals(other: Any?): Boolean
    abstract override fun hashCode(): Int
}

class BaseGraph(override val size: Int, val edges: Set<Edge>) : AbsGraph() {

    override val vertices = (0 until size).asSequence()
    val degreeCalculator = DegreeCalculator(this)

    override val density: Double get() = edges.size.toDouble() / size
    fun toSubGraph() = SubGraph(this)
    override fun contains(vertex: Vertex) = vertex < size

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

class SubGraph : AbsGraph {
    val verticesMask: BooleanArray
    val parent: BaseGraph
    private val degreeCalculator: Lazy<DegreeCalculator>

    override val vertices get() = parent.vertices.filter { verticesMask[it] }
    override var size: Int
    private var edgesCount: Int
    override val density: Double get() = edgesCount.toDouble() / size

    constructor(parent: BaseGraph) {
        this.verticesMask = BooleanArray(parent.size) { true }
        this.parent = parent
        this.size = parent.size
        this.edgesCount = parent.edges.size
        this.degreeCalculator = lazy(LazyThreadSafetyMode.NONE) { parent.degreeCalculator.clone() }
    }

    private constructor(size: Int, edgesCount: Int, verticesMask: BooleanArray, parent: BaseGraph, degreeCalculator: Lazy<DegreeCalculator>) {
        this.verticesMask = verticesMask
        this.edgesCount = edgesCount
        this.parent = parent
        this.size = size
        this.degreeCalculator = degreeCalculator
    }

    fun degreeOf(vertex: Vertex) = degreeCalculator.value.degreeOf(vertex)
    fun clone() = SubGraph(size, edgesCount, verticesMask.clone(), parent, lazy(LazyThreadSafetyMode.NONE) { degreeCalculator.value.clone() })
    override fun contains(vertex: Vertex): Boolean {
        return verticesMask[vertex]
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

    fun remove(vertex: Vertex) {
        if (verticesMask[vertex]) {
            verticesMask[vertex] = false
            size--
            edgesCount -= degreeOf(vertex)
            degreeCalculator.value.remove(vertex)
            hashCache = -1
        }
    }

    fun add(vertex: Vertex) {
        if (!verticesMask[vertex]) {
            verticesMask[vertex] = true
            size++
            degreeCalculator.value.add(vertex)
            edgesCount += degreeOf(vertex)
            hashCache = -1
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is SubGraph && other.parent == parent && other.verticesMask.contentEquals(verticesMask)
    }

    private var hashCache = -1
    override fun hashCode(): Int {
        if (hashCache == -1) {
            hashCache = verticesMask.hashCode()
        }
        return hashCache
    }
}
