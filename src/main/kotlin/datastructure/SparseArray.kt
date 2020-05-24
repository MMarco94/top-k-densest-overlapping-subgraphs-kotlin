package datastructure

private const val SMALL_KEY_LIMIT = 100

/**
 * Implementation of Map that accepts positive integers has keys.
 * Keys less than [SMALL_KEY_LIMIT] have an access time O(1)
 */
class SparseArray<T : Any> {

    @Suppress("UNCHECKED_CAST")
    private val smallValues = arrayOfNulls<Any>(SMALL_KEY_LIMIT) as Array<T?>
    private val largeValues = HashMap<Int, T>()

    fun get(key: Int): T? {
        return if (key < SMALL_KEY_LIMIT) {
            smallValues[key]
        } else {
            largeValues[key]
        }
    }

    fun put(key: Int, value: T) {
        if (key < SMALL_KEY_LIMIT) {
            smallValues[key] = value
        } else {
            largeValues[key] = value
        }
    }

    fun remove(key: Int) {
        if (key < SMALL_KEY_LIMIT) {
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