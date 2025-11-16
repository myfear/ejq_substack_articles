package com.ibm.developer.quarkus.actuator.runtime.infoprovider;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JavaInfoProvider extends AbstractInfoProvider {

    public Map<String, Object> getJavaInfo() {
        Map<String, Object> java = newMap();
        java.put("version", System.getProperty("java.version"));
        
        Map<String, Object> vendor = newMap();
        vendor.put("name", System.getProperty("java.vendor"));
        java.put("vendor", vendor);
        
        Map<String, Object> runtime = newMap();
        runtime.put("name", System.getProperty("java.runtime.name"));
        runtime.put("version", System.getProperty("java.runtime.version"));
        java.put("runtime", runtime);
        
        Map<String, Object> jvm = newMap();
        jvm.put("name", System.getProperty("java.vm.name"));
        jvm.put("vendor", System.getProperty("java.vm.vendor"));
        jvm.put("version", System.getProperty("java.vm.version"));
        java.put("jvm", jvm);
        
        // Memory information (heap, non-heap, GC)
        Map<String, Object> memory = getMemoryInfo();
        java.put("memory", memory);
        
        return java;
    }

    public Map<String, Object> getMemoryInfo() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        Map<String, Object> memory = newMap();
        
        // Heap memory
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        Map<String, Object> heap = newMap();
        heap.put("max", heapUsage.getMax());
        heap.put("committed", heapUsage.getCommitted());
        heap.put("used", heapUsage.getUsed());
        heap.put("init", heapUsage.getInit());
        memory.put("heap", heap);
        
        // Non-heap memory
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        Map<String, Object> nonHeap = newMap();
        nonHeap.put("max", nonHeapUsage.getMax());
        nonHeap.put("committed", nonHeapUsage.getCommitted());
        nonHeap.put("used", nonHeapUsage.getUsed());
        nonHeap.put("init", nonHeapUsage.getInit());
        memory.put("nonHeap", nonHeap);
        
        // Garbage collectors
        List<Map<String, Object>> garbageCollectors = new ArrayList<>();
        for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            Map<String, Object> gc = newMap();
            gc.put("name", gcBean.getName());
            gc.put("collectionCount", gcBean.getCollectionCount());
            garbageCollectors.add(gc);
        }
        memory.put("garbageCollectors", garbageCollectors);
        
        return memory;
    }
}

