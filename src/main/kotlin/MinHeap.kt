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
        val ha = heap[a]
        val hb = heap[b]
        val tmp = reverseIndexes[ha]
        reverseIndexes[ha] = reverseIndexes[hb]
        reverseIndexes[hb] = tmp
        heap[a] = hb
        heap[b] = ha
    }

    private fun bubbleUp(pos: Int) {
        var current = pos
        val currentWeight = getWeight(heap[pos])
        while (current > 0 && getWeight(heap[parent(current)]) > currentWeight) {
            swap(current, parent(current))
            current = parent(current)
        }
    }

    private fun minHeapify(pos: Int, posWeight: Double = getWeight(heap[pos])) {
        val leftChild = leftChild(pos)
        val rightChild = rightChild(pos)
        val hasLeftChild = leftChild < size
        val hasRightChild = rightChild < size

        val leftWeight = if (!hasLeftChild) -1.0 else getWeight(heap[leftChild])
        val rightWeight = if (!hasRightChild) -1.0 else getWeight(heap[rightChild])
        if ((hasLeftChild && posWeight > leftWeight) || (hasRightChild && posWeight > rightWeight)) {

            if (hasRightChild && leftWeight > rightWeight) {
                swap(pos, rightChild)
                minHeapify(rightChild, posWeight)
            } else if (hasLeftChild) {
                swap(pos, leftChild)
                minHeapify(leftChild, posWeight)
            } else throw IllegalStateException()
        }
    }
}
