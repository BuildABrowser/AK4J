package net.buildabrowser.ak4j.sample;

import net.buildabrowser.ak4j.AK4J;
import net.buildabrowser.ak4j.AK4JCallbacks;
import net.buildabrowser.ak4j.AK4JHandle;

public class MainTest implements AK4JCallbacks {
  
  public static void main(String[] args) throws Throwable {
    MainTest mainTest = new MainTest();
    try (AK4JHandle ak4jHandle = AK4J.init(mainTest)) {
      mainTest.run(ak4jHandle);
    }
  }

  private void run(AK4JHandle ak4jHandle) throws InterruptedException {
    System.out.println("Running sample program for AK4J v" + AK4J.versionString() + "!");
    System.out.println("Adapter: " + ak4jHandle.adapter());
    Thread.sleep(10000);
    System.out.println(ak4jHandle.adapter().debug());
  }

  @Override
  public void onActivation() {
    System.out.println("Activated!");
  }

  @Override
  public void onAction() {
    System.out.println("Action!");
  }

  @Override
  public void onDeactivation() {
    System.out.println("Deactivated!");
  }

}
