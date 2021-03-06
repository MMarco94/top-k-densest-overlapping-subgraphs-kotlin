inline fun <T> List<T>.forIf(condition: (T) -> Boolean, f: (T, index: Int) -> Unit) {
    noIteratorForEachIndexed { index, t ->
        if (condition(t)) f(t, index)
    }
}

inline fun <T> List<T>.noIteratorForEach(f: (T) -> Unit) {
    for(i in indices){
        f(get(i))
    }
}

inline fun <T> List<T>.noIteratorForEachIndexed(f: (Int, T) -> Unit) {
    for(i in indices){
        f(i, get(i))
    }
}

inline fun <T> List<T>.sumByDoubleIndexed(f: (T, index: Int) -> Double): Double {
    var sum: Double = 0.0
    for (i in indices) {
        sum += f(get(i), i)
    }
    return sum
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

inline fun Graph.forEachVertex(f: (vertexId: Int) -> Unit) {
    for (i in 0 until size) {
        f(i)
    }
}

inline fun SubGraph.forEachVertex(f: (vertexId: Int) -> Unit) {
    parent.forEachVertex { vertex ->
        if (verticesMask[vertex]) {
            f(vertex)
        }
    }
}
