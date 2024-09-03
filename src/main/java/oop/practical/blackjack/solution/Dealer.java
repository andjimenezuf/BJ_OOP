package oop.practical.blackjack.solution;

import java.util.ArrayList;
import java.util.List;

public class Dealer {
    private List<Card> hand; // The dealer's hand
    private String status; // The status of the dealer's hand (e.g., waiting, won, lost)
    private String statusSplit; // The status of the dealer's hand in relation to the player's split hand

    public Dealer() {
        hand = new ArrayList<>();
        status = "waiting"; // default status
        statusSplit = ""; // default status for the split hand
    }

    public int calculateHandValue() {
        return calculateHandValue(this.hand);
    }

    public boolean hasBlackjack() {
        return this.hand.size() == 2 && calculateHandValue() == 21;
    }

    public void setStatus(String newStatus) {
        this.status = newStatus;
    }
    public void setStatusSplit(String newStatus){
        this.statusSplit = newStatus;
    }

    public void addCard(Card card) {
        hand.add(card);
    }
    public String getStatusSplit() {
        return statusSplit;
    }


    private int calculateHandValue(List<Card> handToEvaluate) {
        int value = 0;
        int acesCount = 0;

        for (Card card : handToEvaluate) {
            int cardValue = card.getValue();
            if (card.getRank() == Card.Rank.ACE) {
                acesCount++;
                cardValue = 11; // Initially treat Ace as 11
            }
            value += cardValue;
        }
        while (value > 21 && acesCount > 0) {
            value -= 10;
            acesCount--;
        }

        return value;
    }

    public String inspect(boolean playerHasSplit) {
        StringBuilder sb = new StringBuilder("Dealer (");

        // Check if the player's turn is still ongoing
        if (status.equals("waiting") || (playerHasSplit && statusSplit.equals("waiting"))) {
            sb.append("? + ");
            if (hand.size() > 1) {
                sb.append(hand.get(1).getValue()); // Value of the second card (face-up card)
            } else {
                sb.append("?"); // The second card is not available yet
            }
            sb.append("): ?"); // Hole card
            if (hand.size() > 1) {
                sb.append(", ").append(hand.get(1)); // Append the face-up card
            }
        } else {
            // Player's turn is completed, show the dealer's total value and all cards
            sb.append(calculateHandValue()).append("): ");
            for (Card card : hand) {
                sb.append(card).append(", ");
            }
            // Remove the trailing comma and space
            sb.delete(sb.length() - 2, sb.length());
        }

        // Append the dealer's status for the main hand
        sb.append(" (").append(this.status);

        // If the player has split, append the dealer's status for the split hand
        if (playerHasSplit) {
            sb.append(", ").append(statusSplit); // Use the status related to the split hand
        }

        // Close the parentheses for the status section
        sb.append(")");

        return sb.toString();
    }

}


 /*   public String inspect(boolean playerHasSplit) {
        StringBuilder sb = new StringBuilder("Dealer (");

        if (status.equals("waiting")) {
            sb.append("? + ");
        } else { /
            sb.append(calculateHandValue()).append("): ");
        }

        sb.append(this.status.equals("waiting") ? "?," : hand.get(0).toString() + ",");
        for (int i = 1; i < hand.size(); i++) {
            sb.append(" ").append(hand.get(i).toString());
        }

        if (playerHasSplit) {
            sb.append(" (").append(this.status).append(")");

            String additionalStatus = determineAdditionalStatus();
            sb.append(" ").append(additionalStatus);
        } else {
            sb.append(" (").append(this.status).append(")");
        }

        return sb.toString();
    }*/



