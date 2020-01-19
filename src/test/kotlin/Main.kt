import java.io.File

fun main() {
    val verticesFile = File("src/test/resources/dbbox_Latin-America.locations")
    val edgesFile = File("src/test/resources/dbbox_Latin-America.edges")

    val vertices = verticesFile.readLines().map { Vertex(it.split("\\s+".toRegex()).first().toLong()) }.toSet()
    val vertexMap = vertices.associateBy { it.id }
    val edges = edgesFile.readLines().map {
        val split = it.split("\\s+".toRegex())
        Edge(vertexMap.getValue(split[0].toLong()), vertexMap.getValue(split[1].toLong()))
    }.toSet()
    val graph = BaseGraph(vertices, edges)

    DOM(graph, 0.25).getDenseOverlappingSubGraphs()
        .take(10)
        .forEach { denseSubGraph ->
            println(denseSubGraph.vertices.sortedBy { it.id }.joinToString { it.id.toString() })
        }
}