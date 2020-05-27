package datastructure

import java.util.*

class VerticesByDegreeQueue(maxDegree: Int) {
    private class DegreeBucket(var degree: Int) : Comparable<DegreeBucket> {

        val vertices = VerticesLinkedList()

        override fun compareTo(other: DegreeBucket): Int {
            return degree.compareTo(other.degree)
        }
    }

    private val queue = PriorityQueue<DegreeBucket>()
    private val buckets = SparseArray<DegreeBucket>(maxDegree)

    fun add(node: VerticesLinkedList.Node) {
        val degree = node.degree
        val bucket = getOrCreateBucket(degree)
        node.add(this, bucket.vertices)
    }

    private fun getOrCreateBucket(degree: Int): DegreeBucket {
        return buckets.getOrPut(degree) {
            DegreeBucket(degree).also { db ->
                queue.add(db)
            }
        }
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
        return firstNonEmptyBucket()!!.vertices.removeFirst()
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

    fun changeDegree(node: VerticesLinkedList.Node, oldDegree: Int, newDegree: Int) {
        if (newDegree == oldDegree - 1 && node.next == null && node.prev == null && !buckets.contains(newDegree)) {
            //Optimizing the case in which the degree decreases by 1: most of the times
            //I can change the bucket instead of removing/adding the node.
            //In this case, the priority queue doesn't need to be updated, since the position in the heap will remain the same
            val oldBucket = buckets.get(oldDegree)!!
            buckets.remove(oldDegree)
            oldBucket.degree = newDegree
            buckets.put(newDegree, oldBucket)
        } else {
            node.remove()
            add(node)
        }
    }
}