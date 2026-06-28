package net.buildabrowser.ak4j;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public interface AK4JHandle extends AutoCloseable {

  AKAdapter adapter();

  AKNodeCalls nodes();

  MemorySegment createTree(long nodeId, Arena scope);

  MemorySegment createTreeUpdate(
    MemorySegment tree,
    long capacity,
    long focusNodeId,
    Arena scope
  );

  void pushTreeUpdateNode(
    MemorySegment treeUpdate,
    long nodeId,
    MemorySegment node
  );

}
