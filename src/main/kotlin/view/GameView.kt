package view

import controller.GameController
import model.Card
import model.GameListener
import model.GameModel
import model.Player

object GameView : GameListener {

    override fun onWin(player: Player, computer: Player, isUserTurn: Boolean, turn: Int) {
        if (isUserTurn) println("${player.name} wins cards") else println("${computer.name} wins cards")
        if (turn != 48) {
            println("Score: ${player.name} ${player.points} - ${computer.name} ${computer.points}")
            println("Cards: ${player.name} ${player.wonCards.size} - ${computer.name} ${computer.wonCards.size}")
        }
    }

    override fun onPlay(player: Player, card: Card) {
        println("${player.name} plays $card")
    }

    override fun onGameOver(player: Player, computer: Player) {
        println("Score: ${player.name} ${player.points} - ${computer.name} ${computer.points}")
        println("Cards: ${player.name} ${player.wonCards.size} - ${computer.name} ${computer.wonCards.size}")
    }

    fun finishGame() {
        printlnMessage("Game Over")
    }

    fun askForCardToPlay(upTo: Int): String {
        val regex = Regex("^[1-$upTo]|exit$")
        var input = ""
        while (!regex.matches(input)) {
            printlnMessage("Choose a card to play (1-$upTo):")
            input = getUserInput()
        }
        return input
    }

    fun printBoard(cardsOnTable: List<Card>? = null) {
        if (!cardsOnTable.isNullOrEmpty()) {
            printlnMessage("${cardsOnTable.size} cards on the table, and the top card is ${cardsOnTable.last()}")
        } else {
            println("No cards on the table")
        }
    }

    fun askIfPlayerFirst() {
        printlnMessage("Play first?")
    }

    fun getUserInput(): String {
        return readLine()!!
    }

    fun printCards(cards: List<Card>) {
        cards.forEach { card -> print("$card ") }
        println()
    }

    fun printlnMessage(message: String = "") {
        println(message)
    }

    fun printMessage(message: String) {
        print(message)
    }

    fun printCardsInHand(cards: List<Card>) {
        print("Cards in hand: ")
        cards.forEachIndexed { index, card -> print("${index + 1})$card ") }
        println()
    }
}

fun main() {
    val game = GameModel()
    val controller = GameController(game, "Indigo Card Game")
    controller.run()
}