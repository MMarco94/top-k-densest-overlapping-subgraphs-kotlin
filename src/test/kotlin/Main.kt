import datastructure.BUCKET_OPTIMIZED
import datastructure.FULL_SCAN_WAS_NECESSARY
import java.io.File
import java.time.Duration
import java.time.Instant
import kotlin.system.measureNanoTime

data class City(val id: Long)

fun main2() {
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
    val peeler = DOSComputer(graph, 0.25)
    repeat(10) {
        peeler.peel()
    }
    println("Took ${Duration.between(start, Instant.now())}")


    peeler.subGraphs().forEach { denseSubGraph ->
        val cities = denseSubGraph.vertices.map { idReassigner.getInitialVertex(it) }
        println("${denseSubGraph.size}: " + cities.sortedBy { it.id }.joinToString { it.id.toString() })
    }
}

fun main() {
    val graph = getGoogleGraph()
    println("${graph.size} vertices; ${graph.edges.size} edges; maxDegree is ${graph.maxDegree}")

    val start = Instant.now()
    val peeler = DOSComputer(graph, 5.0)
    repeat(10) {
        val took = measureNanoTime {
            peeler.peel()
        }
        println("Peeling #$it took ${took / 1000000.0}ms (full node scans = $FULL_SCAN_WAS_NECESSARY; bucket optimized=$BUCKET_OPTIMIZED)")
        FULL_SCAN_WAS_NECESSARY = 0
        BUCKET_OPTIMIZED = 0
    }
    println("Took ${Duration.between(start, Instant.now())}")

    peeler.subGraphs().forEach { sg ->
        println("${sg.size}: " + sg.vertices.joinToString { it.toString() })
    }
}

fun getGoogleGraph(): Graph {
    val dataset = "web-Google"
    //val dataset = "K5_s30-0_n2d0_#0"
    val edgesFile = File("src/test/resources/$dataset.txt")
    val pattern = "(\\d+)\\s+(\\d+)".toPattern()

    var max = 0
    val edges: Set<Edge> = edgesFile.readLines().mapNotNullTo(HashSet()) {
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
    }
    return Graph(max + 1, edges)
}