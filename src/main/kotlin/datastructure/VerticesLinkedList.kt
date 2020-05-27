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
            //Saving this variable since the compiler cannot know that
            //changing the order of this instruction is side effect free
            val isHead = l.head === this
            val p = prev
            val n = next

            p?.next = n
            n?.prev = p

            prev = null
            next = null
            list = null
            queue = null

            if (isHead) {
                l.head = n
            }
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

        /**
         * Alternative to calling [remove] and [add].
         * This is slightly (but measurably) faster than doing so.
         */
        fun move(newList: VerticesLinkedList) {
            val oldL = this.list!!
            val oldN = next

            if (oldL.head === this) {
                oldL.head = oldN
                oldN?.prev = null
            } else {
                val oldP = prev
                oldP?.next = oldN
                oldN?.prev = oldP
                prev = null
            }

            val newHead = newList.head
            next = newHead
            newHead?.prev = this
            newList.head = this
            this.list = newList
        }

        fun resetDegree(graph: Graph) {
            changeDegree(graph.connectionsMap[vertex].size)
        }

        fun changeDegree(newDegree: Int) {
            val oldDegree = degree
            if (newDegree != oldDegree) {
                check(newDegree >= 0)
                degree = newDegree
                queue?.changeDegree(this, newDegree)
            }
        }
    }
}