package oop.practical.blackjack.solution;

import oop.practical.blackjack.lisp.Ast;

import java.util.List;
import java.util.stream.Collectors;

public final class Commands {
    private Player player;
    private Dealer dealer;
    private Deck deck;
    private String lastError = "";

    public String execute(Ast ast) {
        assert ast instanceof Ast.Function;
        var function = (Ast.Function) ast;
        switch (function.name()) {
            case "do" -> {
                return function.arguments().stream()
                    .map(this::execute)
                    .filter(r -> !r.isEmpty())
                    .collect(Collectors.joining("\n"));
            }
            case "deck" -> {
                assert function.arguments().stream().allMatch(a -> a instanceof Ast.Atom);
                var atoms = function.arguments().stream().map(a -> ((Ast.Atom) a).name()).toList();
                return deck(atoms);
            }
            case "deal" -> {
                assert function.arguments().stream().allMatch(a -> a instanceof Ast.Atom);
                var atoms = function.arguments().stream().map(a -> ((Ast.Atom) a).name()).toList();
                return deal(atoms);
            }
            case "hit" -> {
                assert function.arguments().isEmpty();
                return hit();
            }
            case "stand" -> {
                assert function.arguments().isEmpty();
                return stand();
            }
            case "split" -> {
                assert function.arguments().isEmpty();
                return split();
            }
            case "double-down", "doubleDown" -> {
                assert function.arguments().isEmpty();
                return doubleDown();
            }
            case "inspect" -> {
                assert function.arguments().size() == 1 && function.arguments().getFirst() instanceof Ast.Atom;
                var name = ((Ast.Atom) function.arguments().getFirst()).name();
                return inspect(name);
            }
            default -> throw new AssertionError(function.name());
        }
    }

    public String deck(List<String> cards) {
        if (deck == null) {
            deck = new Deck();
        }

        if (cards.isEmpty()) {
            // Shuffle deck if no cards provided
            deck.shuffle();
        } else {
            // otherwise clear curxrent deck, add the provided cards
            deck.clear();
            for (String cardStr : cards) {
                deck.addCard(parseCardString(cardStr));
            }
        }

        // return state of the deck.
        return "Deck set with " + (cards.isEmpty() ? "shuffled cards." : "provided cards.");
    }

    private Card parseCardString(String cardStr) {
        String rankStr = cardStr.substring(0, cardStr.length() - 1);
        String suitStr = cardStr.substring(cardStr.length() - 1);

        Card.Rank rank;
        switch(rankStr) {
            case "2": rank = Card.Rank.TWO; break;
            case "3": rank = Card.Rank.THREE; break;
            case "4": rank = Card.Rank.FOUR; break;
            case "5": rank = Card.Rank.FIVE; break;
            case "6": rank = Card.Rank.SIX; break;
            case "7": rank = Card.Rank.SEVEN; break;
            case "8": rank = Card.Rank.EIGHT; break;
            case "9": rank = Card.Rank.NINE; break;

            case "10": rank = Card.Rank.TEN; break;
            case "J": rank = Card.Rank.JACK; break;
            case "Q": rank = Card.Rank.QUEEN; break;
            case "K": rank = Card.Rank.KING; break;
            case "A": rank = Card.Rank.ACE; break;
            default: throw new IllegalArgumentException("invald rank: " + rankStr);
        }

        Card.Suit suit;
        switch(suitStr) {
            case "S": suit = Card.Suit.SPADES; break;
            case "H": suit = Card.Suit.HEARTS; break;
            case "C": suit = Card.Suit.CLUBS; break;
            case "D": suit = Card.Suit.DIAMONDS; break;


            default: throw new IllegalArgumentException("not valid suite: " + suitStr);
        }

        return new Card(rank, suit);
    }
    public String deal(List<String> cards) {
        // Check if cards are not provided and deck is not initialized or is empty
        if (cards.isEmpty() && (deck == null || deck.isEmpty())) {
            lastError = "The deck is empty and thus cards cannot be dealt.";
            return ""; // do not return the error, just record it
        }
        if (deck == null) { // If no deck exists and cards provided, initialize it
            deck = new Deck();
        }
        // If cards are provided, set up the deck with those cards
        if (!cards.isEmpty()) {
            deck.clear();
            for (String cardStr : cards) {
                deck.addCard(parseCardString(cardStr));
            }
        }

        // If deck does not have enough cards to deal, record an error
        if (deck.getSize() < 4) {
            lastError = "Not enough cards to deal.";
            return "";
        }


        // deal the cards
        player = new Player();
        dealer = new Dealer();
        player.addCard(deck.dealCard(), false);
        dealer.addCard(deck.dealCard());
        player.addCard(deck.dealCard(), false);
        dealer.addCard(deck.dealCard());


        // Check for Blackjack in the player's main hand and the dealer's hand
        boolean playerMainHandBlackjack = player.hasBlackjackMainHand();
        boolean dealerHasBlackjack = dealer.hasBlackjack();
        // Determine outcome
        if (playerMainHandBlackjack && dealerHasBlackjack) {
            player.setStatus("main", "tied");
            dealer.setStatus("tied");
            return "Both player and dealer have Blackjack! It's a tie.";
        } else if (playerMainHandBlackjack) {
            player.setStatus("main", "won");
            dealer.setStatus("lost");
            return "Player has Blackjack! Player wins.";
        } else if (dealerHasBlackjack) {
            player.setStatus("main", "lost");
            dealer.setStatus("won");
            return "Dealer has Blackjack! Dealer wins.";
        }
        player.setStatus("main", "playing");
        dealer.setStatus("waiting");

        lastError = "";
        return "Cards dealt successfully.";

    }


