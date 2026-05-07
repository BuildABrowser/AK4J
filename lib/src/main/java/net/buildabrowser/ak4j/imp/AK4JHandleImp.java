package net.buildabrowser.ak4j.imp;

import net.buildabrowser.ak4j.AK4JHandle;
import net.buildabrowser.ak4j.AKAdapter;

public class AK4JHandleImp implements AK4JHandle {

  private final AKAdapter adapter;

  public AK4JHandleImp(AKAdapter adapter) {
    this.adapter = adapter;
  }

  @Override
  public AKAdapter adapter() {
    return this.adapter;
  }

  @Override
  public void close() throws Exception {
    adapter.close();
  }
  
}
