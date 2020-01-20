inline fun <T> Collection<T>.forIf(condition: (T) -> Boolean, f: (T) -> Unit) {
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

inline fun Graph.forEachVertex(f: (Vertex) -> Unit) {
    when (this) {
        is BaseGraph -> vertexList.forEach(f)
        is SubGraph -> {
            parent.vertexList.forEachIndexed { index, vertex ->
                if (verticesMask[index]) {
                    f(vertex)
                }
            }
        }
        else -> throw IllegalStateException()
    }
}

inline fun Graph.minVertexBy(f: (Vertex) -> Double): Vertex {
    var min: Vertex? = null
    var minVal = 0.0
    forEachVertex { v ->
        val score = f(v)
        if (min == null || score < minVal) {
            min = v
            minVal = score
        }
    }
    return min!!
}