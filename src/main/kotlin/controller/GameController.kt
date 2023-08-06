package controller

import model.Deck
import model.GameModel
import model.GameState
import view.GameView

class GameController(private val game: GameModel, private val gameTitle: String) {

    init {
        game.listeners.add(GameView)
    }

    private fun performUserPlay() {
        game.turn = game.turn + 1
        GameView.printCardsInHand(game.player.hand)
        val userChoice = GameView.askForCardToPlay(game.player.hand.size)
        if (userChoice == "exit") {
            game.status = GameState.FINISHED
            return
        }
        game.playCard(userChoice.toInt() - 1)
        if (game.turn == 48 && game.cardsOnTable.isNotEmpty()) {
            GameView.printBoard(game.cardsOnTable)
        }
    }

    private fun performComputerPlay() {
        game.turn = game.turn + 1
        game.aiPlayCard()
        if (game.turn == 48 && game.cardsOnTable.isNotEmpty()) {
            GameView.printBoard(game.cardsOnTable)
        }
    }

    private fun performRound(firstPlayer: () -> Unit, secondPlayer: () -> Unit) {
        game.dealCards()
        for (i in 1..6) {
            if(game.status == GameState.FINISHED) {
                break
            }
            GameView.printBoard(game.cardsOnTable)
            firstPlayer.invoke()
            if(game.status == GameState.FINISHED) {
                break
            }
            GameView.printBoard(game.cardsOnTable)
            secondPlayer.invoke()
        }
    }

    fun run() {
        GameView.printlnMessage(gameTitle)
        while (true) {
            GameView.askIfPlayerFirst()
            when (GameView.getUserInput().lowercase()) {
                "yes" -> game.isUserFirst = true
                "no" -> game.isUserFirst = false
                else -> continue
            }
            break
        }

        game.shuffle()
        GameView.printMessage("Initial cards on the table: ")
        val firstFourCards = game.getCards(4)
        GameView.printCards(firstFourCards)
        game.cardsOnTable.addAll(firstFourCards)

        while (game.status != GameState.FINISHED) {
            if (game.isUserFirst) {
                performRound(::performUserPlay, ::performComputerPlay) //pass lambda functions as arguments
            } else {
                performRound(::performComputerPlay, ::performUserPlay)
            }
        }
        GameView.finishGame()
    }
}
