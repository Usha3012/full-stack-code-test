package se.kry.codetest.util;

public class Logger {
    public static void log(String message){
        System.out.println(message);
    }
    public static void log(String message,Throwable throwable){
        System.out.println(message);
        throwable.printStackTrace();
    }
}
