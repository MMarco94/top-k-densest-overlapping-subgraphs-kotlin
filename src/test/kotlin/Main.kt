import java.io.File

data class City(val id: Long)

fun main() {
    val verticesFile = File("src/test/resources/dbbox_Latin-America.locations")
    val edgesFile = File("src/test/resources/dbbox_Latin-America.edges")

    val cities = verticesFile.readLines().map { City(it.split("\\s+".toRegex()).first().toLong()) }.toSet()
    val citiesById = cities.associateBy { it.id }
    val edges = edgesFile.readLines().map {
        val split = it.split("\\s+".toRegex())
        Pair(citiesById.getValue(split[0].toLong()), citiesById.getValue(split[1].toLong()))
    }.toSet()
    val idReassigner = GraphTranslator(cities, edges)
    val graph = idReassigner.graph

    println("Max vertex id = ${cities.map { it.id }.max()}. Vertices count=${cities.size}")

    DOM(graph, 0.25).getDenseOverlappingSubGraphs()
        .take(10)
        .forEach { denseSubGraph ->
            val cities = denseSubGraph.vertices.map { idReassigner.getInitialVertex(it) }
            println(cities.sortedBy { it.id }.joinToString { it.id.toString() })
        }
}