import datastructure.VerticesByDegreeQueue
import datastructure.VerticesLinkedList
import kotlin.test.Test
import kotlin.test.assertEquals

class VerticesByDegreeQueueTest {

    @Test
    fun test() {
        val gg = getGoogleGraph()
        val nodes = Array(gg.size) {
            VerticesLinkedList.Node(it)
        }
        val vertices = VerticesByDegreeQueue(gg.maxDegree)
        nodes.forEach { n ->
            n.resetDegree(gg)
            vertices.add(n)
        }
        val extracted = gg.toEmptySubGraph()
        var prev: VerticesLinkedList.Node? = null
        while (vertices.isNotEmpty()) {
            val min = vertices.removeMin()
            if (prev != null) {
                assert(min.degree >= prev.degree)
            }
            assert(!extracted.contains(min.vertex))
            extracted.add(min.vertex)
            prev = min
        }
        assertEquals(gg.size, extracted.size)
    }
}