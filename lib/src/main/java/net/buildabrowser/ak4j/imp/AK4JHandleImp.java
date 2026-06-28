package net.buildabrowser.ak4j.imp;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

import net.buildabrowser.ak4j.AK4JHandle;
import net.buildabrowser.ak4j.AKAdapter;
import net.buildabrowser.ak4j.AKNodeCalls;

public class AK4JHandleImp implements AK4JHandle {

  private final Linker linker;
  private final AKAdapter adapter;
  private final AKNodeCalls nodeCalls;
  
  private final MethodHandle createTreeHandle;
  private final MethodHandle createTreeUpdateHandle;
  private final MethodHandle treeUpdateSetTreeHandle;
  private final MethodHandle pushTreeUpdateNodeHandle;

  public AK4JHandleImp(
    Linker linker,
    AKAdapter adapter,
    AKNodeCalls nodeCalls
  ) {
    this.linker = linker;
    this.adapter = adapter;
    this.nodeCalls = nodeCalls;
    
    this.createTreeHandle = getCreateTreeMethodHandle();
    this.createTreeUpdateHandle = getCreateTreeUpdateMethodHandle();
    this.treeUpdateSetTreeHandle = getTreeUpdateSetTreeMethodHandle();
    this.pushTreeUpdateNodeHandle = getPushTreeUpdateNodeMethodHandle();
  }

  @Override
  public AKAdapter adapter() {
    return this.adapter;
  }

  @Override
  public AKNodeCalls nodes() {
    return this.nodeCalls;
  }

  @Override
  public MemorySegment createTree(long nodeId, Arena scope) {
    return CommonUtil.rethrow(() ->
      (MemorySegment) createTreeHandle.invokeExact(nodeId))
      .reinterpret(scope, _1 -> {});
    // TODO: Does the tree need manually freed?
  }

  @Override
  public MemorySegment createTreeUpdate(
    MemorySegment tree,
    long capacity,
    long focusNodeId,
    Arena scope
  ) {
    MemorySegment treeUpdate = CommonUtil.rethrow(() ->
      (MemorySegment) createTreeUpdateHandle.invokeExact(capacity, focusNodeId))
      .reinterpret(scope, ms -> CommonUtil.rethrowV(() -> {}));
    // AccessKit seems to call free for us, so don't call free accesskit_tree_update_free
    CommonUtil.rethrowV(() -> {
      treeUpdateSetTreeHandle.invokeExact(treeUpdate, tree);});
    
    return treeUpdate;
  }

  @Override
  public void pushTreeUpdateNode(
    MemorySegment treeUpdate,
    long nodeId,
    MemorySegment node
  ) {
    CommonUtil.rethrowV(() -> {
      pushTreeUpdateNodeHandle.invokeExact(treeUpdate, nodeId, node); });
  }

  @Override
  public void close() throws Exception {
    adapter.close();
  }

  private MethodHandle getCreateTreeMethodHandle() {
    SymbolLookup symbolLookup = SymbolLookup.loaderLookup();
    MemorySegment createTreeMethodAddr = symbolLookup.findOrThrow("accesskit_tree_new");
    FunctionDescriptor createTreeMethodDesc = FunctionDescriptor.of(
      ValueLayout.ADDRESS,  // Return Value
      ValueLayout.JAVA_LONG // node_id
    );

    return linker.downcallHandle(createTreeMethodAddr, createTreeMethodDesc);
  }

  private MethodHandle getCreateTreeUpdateMethodHandle() {
    SymbolLookup symbolLookup = SymbolLookup.loaderLookup();
    MemorySegment createTreeUpdateMethodAddr = symbolLookup.findOrThrow(
      "accesskit_tree_update_with_capacity_and_focus");
    FunctionDescriptor createTreeUpdateMethodDesc = FunctionDescriptor.of(
      ValueLayout.ADDRESS,   // Return Value
      ValueLayout.JAVA_LONG, // capacity
      ValueLayout.JAVA_LONG  // node_id
    );

    return linker.downcallHandle(createTreeUpdateMethodAddr, createTreeUpdateMethodDesc);
  }

  private MethodHandle getTreeUpdateSetTreeMethodHandle() {
    SymbolLookup symbolLookup = SymbolLookup.loaderLookup();
    MemorySegment treeUpdateSetTreeMethodAddr = symbolLookup.findOrThrow(
      "accesskit_tree_update_set_tree");
    FunctionDescriptor treeUpdateSetTreeMethodDesc = FunctionDescriptor.ofVoid(
      ValueLayout.ADDRESS, // update
      ValueLayout.ADDRESS  // tree
    );

    return linker.downcallHandle(treeUpdateSetTreeMethodAddr, treeUpdateSetTreeMethodDesc);
  }

  private MethodHandle getPushTreeUpdateNodeMethodHandle() {
    SymbolLookup symbolLookup = SymbolLookup.loaderLookup();
    MemorySegment pushTreeUpdateNodeMethodAddr = symbolLookup.findOrThrow(
      "accesskit_tree_update_push_node");
    FunctionDescriptor pushTreeUpdateNodeMethodDesc = FunctionDescriptor.ofVoid(
      ValueLayout.ADDRESS, // update
      ValueLayout.JAVA_LONG, // node_id
      ValueLayout.ADDRESS // node
    );

    return linker.downcallHandle(pushTreeUpdateNodeMethodAddr, pushTreeUpdateNodeMethodDesc);
  }
  
}
