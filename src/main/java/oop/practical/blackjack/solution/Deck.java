package oop.practical.blackjack.solution;
import static oop.practical.blackjack.solution.Card.Suit;
import static oop.practical.blackjack.solution.Card.Rank;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class Deck {
    public Queue<Card> cards;

    public Deck() {
        cards = new LinkedList<>();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(rank, suit));
            }
        }
        shuffle();
    }

    public void shuffle() {
        // Convert the queue to a list to shuffle it
        List<Card> cardList = new ArrayList<>(cards);
        Collections.shuffle(cardList);
        cards.clear();
        cards.addAll(cardList);
    }

    public Card dealCard() {

        return cards.poll();
    }

    public void clear() {
        cards.clear();
    }

    public void addCard(Card card) {
        cards.offer(card);
    }
    public boolean isEmpty(){
        return cards.isEmpty();
    }

    @Override
    public String toString() {
        if (cards.isEmpty()) {
            return "Deck: (empty)";
        } else {
            return "Deck: " + cards.stream()
                    .map(Card::toString)
                    .collect(Collectors.joining(", "));
        }
    }

    public int getSize(){
        return cards.size();
    }
}

