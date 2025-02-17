package net.rickiekarp.shapass;

/**
 * Using a Main.class as a wrapper to work around JavaFX module loading requirement
 * https://stackoverflow.com/questions/52569724/javafx-11-create-a-jar-file-with-gradle/52571719#52571719
 */
public class Main {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}