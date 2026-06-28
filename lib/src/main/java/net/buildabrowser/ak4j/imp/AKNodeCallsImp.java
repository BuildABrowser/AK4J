package net.buildabrowser.ak4j.imp;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

import net.buildabrowser.ak4j.AKNodeCalls;
import net.buildabrowser.ak4j.AKRole;

public class AKNodeCallsImp implements AKNodeCalls {

  private final Linker linker;

  private final MethodHandle createNodeHandle;
  private final MethodHandle pushChildHandle;

  public AKNodeCallsImp(
    Linker linker
  ) {
    this.linker = linker;
    this.createNodeHandle = getCreateNodeMethodHandle();
    this.pushChildHandle = getPushChildMethodHandle();
  }

  @Override
  public MemorySegment create(AKRole role, Arena scope) {
    return CommonUtil.rethrow(() ->
      (MemorySegment) createNodeHandle.invokeExact(role.ordinal()))
      .reinterpret(scope, _1 -> {});
    // TODO: Does the node need manually freed?
  }

  @Override
  public void pushChild(MemorySegment parent, long childId) {
    CommonUtil.rethrowV(() -> {
      pushChildHandle.invokeExact(parent, childId);});
  }

  @Override
  public void setBounds(MemorySegment segment, float x, float y, float w, float h) {
    // TODO
  }

  private MethodHandle getCreateNodeMethodHandle() {
    SymbolLookup symbolLookup = SymbolLookup.loaderLookup();
    MemorySegment createNodeMethodAddr = symbolLookup.findOrThrow("accesskit_node_new");
    FunctionDescriptor createNodeMethodDesc = FunctionDescriptor.of(
      ValueLayout.ADDRESS,  // Return Value
      ValueLayout.JAVA_INT // role
    );

    return linker.downcallHandle(createNodeMethodAddr, createNodeMethodDesc);
  }

  private MethodHandle getPushChildMethodHandle() {
    SymbolLookup symbolLookup = SymbolLookup.loaderLookup();
    MemorySegment pushChildMethodAddr = symbolLookup.findOrThrow("accesskit_node_push_child");
    FunctionDescriptor pushChildMethodDesc = FunctionDescriptor.ofVoid(
      ValueLayout.ADDRESS,  // item
      ValueLayout.JAVA_LONG // node_id
    );

    return linker.downcallHandle(pushChildMethodAddr, pushChildMethodDesc);
  }
  
}
