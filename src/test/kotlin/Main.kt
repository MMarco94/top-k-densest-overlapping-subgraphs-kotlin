import java.io.File
import java.time.Duration
import java.time.Instant
import java.util.regex.Pattern

data class City(val id: Long)

fun main() {
    val dataset = "../dbbox_Latin-America"
    //val dataset = "K5_s30-0_n2d0_#0"
    val verticesFile = File("src/test/resources/synth/$dataset.labels")
    val edgesFile = File("src/test/resources/synth/$dataset.edgelist")
    val pattern = "(\\d+)\\s+(\\d+)".toPattern()

    val cities = verticesFile.readLines().map { City(it.split("\\s+".toRegex()).first().toLong()) }.toSet()
    val citiesById = cities.associateBy { it.id }
    val edges = edgesFile.readLines().map {
        val matcher = pattern.matcher(it)
        if(!matcher.matches()){
            throw IllegalArgumentException("Invalid line '$it'")
        }
        Pair(citiesById.getValue(matcher.group(1).toLong()), citiesById.getValue(matcher.group(2).toLong()))
    }.toSet()
    val idReassigner = GraphTranslator(cities, edges)
    val graph = idReassigner.graph

    println("${cities.size} vertices; ${edges.size} edges")

    val start = Instant.now()
    val subGraphs = DOS(graph, 0.25).getDenseOverlappingSubGraphs().take(10).toList()
    val took = Duration.between(start, Instant.now())
    println("Creating sub-graphs took $took")

    subGraphs.forEach { denseSubGraph ->
        val cities = denseSubGraph.vertices.map { idReassigner.getInitialVertex(it) }
        println("${denseSubGraph.size}: " + cities.sortedBy { it.id }.joinToString { it.id.toString() })
    }
}