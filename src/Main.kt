fun main() {
    val lpProblem = LpProblem("p1", "p2", "p3") {
        50 * "p1" + 20 * "p2" + 25 * "p3"
    }
    lpProblem.addRestriction {
        (9 * "p1") + (3 * "p2") + (5 * "p3") <= 500
    }
    lpProblem.addRestriction {
        5 * "p1" + 4 * "p2" <= 350
    }
    lpProblem.addRestriction {
        3 * "p1" + 2 * "p3" <= 150
    }
    lpProblem.addRestriction {
        "p3".value <= 20
    }
    println(lpProblem.getMax())
}

