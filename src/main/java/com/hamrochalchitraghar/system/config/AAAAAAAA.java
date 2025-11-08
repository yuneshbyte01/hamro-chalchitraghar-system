package com.hamrochalchitraghar.system.config;

import java.util.Scanner;

public class AAAAAAAA {
    public static String[] users = {"admin", "user1", "user2"};
    public static String[] passwords = {"adminpass", "user1pass", "user2pass"};

    public static boolean validateUser(String username, String password) {
        for (int i = 0; i < users.length; i++) {
            if (users[i].equals(username) && passwords[i].equals(password)) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if (validateUser(username, password)) {
            System.out.println("Login successful!");
        } else {
            System.out.println("Invalid username or password.");
        }

        scanner.close();
    }
}
