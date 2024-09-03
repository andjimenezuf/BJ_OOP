package oop.practical.blackjack.solution;

import oop.practical.blackjack.lisp.Lisp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public final class CommandsTests {

    @Nested
    public final class DeckTests {

        @ParameterizedTest
        @MethodSource
        public void testDeck(String name, String setup, String expected) {
            test(setup, "(inspect :deck)", expected);
        }

        private static Stream<Arguments> testDeck() {
            return Stream.of(
                Arguments.of("Empty Deck", """
                    (do)
                    """, "Deck: (empty)"),
                Arguments.of("Single Card", """
                    (deck :2S)
                    """, "Deck: 2S"),
                Arguments.of("Multiple Cards", """
                    (deck :2S :10H :AC :7D)
                    """, "Deck: 2S, 10H, AC, 7D")
            );
        }

        @Test
        public void testDeckRandom() {
            var commands = new Commands();
            Assertions.assertDoesNotThrow(() -> commands.execute(Lisp.parse("(deck)")));
            var result = Assertions.assertDoesNotThrow(() -> commands.execute(Lisp.parse("(inspect :deck)")));
            var cards = Set.of(result.replace("Deck: ", "").split(", "));
            Assertions.assertEquals(52, cards.size());
            //Note: This test doesn't account for random behavior - make sure to check for that!
        }

    }

    @Nested
    public final class DealTests {

        @ParameterizedTest
        @MethodSource
        public void testDeal(String name, String setup, String expected) {
            testState(setup, expected);
        }

        private static Stream<Arguments> testDeal() {
            return Stream.of(
                Arguments.of("Existing Deck", """
                    (deck :2S :10H :AC :7D)
                    (deal)
                    """, """
                    Deck: (empty)
                    Player (13): 2S, AC (playing)
                    Dealer (? + 7): ?, 7D (waiting)
                    """),
                Arguments.of("Custom Deck", """
                    (deal :2S :10H :AC :7D)
                    """, """
                    Deck: (empty)
                    Player (13): 2S, AC (playing)
                    Dealer (? + 7): ?, 7D (waiting)
                    """),
                Arguments.of("Empty Deck", """
                    (deal)
                    """, null)
            );
        }

        @ParameterizedTest
        @MethodSource
        public void testDealBlackjack(String name, String setup, String expected) {
            testState(setup, expected);
        }

        private static Stream<Arguments> testDealBlackjack() {
            return Stream.of(
                Arguments.of("Player Blackjack", """
                    (deal :JS :10H :AC :6D)
                    """, """
                    Deck: (empty)
                    Player (21): JS, AC (won)
                    Dealer (16): 10H, 6D (lost)
                    """),
                Arguments.of("Dealer Blackjack", """
                    (deal :2S :10H :AC :AD)
                    """, """
                    Deck: (empty)
                    Player (13): 2S, AC (lost)
                    Dealer (21): 10H, AD (won)
                    """),
                Arguments.of("Empty Deck", """
                    (deal)
                    """, null)
            );
        }

    }

    @Nested
    public final class HitTests {

        @ParameterizedTest
        @MethodSource
        public void testHit(String name, String setup, String expected) {
            testState(setup, expected);
        }

        private static Stream<Arguments> testHit() {
            return Stream.of(
                Arguments.of("Playing", """
                    (deal :2S :10H :AC :7D :5S)
                    (hit)
                    """, """
                    Deck: (empty)
                    Player (18): 2S, AC, 5S (playing)
                    Dealer (? + 7): ?, 7D (waiting)
                    """),
                Arguments.of("21", """
                    (deal :2S :10H :AC :7D :8S)
                    (hit)
                    """, """
                    Deck: (empty)
                    Player (21): 2S, AC, 8S (won)
                    Dealer (17): 10H, 7D (lost)
                    """),
                Arguments.of("Ace Value Change", """
                    (deal :2S :10H :AC :7D :KS)
                    (hit)
                    """, """
                    Deck: (empty)
                    Player (13): 2S, AC, KS (playing)
                    Dealer (? + 7): ?, 7D (waiting)
                    """),
                Arguments.of("Busted", """
                    (deal :2S :10H :QC :7D :KS)
                    (hit)
                    """, """
                    Deck: (empty)
                    Player (22): 2S, QC, KS (busted)
                    Dealer (17): 10H, 7D (won)
                    """)
            );
        }

    }

    @Nested
    public final class StandTests {

        @ParameterizedTest
        @MethodSource
        public void testStand(String name, String setup, String expected) {
            testState(setup, expected);
        }

        private static Stream<Arguments> testStand() {
            return Stream.of(
                Arguments.of("Player Win", """
                    (deal :2S :10H :AC :7D)
                    (stand)
                    """, """
                    Deck: (empty)
                    Player (13): 2S, AC (lost)
                    Dealer (17): 10H, 7D (won)
                    """),
                Arguments.of("Player Loss", """
                    (deal :8S :10H :AC :7D)
                    (stand)
                    """, """
                    Deck: (empty)
                    Player (19): 8S, AC (won)
                    Dealer (17): 10H, 7D (lost)
                    """)
            );
        }

    }

    @Nested
    public final class SplitTests {

        @ParameterizedTest
        @MethodSource
        public void testSplit(String name, String setup, String expected) {
            testState(setup, expected);
        }

        private static Stream<Arguments> testSplit() {
            return Stream.of(
                Arguments.of("Split", """
                    (deal :10S :10H :10C :7D :6S :QC)
                    (split)
                    """, """
                    Deck: (empty)
                    Player (16): 10S, 6S (playing)
                    Player (20): 10C, QC (waiting)
                    Dealer (? + 7): ?, 7D (waiting, waiting)
                    """),
                Arguments.of("Blackjack", """
                    (deal :10S :10H :10C :7D :6S :AC)
                    (split)
                    """, """
                    Deck: (empty)
                    Player (16): 10S, 6S (playing)
                    Player (21): 10C, AC (won)
                    Dealer (? + 7): ?, 7D (waiting, lost)
                    """),
                Arguments.of("First Hand Resolved", """
                    (deal :10S :10H :10C :7D :6S :QC)
                    (split)
                    (stand)
                    """, """
                    Deck: (empty)
                    Player (16): 10S, 6S (resolved)
                    Player (20): 10C, QC (playing)
                    Dealer (? + 7): ?, 7D (waiting, waiting)
                    """)
            );
        }

    }

    @Nested
    public final class DoubleDownTests {

        @ParameterizedTest
        @MethodSource
        public void testDoubleDown(String name, String setup, String expected) {
            testState(setup, expected);
        }

        private static Stream<Arguments> testDoubleDown() {
            return Stream.of(
                Arguments.of("Resolved Early", """
                    (deal :2S :10H :AC :7D :6S)
                    (double-down)
                    """, """
                    Deck: (empty)
                    Player (19): 2S, AC, 6S (won)
                    Dealer (17): 10H, 7D (lost)
                    """)
            );
        }

    }

    private static void test(String setup, String command, String expected) {
        var commands = new Commands();
        Assertions.assertDoesNotThrow(() -> commands.execute(Lisp.parse("(do " + setup + ")")));
        if (expected != null) {
            var result = Assertions.assertDoesNotThrow(() -> commands.execute(Lisp.parse(command)));
            //Note: We use stripTrailing() to remove whitespace at the end of
            //the expected value (specifically, the newline resulting from
            //having the closing """ on a separate line for readable). We then
            //use stripIndent() in case the content is indented further than the
            //closing quotes (which was the issue with the original tests, but
            //fixed above). This is what we will use when grading submissions.
            //Note(2): Our Lisp parser ignores whitespace, so it doesn't mind
            //any extraneous indentation or newlines already.
            Assertions.assertEquals(expected.stripTrailing().stripIndent(), result.stripTrailing().stripIndent());
        } else {
            var error = Assertions.assertDoesNotThrow(() -> commands.execute(Lisp.parse("(inspect :error)")));
            Assertions.assertNotEquals("", error);
        }
    }

    private static void testState(String setup, String expected) {
        test(setup, "(do (inspect :deck) (inspect :player) (inspect :dealer) (inspect :error))", expected);
    }

}
