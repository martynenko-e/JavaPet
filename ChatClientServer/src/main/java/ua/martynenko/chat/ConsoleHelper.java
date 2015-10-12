package ua.martynenko.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleHelper
{
    private static BufferedReader bis = new BufferedReader(new InputStreamReader(System.in));

    public static void writeMessage(String message) {
        System.out.println(message);
    }

    public static String readString() {

        String text = "";
        while(true) {
            try {
                text = bis.readLine();
            } catch (IOException e) {
                writeMessage("Произошла ошибка при попытке ввода текста. Попробуйте еще раз.");
                continue;
            }
            break;
        }
        return text;
    }

    public static int readInt() {
        int number = 0;
        while(true) {
            try {
                number = Integer.parseInt(readString().trim());
            } catch (NumberFormatException ex) {
                writeMessage("Произошла ошибка при попытке ввода числа. Попробуйте еще раз.");
                continue;
            }
            break;
        }
        return number;
    }
}
