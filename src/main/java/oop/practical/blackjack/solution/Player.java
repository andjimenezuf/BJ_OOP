package oop.practical.blackjack.solution;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Player {
    public List<Card> hand;
    public List<Card> splitHand;
    private boolean hasSplit;
    public String status;
    private String splitStatus;
    private boolean isCurrentHandSplit = false; // indicates which hand is currently active


    public Player() {
        hand = new ArrayList<>();
        splitHand = new ArrayList<>();
        hasSplit = false;
    }
    public void switchToSplitHand() {
        if (hasSplit) {
            isCurrentHandSplit = true;
        }
    }
    public boolean isCurrentHandSplit() {
        return isCurrentHandSplit;
    }
    public List<Card> getMainHand() {
        return hand; // hand is the main hand
    }
    public List<Card> getSplitHand() {
        return splitHand; // hand created after split
    }
    public String getStatus(String handType) {
        if ("main".equals(handType)) {
            return this.status; // return the status of main hand
        } else if ("split".equals(handType)) {
            return this.splitStatus; // return the status of split hand
        }
        return ""; // empty string if handType is not recognized
    }


    public boolean canSplitHand() {
        // check if the hand has exactly two cards of  same rank: REVIION HAS TO COMPARE VALS ONLY
        //return hand.size() == 2 && hand.get(0).getRank() == hand.get(1).getRank();
        return hand.size() == 2 && hand.get(0).getValue() == hand.get(1).getValue();

    }

    public void split() {
        if (canSplitHand()) {
            splitHand.add(hand.remove(1)); // move the second card to split hand
            hasSplit = true; // indicate hand has been split
        }
    }

    public boolean hasSplitHand() {
        return hasSplit;
    }

    public void addCard(Card card, boolean toSplitHand) {
        if (toSplitHand && hasSplit) {
            splitHand.add(card);
        } else {
            hand.add(card);
        }
    }

    public int calculateHandValue(List<Card> handToEvaluate) {
        int value = 0;
        int acesCount = 0;

        for (Card card : handToEvaluate) {
            int cardValue = card.getValue();
            if (card.getRank() == Card.Rank.ACE) {
                acesCount++;
                cardValue = 11; // initially treat Ace as 11
            }
            value += cardValue;
        }

        // Adjust aces if value goes over 21
        while (value > 21 && acesCount > 0) {
            value -= 10; // Convert
            acesCount--;
        }

        return value;
    }
    public boolean hasBlackjack(List<Card> handToEvaluate) {
        if (handToEvaluate.size() == 2) { // blackjack only possible with two cards
            return calculateHandValue(handToEvaluate) == 21;
        }
        return false;
    }
    public boolean hasBlackjackMainHand() {
        return hasBlackjack(this.hand);
    }

    public boolean hasBlackjackSplitHand() {
        if (hasSplit) {
            return hasBlackjack(this.splitHand);
        }
        return false;
    }


    public void setStatus(String statusToEvaluate, String newStatus) {
        if ("main".equals(statusToEvaluate)) {
            this.status = newStatus; // set the status for main hand
        } else if ("split".equals(statusToEvaluate) && hasSplit) {
            this.splitStatus = newStatus; // set the status for the split hand, if exists
        }
    }

    public String inspectHand() {
        StringBuilder sb = new StringBuilder();

        // inspect the main hand
        sb.append("Player (")
                .append(calculateHandValue(hand))
                .append("): ")
                .append(hand.stream().map(Card::toString).collect(Collectors.joining(", ")))
                .append(" (")
                .append(status)
                .append(")");

        // if there's a split hand, inspect it in new line
        if (hasSplit) {
            sb.append("\nPlayer (")
                    .append(calculateHandValue(splitHand))
                    .append("): ")
                    .append(splitHand.stream().map(Card::toString).collect(Collectors.joining(", ")))
                    .append(" (")
                    .append(splitStatus)
                    .append(")");
        }

        return sb.toString();
    }


}
