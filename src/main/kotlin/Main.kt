package calculator

import java.math.BigInteger

val variables = mutableMapOf<String, BigInteger>()

class Calculator(private var uiOrig: String) {
    init {
        if ("^(\\d+[a-zA-Z]+|[a-zA-Z]+\\d+)".toRegex().find(uiOrig) != null) {
            println("Invalid identifier")
        } else if ("(\\d+[a-zA-Z]+|[a-zA-Z]+\\d+)$".toRegex().find(uiOrig) != null || uiOrig.count { it == '=' } > 1) {
            println("Invalid assignment")
        } else if (uiOrig.contains("=")) {
            val (keyMap, valueMap) = uiOrig.split(Regex("=")).map { it.trim() }
            if ("[a-zA-Z]+".toRegex().matches(keyMap) && "[+-]?\\d+".toRegex().matches(valueMap)) {
                variables[keyMap] = valueMap.toBigInteger()
            } else if ("\\d+".toRegex().find(valueMap) == null) {
                if (variables.containsKey(valueMap)) {
                    variables[keyMap] = variables[valueMap]!!
                } else println("Unknown variable")
            }
        } else if ("\\d+|[*+^√/-]+".toRegex().find(uiOrig) == null) {
            if (variables.containsKey(uiOrig)) println(variables[uiOrig]) else println("Unknown variable")
        } else {
            if (uiOrig.count { it == '(' } != uiOrig.count { it == ')' } ||
                "[*^√/]{2,}".toRegex().find(uiOrig) != null) {
                println("Invalid expression")
            } else {
                variables.forEach { (key, value) -> uiOrig = uiOrig.replace(key, value.toString()) }
                infixToRPN(
                    uiOrig.replace(" ", "").replace("---", "-").replace("--", "+").replace(Regex("[+]{2,}"), "+")
                        .replace("+-", "-").replace("\\W".toRegex()) { " ${it.value} " }
                        .replace("\\s{2,}".toRegex(), " ")
                )
            }
        }
    }

    private fun infixToRPN(expression: String) {
        val stackExp = ArrayDeque<Char>()
        val output = StringBuilder()
        val precedence = mapOf('+' to 1, '-' to 1, '*' to 2, '/' to 2, '(' to -1, ')' to -1, '^' to 3, '√' to 4)
        for (char in expression) {
            when (char) {
                '(' -> stackExp.addLast(char)
                ')' -> {
                    while (stackExp.last() != '(') {
                        output.append(stackExp.removeLast())
                    }
                    stackExp.removeLast()
                }
                in "+-*/^" -> {
                    while (stackExp.isNotEmpty() && (precedence[char]!! <= precedence[stackExp.last()]!!)) {
                        output.append(stackExp.removeLast())
                    }
                    stackExp.addLast(char)
                }
                '√' -> {
                    stackExp.addLast(char)
                }
                else -> output.append(char)
            }
        }
        while (stackExp.isNotEmpty()) output.append(stackExp.removeLast())
        calculateRPN(output.toString().replace("\\W".toRegex()) { " ${it.value} " }.replace("\\s{2,}".toRegex(), " "))
    }

    private fun calculateRPN(stringRPN: String) {
        val stackExp = ArrayDeque<BigInteger>()
        stringRPN.trim().split(" ").forEach {
            when (it) {
                "+" -> stackExp.addLast(stackExp.removeLast() + stackExp.removeLast())
                "-" -> {
                    val a = stackExp.removeLast()
                    stackExp.addLast(stackExp.removeLast() - a)
                }

                "*" -> stackExp.addLast(stackExp.removeLast() * stackExp.removeLast())
                "/" -> {
                    val b = stackExp.removeLast()
                    stackExp.addLast(stackExp.removeLast() / b)
                }

                "^" -> {
                    val b = stackExp.removeLast()
                    val a = stackExp.removeLast()
                    stackExp.addLast(a.pow(b.toInt()))
                }

                "√" -> {
                    val a = stackExp.removeLast()
                    stackExp.addLast(a.sqrt())
                }

                else -> stackExp.addLast(it.toBigInteger())
            }
        }
        println(stackExp.last())
    }
}

fun userInput() {
    val uiOriginal = readln().trim()
    when {
        uiOriginal.isEmpty() -> {}
        uiOriginal == "/exit" -> return
        uiOriginal == "/help" -> println("A calculator program that can use the + - / * ^ √ operators")
        uiOriginal.first() == '/' -> println("Unknown command")
        else -> Calculator(uiOriginal)
    }
    userInput()
}

fun main() = userInput().also { println("Bye!") }