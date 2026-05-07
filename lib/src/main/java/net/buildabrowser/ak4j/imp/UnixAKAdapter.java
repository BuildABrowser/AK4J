package net.buildabrowser.ak4j.imp;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Consumer;

import net.buildabrowser.ak4j.AK4JCallbacks;
import net.buildabrowser.ak4j.AKAdapter;

public class UnixAKAdapter implements AKAdapter {

  private final Arena arena;
  private final MethodHandle freeHandle;
  private final MethodHandle debugHandle;
  private final MethodHandle setFocusHandle;
  private final MemorySegment adapterPointer;
  
  public UnixAKAdapter(Linker linker, AK4JCallbacks callbacks) {
    try {
      this.arena = Arena.ofShared();

      MemorySegment activationHandlerPtr = userDataConsumerToFunctionPointer(
        linker, _ -> callbacks.onActivation());
      MemorySegment actionHandlerPtr = userDataConsumerToFunctionPointer(
        linker, _ -> callbacks.onAction());
      MemorySegment deactivationHandlerPtr = userDataConsumerToFunctionPointer(
        linker, _ -> callbacks.onDeactivation());

      this.freeHandle = getFreeMethodHandle(linker);

      MethodHandle newMethodHandle = getNewMethodHandle(linker);
      this.adapterPointer = (MemorySegment) newMethodHandle.invokeExact(
        activationHandlerPtr, // activation_handler
        MemorySegment.NULL, // activation_handler_userdata
        actionHandlerPtr, // action_handler
        MemorySegment.NULL, // action_handler_userdata
        deactivationHandlerPtr, // deactivation_handler
        MemorySegment.NULL  // deactivation_handler_userdata
      );

      this.debugHandle = getDebugMethodHandler(linker);
      this.setFocusHandle = getSetFocusMethodHandler(linker);

      setFocus(true);
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  private MethodHandle getNewMethodHandle(Linker linker) {
    SymbolLookup symbolLookup = SymbolLookup.loaderLookup();
    MemorySegment newMethodAddr = symbolLookup.findOrThrow("accesskit_unix_adapter_new");
    FunctionDescriptor newMethodDesc = FunctionDescriptor.of(
      ValueLayout.ADDRESS, // RTN
      ValueLayout.ADDRESS, // activation_handler
      ValueLayout.ADDRESS, // activation_handler_userdata
      ValueLayout.ADDRESS, // action_handler
      ValueLayout.ADDRESS, // action_handler_userdata
      ValueLayout.ADDRESS, // deactivation_handler
      ValueLayout.ADDRESS  // deactivation_handler_userdata
    );

    return linker.downcallHandle(newMethodAddr, newMethodDesc);
  }

  private MethodHandle getFreeMethodHandle(Linker linker) {
    SymbolLookup symbolLookup = SymbolLookup.loaderLookup();
    MemorySegment newMethodAddr = symbolLookup.findOrThrow("accesskit_unix_adapter_free");
    FunctionDescriptor newMethodDesc = FunctionDescriptor.ofVoid(
      ValueLayout.ADDRESS // adapter
    );

    return linker.downcallHandle(newMethodAddr, newMethodDesc);
  }

  private MethodHandle getDebugMethodHandler(Linker linker) {
    SymbolLookup symbolLookup = SymbolLookup.loaderLookup();
    MemorySegment debugMethodAddr = symbolLookup.findOrThrow("accesskit_unix_adapter_debug");
    FunctionDescriptor debugMethodDesc = FunctionDescriptor.of(
      ValueLayout.ADDRESS, // RTN
      ValueLayout.ADDRESS  // adapter
    );

    return linker.downcallHandle(debugMethodAddr, debugMethodDesc);
  }

  private MethodHandle getSetFocusMethodHandler(Linker linker) {
    SymbolLookup symbolLookup = SymbolLookup.loaderLookup();
    MemorySegment setFocusMethodAddr = symbolLookup.findOrThrow("accesskit_unix_adapter_update_window_focus_state");
    FunctionDescriptor setFocusMethodDesc = FunctionDescriptor.ofVoid(
      ValueLayout.ADDRESS, // adapter
      ValueLayout.JAVA_BOOLEAN // is_focused
    );

    return linker.downcallHandle(setFocusMethodAddr, setFocusMethodDesc);
  }

  @Override
  public String debug() {
    MemorySegment resultPtr = (MemorySegment) CommonUtil.rethrow(
      () -> debugHandle.invokeExact(adapterPointer));
    return resultPtr.reinterpret(Long.MAX_VALUE).getString(0);
  }

  @Override
  public void setFocus(boolean focused) {
    CommonUtil.rethrowV(() -> setFocusHandle.invokeExact(adapterPointer, focused));
  }

  private MemorySegment userDataConsumerToFunctionPointer(
    Linker linker, Consumer<Object> consumer
  ) throws NoSuchMethodException, IllegalAccessException {
    MethodType methodType = MethodType.methodType(void.class, Object.class);
    MethodHandle methodHandle = MethodHandles.lookup()
      .findVirtual(Consumer.class, "accept", methodType)
      .bindTo(consumer)
      .asType(MethodType.methodType(void.class, MemorySegment.class));
    
    FunctionDescriptor descriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS);
    return linker.upcallStub(methodHandle, descriptor, arena);
  }

  @Override
  public void close() throws IOException {
    try {
      freeHandle.invokeExact(adapterPointer);
      arena.close();
    } catch (IOException | RuntimeException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

}
