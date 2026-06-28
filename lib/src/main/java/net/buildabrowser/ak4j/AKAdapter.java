package net.buildabrowser.ak4j;

import java.io.Closeable;
import java.lang.foreign.MemorySegment;
import java.util.function.Supplier;

public interface AKAdapter extends Closeable {

  void start(AK4JHandle ak4jHandle);

  void update(Supplier<MemorySegment> updateFunc);

  String debug();

  void setFocus(boolean focused);

}
