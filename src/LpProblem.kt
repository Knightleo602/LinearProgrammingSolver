private typealias Comparison = (Float, Float) -> Boolean
typealias Restriction<K> = LpProblemScope<K>.() -> Boolean
typealias ObjectiveFunction<K> = LpProblemScope<K>.() -> Float

class LpProblemScope<K>(
    private val variablesData: Map<K, Int>
) {
    operator fun Float.times(variable: K): Float =
        variable.value * this
    operator fun Int.times(variable: K): Float =
        variable.value * this
    val K.value: Float get() = variablesData[this]?.toFloat() ?: 0f
}

data class LpResult<K>(
    val optimalValue: Float,
    val variables: Map<K, Int>
) {
    override fun toString(): String =
        "Question Result: optimal value is $optimalValue with variables ${variables.entries}"
}

/**
 * The builder class of the problem to be solved, it initially requires a set of variables and the
 * objective function.
 *
 * The method of solving the problem, is by brute forcing all possibilities and finding out which
 * one had the best [objectiveFunction].
 *
 * Once you've added all the restrictions using [addRestriction] or [addRestrictions], call either
 * [getMax] or [getMin] to compute the solution.
 *
 * @param variables The set of variables that the problem has, this can be any object
 * @param objectiveFunction The objective function of the problem
 * @param nonNegativityRestriction If non negativity restrictions should be added automatically to all variables
 * (Default: true)
 */
class LpProblem<K>(
    vararg variables: K,
    nonNegativityRestriction: Boolean = true,
    private val objectiveFunction: ObjectiveFunction<K>,
) {
    private val variablesSet: Set<K> = variables.toSet()
    private val restrictions: MutableList<Restriction<K>> = mutableListOf()

    init {
        if(nonNegativityRestriction) {
            for(v in variablesSet) addRestriction {
                v.value >= 0
            }
        }
    }

    fun getMax(): LpResult<K> =
        bruteForce { first, second ->
            first < second
        }

    fun getMin(): LpResult<K> =
        bruteForce { first, second ->
            first > second
        }

    private inline fun bruteForce(limit: Int = 100, crossinline compare: Comparison): LpResult<K> {
        var best: LpResult<K> = LpResult(0f, hashMapOf())
        variablesSet.toList().iterateEveryCombination(limit = limit) {
            if(checkAll(it)) {
                val optimal = objectiveFunction(LpProblemScope(it))
                if(compare(best.optimalValue, optimal))
                    best = LpResult(optimal, it.toMap())
            }
        }
        return best
    }

    private fun checkAll(variables: Map<K, Int>): Boolean {
        for(r in restrictions) if(!r(LpProblemScope(variables))) return false
        return true
    }

    private fun List<K>.iterateEveryCombination(
        limit: Int = 100,
        currentIndex: Int = 0,
        trackedValues: MutableMap<K, Int> = variablesSet.associateWith { 0 }.toMutableMap(),
        block: (MutableMap<K, Int>) -> Unit
    ) {
        if(currentIndex >= size) return
        val v = get(currentIndex)
        if(currentIndex + 1 < size)
            for(l in 0..limit) {
                trackedValues[v] = l
                iterateEveryCombination(limit, currentIndex+1, trackedValues, block)
            }
        else
            for(l in 0..limit) {
                trackedValues[v] = l
                block(trackedValues)
            }
    }

    fun addRestrictions(vararg restriction: Restriction<K>) {
        restrictions.addAll(restriction)
    }
    fun addRestriction(restriction: Restriction<K>) = restrictions.add(restriction)
}