    public String hit() {
        if (deck == null || deck.isEmpty()) {
            lastError = "The deck is empty, cannot hit.";
            return "";
        }

        // Determine which hand to deal to
        boolean dealToSplitHand = player.hasSplitHand() && !player.isCurrentHandSplit() &&
                player.getStatus("main").equals("busted") &&
                !player.getStatus("split").equals("busted");

        // Switch to split hand if the main hand has busted and the split hand is still in play
        if (dealToSplitHand) {
            player.switchToSplitHand();
        }

        // Deal a card to the current hand (main or split)
        Card newCard = deck.dealCard();
        player.addCard(newCard, player.isCurrentHandSplit());

        // Evaluate the player's current hand value
        int handValue = player.calculateHandValue(player.isCurrentHandSplit() ? player.getSplitHand() : player.getMainHand());

        // Check for player bust in the current hand
        if (handValue > 21) {
            if (player.isCurrentHandSplit()) {
                player.setStatus("split", "busted");
                dealer.setStatusSplit("won"); // Set dealer's split status to won because the split hand busted
            } else {
                player.setStatus("main", "busted");
                dealer.setStatus("won"); // Set dealer's main status to won because the main hand busted
                if (player.hasSplitHand()) {
                    player.setStatus("split", "playing"); // Set split hand to playing if it exists
                } else {
                    dealer.setStatusSplit("won"); // Dealer wins the split status by default as there is no split hand
                }
            }
            lastError = "";
            return player.inspectHand();
        }

        // Dealer hits if the player has not busted and the dealer's hand value is 16 or less
        if (!player.getStatus("main").equals("busted") || (player.hasSplitHand() && !player.getStatus("split").equals("busted"))) {
            while (dealer.calculateHandValue() <= 16) {
                Card dealerCard = deck.dealCard();
                dealer.addCard(dealerCard);
            }
        }

        int dealerHandValue = dealer.calculateHandValue();

        // Check for dealer bust
        if (dealerHandValue > 21) {
            dealer.setStatus("busted");
            dealer.setStatusSplit("busted");
            player.setStatus(player.isCurrentHandSplit() ? "split" : "main", "won");
        }
        // Check for a tie if both player and dealer have 21
        else if (handValue == 21 && dealerHandValue == 21) {
            player.setStatus(player.isCurrentHandSplit() ? "split" : "main", "tied");
            dealer.setStatus("tied");
            dealer.setStatusSplit("tied");
        }
        // Player has 21 but dealer does not
        else if (handValue == 21) {
            player.setStatus(player.isCurrentHandSplit() ? "split" : "main", "won");
            dealer.setStatus("lost");
            dealer.setStatusSplit("lost");
        }
        // If nobody busted or has exactly 21, continue the game
        else {
            player.setStatus(player.isCurrentHandSplit() ? "split" : "main", "playing");
            // Dealer stands if they have 17 or more
            if (dealerHandValue >= 17) {
                dealer.setStatus("waiting");
            }
        }

        lastError = "";
        return player.inspectHand();
    }



    public String stand() {
        if (player == null || dealer == null) {
            lastError = "Game not set up correctly.";
            return "Error: " + lastError;
        }
        if(deck.isEmpty() && player.status=="won"){
            lastError = "game over or somthing else";
            return "Error: " + lastError;
        }

        // determine which hand  player is standing on and set status to resolved
        if (!player.isCurrentHandSplit()) {
            player.setStatus("main", "resolved");

            // If the player has a split hand, the game should now wait for the player's action on the split hand
            if (player.hasSplitHand()) {
                //  split hand is now  current hand
                player.switchToSplitHand();
                player.setStatus("split", "playing");
            } else {
                // if no split hand, the dealer takes their turn after player stand
                playDealerHand();
                determineOutcomes();
            }
        } else {
            player.setStatus("split", "resolved");
            playDealerHand();
            determineOutcomes();
        }

        lastError = "";
        return player.inspectHand();

    }

