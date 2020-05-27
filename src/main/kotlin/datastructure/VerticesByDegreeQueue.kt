package datastructure

import java.util.*
import kotlin.math.min

private const val DEGREE_THRESHOLD = 100

var FULL_SCAN_WAS_NECESSARY = 0
var BUCKET_OPTIMIZED = 0

class VerticesByDegreeQueue(maxDegree: Int) {
    private class DegreeBucket(var degree: Int) : Comparable<DegreeBucket> {

        val vertices = VerticesLinkedList()

        override fun compareTo(other: DegreeBucket): Int {
            return degree.compareTo(other.degree)
        }
    }

    private val queue = PriorityQueue<DegreeBucket>()
    private val buckets = SparseArray<DegreeBucket>(min(maxDegree, DEGREE_THRESHOLD))

    //Contains nodes with degree >= DEGREE_THRESHOLD
    private val otherNodes = VerticesLinkedList()

    fun add(node: VerticesLinkedList.Node) {
        node.add(this, getListForDegree(node.degree))
    }

    private fun getListForDegree(degree: Int): VerticesLinkedList {
        return if (degree < DEGREE_THRESHOLD) {
            val bucket = getOrCreateBucket(degree)
            bucket.vertices
        } else {
            otherNodes
        }
    }

    private fun getOrCreateBucket(degree: Int): DegreeBucket {
        return buckets.getOrPut(degree) {
            DegreeBucket(degree).also { db ->
                queue.add(db)
            }
        }
    }

    fun isEmpty(): Boolean {
        return firstNonEmptyBucket() == null && otherNodes.isEmpty()
    }

    fun isNotEmpty(): Boolean {
        return !isEmpty()
    }

    fun min(): VerticesLinkedList.Node {
        //Can call peek since if someone calls this method it means
        //that it checked for isEmpty, so the queue will already be organized
        val firstNonEmptyBucket = queue.peek()
        return if (firstNonEmptyBucket == null) {
            FULL_SCAN_WAS_NECESSARY++
            otherNodes.min()
        } else {
            firstNonEmptyBucket.vertices.first()
        }
    }

    fun removeMin(): VerticesLinkedList.Node {
        return min().also { it.remove() }
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

    fun changeDegree(node: VerticesLinkedList.Node, newDegree: Int) {
        val new = getListForDegree(newDegree)
        if (node.list != new) {
            node.move(new)
        }
    }
}