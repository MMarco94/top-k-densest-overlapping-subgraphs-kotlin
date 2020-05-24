package datastructure

import java.util.*
import kotlin.collections.HashMap

class VerticesByDegreeQueue(private val nodes: Array<VerticesLinkedList.Node>) {
    private class DegreeBucket(val degree: Int) : Comparable<DegreeBucket> {
        val vertices = VerticesLinkedList()

        override fun compareTo(other: DegreeBucket): Int {
            return degree.compareTo(other.degree)
        }
    }

    private val queue = PriorityQueue<DegreeBucket>()
    private val buckets = HashMap<Int, DegreeBucket>()

    fun add(node: VerticesLinkedList.Node) {
        val degree = node.degree
        val bucket = buckets.getOrPut(degree) {
            DegreeBucket(degree).also { db ->
                queue.add(db)
            }
        }
        if (node.list != null) {
            node.remove()
        }
        node.add(this, bucket.vertices)
    }

    fun isEmpty(): Boolean {
        return firstNonEmptyBucket() == null
    }

    fun isNotEmpty(): Boolean {
        return !isEmpty()
    }

    fun min(): VerticesLinkedList.Node {
        return firstNonEmptyBucket()!!.vertices.first()
    }

    fun removeMin(): VerticesLinkedList.Node {
        return firstNonEmptyBucket()!!.vertices.first().also { node ->
            node.remove()
        }
    }

    private fun firstNonEmptyBucket(): DegreeBucket? {
        while (true) {
            val firstBucket = queue.peek()
            if (firstBucket != null && firstBucket.vertices.isEmpty()) {
                buckets.remove(firstBucket.degree)
                queue.remove()
            } else {
                return firstBucket
            }
        }
    }

    fun changeDegree(node: VerticesLinkedList.Node) {
        node.remove()
        add(node)
    }
}