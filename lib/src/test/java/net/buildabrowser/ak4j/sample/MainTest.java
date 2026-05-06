package net.buildabrowser.ak4j.sample;

import net.buildabrowser.ak4j.AK4J;

public class MainTest {
    
    public static void main(String[] args) {
        AK4J.init();
        System.out.println("Running sample program for AK4J v" + AK4J.versionString() + "!");
    }

}
