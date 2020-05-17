import java.io.File
import java.time.Duration
import java.time.Instant

data class City(val id: Long)

fun mainWithtranslator() {
    val dataset = "dbbox_Latin-America"
    //val dataset = "K5_s30-0_n2d0_#0"
    val verticesFile = File("src/test/resources/$dataset.labels")
    val edgesFile = File("src/test/resources/$dataset.edgelist")
    val pattern = "(\\d+)\\s+(\\d+)".toPattern()

    val cities = verticesFile.readLines().map { City(it.split("\\s+".toRegex()).first().toLong()) }.toSet()
    val citiesById = cities.associateBy { it.id }
    val edges = edgesFile.readLines().map {
        val matcher = pattern.matcher(it)
        if (!matcher.matches()) {
            throw IllegalArgumentException("Invalid line '$it'")
        }
        Pair(citiesById.getValue(matcher.group(1).toLong()), citiesById.getValue(matcher.group(2).toLong()))
    }.toSet()
    val idReassigner = GraphTranslator(cities, edges)
    val graph = idReassigner.graph

    println("${cities.size} vertices; ${edges.size} edges")

    val start = Instant.now()
    val subGraphs = DOS(graph, 0.25).getDenseOverlappingSubGraphs().elementAt(10)
    val took = Duration.between(start, Instant.now())
    println("Creating sub-graphs took $took")

    subGraphs.subGraphs.forEach { denseSubGraph ->
        val cities = denseSubGraph.vertices.map { idReassigner.getInitialVertex(it) }
        println("${denseSubGraph.size}: " + cities.sortedBy { it.id }.joinToString { it.id.toString() })
    }
}

fun main() {
    val dataset = "web-Google"
    //val dataset = "K5_s30-0_n2d0_#0"
    val edgesFile = File("src/test/resources/$dataset.txt")
    val pattern = "(\\d+)\\s+(\\d+)".toPattern()

    var max = 0
    val edges = edgesFile.readLines().mapNotNull {
        if (it.firstOrNull() == '#') null
        else {
            val matcher = pattern.matcher(it)
            if (!matcher.matches()) {
                throw IllegalArgumentException("Invalid line '$it'")
            }
            val e1 = matcher.group(1).toInt()
            val e2 = matcher.group(2).toInt()
            max = maxOf(max, e1, e2)
            Edge(e1, e2)
        }
    }.toSet()
    val graph = Graph(max + 1, edges)
    println("${graph.size} vertices; ${edges.size} edges")

    val k = 10
    val subGraphs = DOS(graph, 0.25).getDenseOverlappingSubGraphs().drop(1).take(k)

    subGraphs.forEach {
        val denseSubGraph = it.subGraphs.last()
        val nodes = denseSubGraph.vertices
        println("${denseSubGraph.size}: " + nodes.joinToString { it.toString() })
    }
}