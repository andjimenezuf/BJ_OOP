package oop.practical.blackjack.solution;

public class Card {
    public enum Suit {
        HEARTS, DIAMONDS, CLUBS, SPADES
    }

    public enum Rank {
        TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE
    }
    private final Suit suit;
    private final Rank rank;

    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }

    public int getValue() {
        switch (rank) {
            case ACE:
                //  ACE can be 1 or 11 depending on the hand
                return 11;
            case TWO: return 2;
            case THREE: return 3;
            case FOUR: return 4;
            case FIVE: return 5;
            case SIX: return 6;
            case SEVEN: return 7;
            case EIGHT: return 8;
            case NINE: return 9;
            case TEN:
            case JACK:
            case QUEEN:
            case KING:
                return 10;
            default:
                throw new IllegalArgumentException("Invalid card rank");
        }
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }
    @Override
    public String toString() {
        String rankString;
        switch (rank) {
            case ACE: rankString = "A"; break;
            case TWO: rankString = "2"; break;
            case THREE: rankString = "3"; break;
            case FOUR: rankString = "4"; break;
            case FIVE: rankString = "5"; break;
            case SIX: rankString = "6"; break;
            case SEVEN: rankString = "7"; break;
            case EIGHT: rankString = "8"; break;
            case NINE: rankString = "9"; break;
            case TEN: rankString = "10"; break;
            case JACK: rankString = "J"; break;
            case QUEEN: rankString = "Q"; break;
            case KING: rankString = "K"; break;
            default: throw new IllegalArgumentException("Invalid card rank");
        }
        return rankString + suit.toString().substring(0, 1);
    }


}

