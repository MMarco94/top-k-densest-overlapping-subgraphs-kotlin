abstract class MinHeap(
    private val size: Int
) {
    private val heap = IntArray(size) { it }
    private val reverseIndexes = IntArray(size) { it }

    protected fun minHeapify() {
        for (pos in size / 2 downTo 0) {
            minHeapify(pos)
        }
    }

    abstract fun getWeight(pos: Int): Double

    fun head(): Int {
        return heap[0]
    }

    private fun isGt(a: Int, b: Int): Boolean {
        return getWeight(heap[a]) > getWeight(heap[b])
    }

    protected fun notifyComparisonChanged(elem: Int) {
        val pos = reverseIndexes[elem]
        minHeapify(pos)
        bubbleUp(pos)
    }

    private fun parent(pos: Int): Int {
        return (pos + 1) / 2 - 1
    }

    private fun leftChild(pos: Int): Int {
        return 2 * pos + 1
    }

    private fun rightChild(pos: Int): Int {
        return 2 * pos + 2
    }

    private fun swap(a: Int, b: Int) {
        swapReverse(heap[a], heap[b])
        val tmp = heap[a]
        heap[a] = heap[b]
        heap[b] = tmp
    }

    private fun swapReverse(a: Int, b: Int) {
        val tmp = reverseIndexes[a]
        reverseIndexes[a] = reverseIndexes[b]
        reverseIndexes[b] = tmp
    }


    private fun bubbleUp(pos: Int) {
        var current = pos
        while (current > 0 && isGt(parent(current), current)) {
            swap(current, parent(current))
            current = parent(current)
        }
    }

    private fun minHeapify(pos: Int) {
        val leftChild = leftChild(pos)
        val rightChild = rightChild(pos)
        val hasLeftChild = leftChild < size
        val hasRightChild = rightChild < size
        if ((hasLeftChild && isGt(pos, leftChild)) || (hasRightChild && isGt(pos, rightChild))) {
            if (hasRightChild && isGt(leftChild, rightChild)) {
                swap(pos, rightChild)
                minHeapify(rightChild)
            } else if (hasLeftChild) {
                swap(pos, leftChild)
                minHeapify(leftChild)
            } else throw IllegalStateException()
        }
    }
}
