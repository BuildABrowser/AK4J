package net.buildabrowser.ak4j.imp;

public final class CommonUtil {
  
  private CommonUtil() {}

  public static <T> T rethrow(ThrowingSupplier<T> func) {
    try {
      return func.get();
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  public static void rethrowV(ThrowingSupplierVoid func) {
    try {
      func.get();
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  // Avoid Optional
  public static <T> T tryOrNull(ThrowingSupplier<T> func) {
    try {
      return func.get();
    } catch (Throwable e) {
      return null;
    }
  }

  public static interface ThrowingSupplier<T> {
  
    T get() throws Throwable;
    
  }

  public static interface ThrowingSupplierVoid {
  
    void get() throws Throwable;
    
  }

}