package datastructure

import Graph
import Vertex

class VerticesLinkedList {

    private var head: Node? = null

    fun isEmpty() = head == null

    fun first(): Node = head!!

    fun min(): Node {
        var ret = head!!
        var iter = ret.next
        while (iter != null) {
            if (iter.degree < ret.degree) {
                ret = iter
            }
            iter = iter.next
        }
        return ret
    }

    class Node(
        var vertex: Vertex,
        var prev: Node? = null,
        var next: Node? = null,
        var degree: Int = 0,
        var queue: VerticesByDegreeQueue? = null,
        var list: VerticesLinkedList? = null
    ) {

        fun remove() {
            val l = list!!
            val p = prev
            val n = next

            p?.next = n
            n?.prev = p
            if (l.head === this) {
                l.head = n
            }

            prev = null
            next = null
            list = null
            queue = null
        }

        fun add(queue: VerticesByDegreeQueue, list: VerticesLinkedList) {
            check(this.queue == null)
            check(this.list == null)
            check(next == null)
            check(prev == null)
            val prevHead = list.head
            next = prevHead
            prevHead?.prev = this
            list.head = this
            this.list = list
            this.queue = queue
        }

        fun resetDegree(graph: Graph) {
            changeDegree(graph.edgesMap[vertex].size)
        }

        fun changeDegree(newDegree: Int) {
            val oldDegree = degree
            if (newDegree != oldDegree) {
                check(newDegree >= 0)
                degree = newDegree
                queue?.changeDegree(this, oldDegree, newDegree)
            }
        }
    }
}