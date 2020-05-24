package datastructure


/**
 * Implementation of Map that accepts positive integers has keys.
 * Keys less than [smallKeysLimit] have an access time O(1)
 */
class SparseArray<T : Any>(val smallKeysLimit: Int) {

    @Suppress("UNCHECKED_CAST")
    private val smallValues = arrayOfNulls<Any>(smallKeysLimit) as Array<T?>
    private val largeValues = HashMap<Int, T>()

    fun get(key: Int): T? {
        return if (key < smallKeysLimit) {
            smallValues[key]
        } else {
            largeValues[key]
        }
    }

    fun put(key: Int, value: T) {
        if (key < smallKeysLimit) {
            smallValues[key] = value
        } else {
            largeValues[key] = value
        }
    }

    fun remove(key: Int) {
        if (key < smallKeysLimit) {
            smallValues[key] = null
        } else {
            largeValues.remove(key)
        }
    }

    inline fun getOrPut(key: Int, defaultValue: () -> T): T {
        val value = get(key)
        return if (value == null) {
            val answer = defaultValue()
            put(key, answer)
            answer
        } else {
            value
        }
    }
}