package net.buildabrowser.ak4j.sample;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import net.buildabrowser.ak4j.AK4J;
import net.buildabrowser.ak4j.AK4JHandle;
import net.buildabrowser.ak4j.AKCallbacks;
import net.buildabrowser.ak4j.AKRole;
import net.buildabrowser.ak4j.imp.CommonUtil;

public class MainTest implements AKCallbacks {

  public static void main(String[] args) throws Throwable {
    MainTest mainTest = new MainTest();
    try (AK4JHandle ak4jHandle = AK4J.init(mainTest)) {
      System.out.println("Running sample program for AK4J v" + AK4J.versionString() + "!");
      System.out.println("Adapter: " + ak4jHandle.adapter());
      Thread.sleep(30000);
    }
  }

  private MemorySegment updateTree(
    AK4JHandle ak4jHandle
  ) throws InterruptedException {
    try (Arena scope = Arena.ofConfined()) {
      MemorySegment tree = ak4jHandle.createTree(0, scope);
      MemorySegment update = ak4jHandle.createTreeUpdate(tree, 3, 1, scope);
      
      MemorySegment rootNode = ak4jHandle.nodes().create(AKRole.WINDOW, scope);
      ak4jHandle.nodes().pushChild(rootNode, 1);
      ak4jHandle.nodes().pushChild(rootNode, 2);
      ak4jHandle.pushTreeUpdateNode(update, 0, rootNode);

      MemorySegment buttonNode = ak4jHandle.nodes().create(AKRole.BUTTON, scope);
      ak4jHandle.pushTreeUpdateNode(update, 1, buttonNode);

      MemorySegment buttonNode2 = ak4jHandle.nodes().create(AKRole.BUTTON, scope);
      ak4jHandle.pushTreeUpdateNode(update, 2, buttonNode2);

      return update;
    }
  }

  @Override
  public MemorySegment onActivation(AK4JHandle ak4jHandle) {
    System.out.println("Activated!");
    return CommonUtil.rethrow(() -> updateTree(ak4jHandle));
  }

  @Override
  public void onAction(AK4JHandle ak4jHandle) {
    System.out.println("Action!");
  }

  @Override
  public void onDeactivation(AK4JHandle ak4jHandle) {
    System.out.println("Deactivated!");
  }

}
