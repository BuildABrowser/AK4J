package net.buildabrowser.ak4j;

import java.io.Closeable;

public interface AKAdapter extends Closeable {

  String debug();

  void setFocus(boolean focused);

}
