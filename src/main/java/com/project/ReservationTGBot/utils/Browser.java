package com.project.ReservationTGBot.utils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Browser {
    public static void openURL(String url) {
        System.out.println("\nWelcome to Multi Brow Pop.\nThis aims to popup a browsers in multiple operating systems.\n");

//        String url = "http://www.birdfolk.co.uk/cricmob";
        System.out.println("We're going to this page: "+ url);

        String myOS = System.getProperty("os.name").toLowerCase();
        System.out.println("(Your operating system is: "+ myOS +")\n");

        try {
            if(Desktop.isDesktopSupported()) { // Probably Windows
                System.out.println(" -- Going with Desktop.browse ...");
                Desktop desktop = Desktop.getDesktop();
                desktop.browse(new URI(url));
            } else { // Definitely Non-windows
                Runtime runtime = Runtime.getRuntime();
                if(myOS.contains("mac")) { // Apples
                    System.out.println(" -- Going on Apple with 'open'...");
                    runtime.exec("open " + url);
                }
                else if(myOS.contains("nix") || myOS.contains("nux")) { // Linux flavours
                    System.out.println(" -- Going on Linux with 'xdg-open'...");
                    runtime.exec("xdg-open " + url);
                }
                else
                    System.out.println("I was unable/unwilling to launch a browser in your OS :( #SadFace");
            }
            System.out.println("\nThings have finished.\nI hope you're OK.");
        }
        catch(IOException | URISyntaxException eek) {
            System.out.println("**Stuff wrongly: "+ eek.getMessage());
        }
    }
}
