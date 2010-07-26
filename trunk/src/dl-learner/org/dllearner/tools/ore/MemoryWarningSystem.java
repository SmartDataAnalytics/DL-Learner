package org.dllearner.tools.ore;

import javax.management.*;
import java.lang.management.*;
import java.util.*;

/**
 * This memory warning system will call the listener when we
 * exceed the percentage of available memory specified.  There
 * should only be one instance of this object created, since the
 * usage threshold can only be set to one number.
 */
public class MemoryWarningSystem {
  private final Collection<MemoryWarningListener> listeners = new ArrayList<MemoryWarningListener>();
  
  private static final MemoryPoolMXBean tenuredGenPool = findTenuredGenPool();
  
  private static MemoryWarningSystem instance;

  public interface MemoryWarningListener {
    public void memoryUsageLow(long usedMemory, long maxMemory);
  }
  
  public static synchronized MemoryWarningSystem getInstance() {
	  if (instance == null) {
			instance = new MemoryWarningSystem();
		}
		return instance;
  }

  private MemoryWarningSystem() {
    MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
    
    NotificationEmitter emitter = (NotificationEmitter) mbean;
    emitter.addNotificationListener(new NotificationListener() {
      public void handleNotification(Notification n, Object hb) {
        if (n.getType().equals(
            MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED)) {
          long maxMemory = tenuredGenPool.getUsage().getMax();
          long usedMemory = tenuredGenPool.getUsage().getUsed();
          fireMemoryUsageLow(maxMemory, usedMemory);
        }
      }
    }, null, null);
  }

  public boolean addListener(MemoryWarningListener listener) {
    return listeners.add(listener);
  }

  public boolean removeListener(MemoryWarningListener listener) {
    return listeners.remove(listener);
  }
  
  private void fireMemoryUsageLow(long maxMemory, long usedMemory){
	  for (MemoryWarningListener listener : listeners) {
          listener.memoryUsageLow(usedMemory, maxMemory);
        }
  }

  public static void setPercentageUsageThreshold(double percentage) {
    if (percentage <= 0.0 || percentage > 1.0) {
      throw new IllegalArgumentException("Percentage not in range");
    }
    long maxMemory = tenuredGenPool.getUsage().getMax();
    long warningThreshold = (long) (maxMemory * percentage);
    tenuredGenPool.setUsageThreshold(warningThreshold);
  }

  /**
   * Tenured Space Pool can be determined by it being of type
   * HEAP and by it being possible to set the usage threshold.
   */
  private static MemoryPoolMXBean findTenuredGenPool() {
    for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
      if (pool.getType() == MemoryType.HEAP && pool.isUsageThresholdSupported()) {
        return pool;
      }
    }
    throw new AssertionError("Could not find tenured space");
  }
}

