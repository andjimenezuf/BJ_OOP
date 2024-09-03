package oop.practical.blackjack;

import oop.practical.blackjack.lisp.Lisp;
import oop.practical.blackjack.lisp.ParseException;
import oop.practical.blackjack.solution.Commands;

import java.util.Scanner;

public final class Main {

    public static void main(String[] args) {
        var commands = new Commands();
        var scanner = new Scanner(System.in);
        while (true) {
            var input = scanner.nextLine();
            if (input.equals("exit")) {
                break;
            }
            try {
                var ast = Lisp.parse(input);
                System.out.println(commands.execute(ast));
            } catch (ParseException e) {
                System.out.println("Error parsing input: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Unexpected exception: " + e.getMessage());
            }
        }
    }

}
