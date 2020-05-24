package datastructure


/**
 * Implementation of Map that accepts as keys non-negative integers less than limit.
 */
class SparseArray<T : Any>(limit: Int) {

    @Suppress("UNCHECKED_CAST")
    private val smallValues = arrayOfNulls<Any>(limit + 1) as Array<T?>

    fun get(key: Int): T? {
        return smallValues[key]
    }

    fun put(key: Int, value: T) {
        smallValues[key] = value
    }

    fun remove(key: Int) {
        smallValues[key] = null
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