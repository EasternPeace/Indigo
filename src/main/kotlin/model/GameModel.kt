package model

import view.GameView

data class Card(val rank: Ranks, val suit: Suits) {
    override fun toString(): String {
        return "${rank.sign}${suit.sign}"
    }
}

enum class Suits(val sign: Char) {
    DIAMONDS('♦'),
    HEARTS('♥'),
    SPADES('♠'),
    CLUBS('♣')
}

enum class Ranks(val sign: String, val points: Int) {
    ACE("A", 1),
    TWO("2", 0),
    THREE("3", 0),
    FOUR("4", 0),
    FIVE("5", 0),
    SIX("6", 0),
    SEVEN("7", 0),
    EIGHT("8", 0),
    NINE("9", 0),
    TEN("10", 1),
    JACK("J", 1),
    QUEEN("Q", 1),
    KING("K", 1);

}

object Deck {
    val cards = mutableListOf<Card>()

    init {
        resetDeck()
    }

    fun getTopCard(): Card {
        return cards.first()
    }

    fun getTopCards(number: Int): List<Card> {
        return cards.take(number)
    }

    fun removeCard(card: Card) {
        cards.remove(card)
    }

    fun removeCards(cardsToRemove: List<Card>) {
        cards.removeAll(cardsToRemove)
    }

    fun shuffleDeck() {
        cards.shuffle()
    }

    fun resetDeck() {
        cards.clear()
        for (suit in Suits.values()) {
            for (rank in Ranks.values()) {
                cards.add(Card(rank, suit))
            }
        }
    }
}

enum class GameState {
    RUNNING,
    FINISHED
}

class GameModel {
    var isUserFirst: Boolean = false
    var cardsOnTable: MutableList<Card> = mutableListOf()
    val player = Player("Player")
    private val computer = Player("Computer")
    var lastWinner: Player? = null
    var isLastRound = false
    var status = GameState.RUNNING
    var turn = 0
    val listeners: MutableList<GameListener> = mutableListOf()

    fun playCard(index: Int): Card {
        val cardToPlay = player.hand[index]
        player.hand.remove(cardToPlay)
        if (cardsOnTable.isNotEmpty()) {
            val won = cardsOnTable.last().rank == cardToPlay.rank || cardsOnTable.last().suit == cardToPlay.suit
            cardsOnTable.add(cardToPlay)
            if (won) {
                player.wonCards.addAll(cardsOnTable)
                cardsOnTable.clear()
                lastWinner = player
                listeners.forEach { it.onWin(player, computer, true, turn) }
            }
        } else {
            cardsOnTable.add(cardToPlay)
        }
        return cardToPlay
    }

    fun aiPlayCard(): Card {
        val cardToPlay = computer.hand.random()
        computer.hand.remove(cardToPlay)
        listeners.forEach {
            it.onPlay(computer, cardToPlay)
        }
        if (cardsOnTable.isNotEmpty()) {
            val won = cardsOnTable.last().rank == cardToPlay.rank || cardsOnTable.last().suit == cardToPlay.suit
            cardsOnTable.add(cardToPlay)
            if (won) {
                computer.wonCards.addAll(cardsOnTable)
                cardsOnTable.clear()
                lastWinner = computer
                listeners.forEach { it.onWin(player, computer, false, turn) }
            }
        } else {
            cardsOnTable.add(cardToPlay)
        }
        return cardToPlay
    }

    private fun calculateBonus() {
        return if (player.wonCards.size > computer.wonCards.size) {
            player.bonusPoints += 3
        } else if (computer.wonCards.size > player.wonCards.size) {
            computer.bonusPoints += 3
        } else {
            if (isUserFirst) {
                player.bonusPoints += 3
            } else {
                computer.bonusPoints += 3
            }
        }
    }

    fun dealCards() {
        if (Deck.cards.size < 12) {
            if (cardsOnTable.isNotEmpty()) {
                if (lastWinner != null) {
                    lastWinner?.wonCards?.addAll(cardsOnTable)
                } else {
                    if (isUserFirst) {
                        player.hand.addAll(cardsOnTable)
                    } else {
                        computer.hand.addAll(cardsOnTable)
                    }
                }
                cardsOnTable.clear()
            }
            calculateBonus()
            status = GameState.FINISHED
            listeners.forEach { it.onGameOver(player, computer) }
        }
        val cards = Deck.getTopCards(12)
        Deck.removeCards(cards)
        player.hand.addAll(cards.take(6))
        computer.hand.addAll(cards.takeLast(6))
    }

    fun shuffle() {
        Deck.shuffleDeck()
    }

    fun getCards(numberOfCards: Int): List<Card> {
        if (numberOfCards in 1..Deck.cards.size) {
            val cards = Deck.getTopCards(numberOfCards)
            Deck.removeCards(cards)
            return cards
        } else {
            throw IllegalArgumentException("The remaining cards are insufficient to meet the request.")
        }
    }
}

open class Player(val name: String) {
    val hand: MutableList<Card> = mutableListOf()
    val wonCards: MutableList<Card> = mutableListOf()
    var bonusPoints: Int = 0
    val points: Int
        get() {
            var total = 0
            wonCards.forEach {
                total += it.rank.points
            }
            return total + bonusPoints
        }
}


interface GameListener {
    fun onWin(player: Player, computer: Player, isUserTurn: Boolean, turn: Int)
    fun onPlay(player: Player, card: Card)
    fun onGameOver(player: Player, computer: Player)
}