package datastructure


/**
 * Implementation of Map that accepts as keys non-negative integers less than limit.
 */
class SparseArray<T : Any>(limit: Int) {

    @Suppress("UNCHECKED_CAST")
    private val values = arrayOfNulls<Any>(limit + 1) as Array<T?>

    fun get(key: Int): T? {
        return values[key]
    }

    fun put(key: Int, value: T) {
        values[key] = value
    }

    fun remove(key: Int) {
        values[key] = null
    }

    fun contains(key: Int): Boolean {
        return values[key] != null
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