    private void playDealerHand() {
        while (dealer.calculateHandValue() < 17) {
            dealer.addCard(deck.dealCard());
        }
    }
    private void determineOutcomeForHand(List<Card> hand, String handType) {
        int playerValue = player.calculateHandValue(hand);
        int dealerValue = dealer.calculateHandValue();
        boolean dealerBusted = dealerValue > 21;
        boolean playerBusted = playerValue > 21;

        if (playerBusted) {
            player.setStatus(handType, "busted");
            dealer.setStatus(handType.equals("main") ? "won" : dealer.getStatusSplit().equals("") ? "won" : dealer.getStatusSplit());
        } else if (dealerBusted || playerValue > dealerValue) {
            player.setStatus(handType, "won");
            dealer.setStatus(handType.equals("main") ? "lost" : dealer.getStatusSplit().equals("") ? "lost" : dealer.getStatusSplit());
        } else if (playerValue < dealerValue) {
            player.setStatus(handType, "lost");
            dealer.setStatus(handType.equals("main") ? "won" : dealer.getStatusSplit().equals("") ? "won" : dealer.getStatusSplit());
        } else {
            player.setStatus(handType, "tied");
            dealer.setStatus(handType.equals("main") ? "tied" : dealer.getStatusSplit().equals("") ? "tied" : dealer.getStatusSplit());
        }
    }
    private void determineOutcomes() {
        // determine outcome for the main hand if resolved
        if ("resolved".equals(player.getStatus("main"))) {
            determineOutcomeForHand(player.getMainHand(), "main");
        }

        // determine the outcome for split hand if it exists and is resolved
        if (player.hasSplitHand() && "resolved".equals(player.getStatus("split"))) {
            determineOutcomeForHand(player.getSplitHand(), "split");
        }
    }
    public String split() {
        if (player == null || dealer == null || deck == null) {
            lastError = "Game not set up correctly.";
            return "Error: " + lastError;
        }

        // Check if the player's hand can be split
        if (!player.canSplitHand()) {
            lastError = "Cannot split hand.";
            return "Error: " + lastError;
        }
        if(player.hasSplitHand()){
            lastError = "ALREADY SPLITTED HAND BOZO";
            return "Error: " + lastError;
        }

        // perform split
        player.split();

        // deal a new card to each of new hands
        player.addCard(deck.dealCard(), false); // Add to the original hand
        player.addCard(deck.dealCard(), true);  // Add to the split hand

        // if one of hands has a value of 21 after split, auto win
        if (player.hasBlackjackMainHand()) {
            player.setStatus("main", "won");
            dealer.setStatus("lost");

        } else {
            player.setStatus("main", "playing");
        }

        if (player.hasSplitHand() && player.hasBlackjackSplitHand()) {
            player.setStatus("split", "won");
            dealer.setStatusSplit("lost");
        } else {
            player.setStatus("split", "waiting");
            dealer.setStatusSplit("waiting"); //dealer  waiting for the outcome of the split

        }

        lastError = "";
        return player.inspectHand();
    }

    public String doubleDown() {
        if (deck == null || deck.isEmpty()) {
            lastError = "The deck is empty, cannot double down.";
            return "Error: " + lastError;
        }
        if (player == null || dealer == null) {
            lastError = "Game not set up correctly.";
            return "Error: " + lastError;
        }

        List<Card> currentHand = player.isCurrentHandSplit() ? player.getSplitHand() : player.getMainHand();

        // The player can only double down if they have exactly two cards in their current hand
        if (currentHand.size() != 2) {
            lastError = "Double down is only allowed on the initial hand of two cards.";
            return "Error: " + lastError;
        }

        // Give one additional card to the current hand
        player.addCard(deck.dealCard(), player.isCurrentHandSplit());

        // After doubling down, the player's turn ends for the current hand
        player.setStatus(player.isCurrentHandSplit() ? "split" : "main", "resolved");

        // Determine the outcome for the hand that just played
        determineOutcomeForHand(currentHand, player.isCurrentHandSplit() ? "split" : "main");

        // Check if the game should switch to the split hand or if all player hands are resolved
        if (!player.isCurrentHandSplit() && player.hasSplitHand()) {
            // Switch to the split hand if the main hand is resolved and there is a split hand
            player.switchToSplitHand();
            // Set the split hand as playing, waiting for player action
            player.setStatus("split", "playing");
        } else {
            // If the double down was on the split hand, or there's no split hand, play dealer's hand
            playDealerHand();
            determineOutcomes();
        }

        lastError = "";
        return player.inspectHand();
    }


    public String inspect(String name) {
        switch (name) {
            case "deck":
                return deck != null ? deck.toString() : "Deck: (empty)";
            case "player":
                // Make sure to check if player is not null before calling toString
                return player != null ? player.inspectHand() : "Player: (empty)";
            case "dealer":
                // Make sure to check if dealer is not null before calling toString
                return dealer != null ? dealer.inspect(player.hasSplitHand()) : "Dealer: (empty)";
            case "error":
                // return the last recorded error message
                return lastError.isEmpty() ? "" : "Error: " + lastError;
            default:
                return "Invalid inspect target: " + name;
        }
    }



}
