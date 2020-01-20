fun <T> Collection<T>.sumIf(condition: (T) -> Boolean, value: (T) -> Double): Double {
    return sumByDouble {
        if (condition(it)) value(it) else 0.0
    }
}

fun <T> Collection<T>.forIf(condition: (T) -> Boolean, f: (T) -> Unit) {
    forEach {
        if (condition(it)) f(it)
    }
}

operator fun <K, V> Map<K, List<V>>.plus(another: Map<K, List<V>>): Map<K, List<V>> {
    val ret = this.toMutableMap()
    another.forEach { (k, list) ->
        if (k in ret) {
            ret[k] = ret.getValue(k).plus(list)
        } else {
            ret[k] = list
        }
    }
    return ret
}