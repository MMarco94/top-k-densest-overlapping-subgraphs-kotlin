class GraphTranslator<T>(vertices: Set<T>, edges: Set<Pair<T, T>>) {
    private val outToIn = vertices.toList()
    private val outVertices = outToIn.indices.map { Vertex(it) }
    private val inToOut = vertices.withIndex().associate { it.value to outVertices[it.index] }
    private val outEdges: Set<Edge> = edges.mapTo(HashSet(edges.size)) { (a, b) -> Edge(inToOut.getValue(a), inToOut.getValue(b)) }
    val graph = BaseGraph(outVertices, outEdges.toSet())

    fun getInitialVertex(vertex: Vertex) = outToIn[vertex.id]
}