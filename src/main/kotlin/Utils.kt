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

inline fun BaseGraph.forEachVertex(f: (vertexId: Int) -> Unit) {
    for (i in 0 until size) {
        f(i)
    }
}

inline fun Graph.forEachVertex(f: (vertexId: Int) -> Unit) {
    when (this) {
        is BaseGraph -> forEachVertex(f)
        is SubGraph -> {
            parent.forEachVertex { vertex ->
                if (verticesMask[vertex]) {
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