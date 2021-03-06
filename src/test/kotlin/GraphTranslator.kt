class GraphTranslator<T>(vertices: Set<T>, edges: Set<Pair<T, T>>) {
    private val outToIn = vertices.toList()
    private val inToOut = outToIn.withIndex().associate { it.value to it.index }
    private val outEdges: Set<Edge> = edges.mapTo(HashSet(edges.size)) { (a, b) -> Edge(inToOut.getValue(a), inToOut.getValue(b)) }
    val graph = Graph(vertices.size, outEdges.toSet())

    fun getInitialVertex(vertex: Vertex) = outToIn[vertex]
}