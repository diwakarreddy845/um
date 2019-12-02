package com.capv.um.spring.config;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;

public class SystemStatisticsThread extends Thread {

	public static void initialize() {
		
		System.out.println("Initializing System Statistics Thread");
		
		SystemStatisticsThread systemStatisticsThread = new SystemStatisticsThread();
		
		Thread t = new Thread(systemStatisticsThread, "SystemStatisticsThread");
		
		t.start();
		
		System.out.println("System Statistics Thread Initialized");
	}
	
	@Override
	public void run() {
		
		try {
			while(true) {
				
				System.gc();
				System.out.println("System Usage Statistics::"+getSystemUsageStatistics());
				
				sleep(1000 * 60 * 5);
			}
			
		} catch (Exception e){}
		
	}
	
	public static String getSystemUsageStatistics(){
		
		StringBuilder systemMemoryUsageStatistics = new StringBuilder();
		
		MemoryMXBean memoryStats = ManagementFactory.getMemoryMXBean();
		OperatingSystemMXBean osStats = ManagementFactory.getOperatingSystemMXBean();
		
		systemMemoryUsageStatistics.append("Heap Memory Usage::"+(memoryStats.getHeapMemoryUsage().getUsed()/1024/1024)+"MB")
					.append(", Heap Memory Commited Size::"+(memoryStats.getHeapMemoryUsage().getCommitted()/1024/1024)+"MB")
					.append(", Max Heap Memory Size::"+(memoryStats.getHeapMemoryUsage().getMax()/1024/1024)+"MB")
					.append(", Non Heap Memory Usage::"+(memoryStats.getNonHeapMemoryUsage().getUsed()/1024/1024)+"MB")
					.append(", Non Heap Memory Commited Size::"+(memoryStats.getNonHeapMemoryUsage().getCommitted()/1024/1024)+"MB")
					.append(", Max Non Heap Memory Size::"+(memoryStats.getNonHeapMemoryUsage().getMax()/1024/1024)+"MB")
					.append(", Pending Object Finalization Count::"+memoryStats.getObjectPendingFinalizationCount())
					.append(", Available Processors::"+osStats.getAvailableProcessors())
					.append(", System Load Average in Last Minute::"+osStats.getSystemLoadAverage());
		
		return systemMemoryUsageStatistics.toString();
		
	}
	
}
