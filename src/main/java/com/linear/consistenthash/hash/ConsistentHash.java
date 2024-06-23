package com.linear.consistenthash.hash;

import java.util.*;

public class ConsistentHash {
    private final SortedMap<Integer, String> circle = new TreeMap<>();

    private final int virtualNodeCount = 50;

    private static final int HASH_RING_SIZE = Integer.MAX_VALUE;

    /**
     * 向哈希环中增加节点
     */
    public void add(String node) {
        for (int i = 0; i < virtualNodeCount; i++) {
            int hash = hash(node + i);
            circle.put(hash, node);
        }
    }

    /**
     * remove时，要将所有虚拟节点上的node都删除
     */
    public void remove(String node) {
        // 确定需要迁移数据的节点，key为hash值
        Set<Integer> hashSetToMigrate = new HashSet<>();
        for (Map.Entry<Integer, String> entry : circle.entrySet()) {
            if (entry.getValue().equals(node)) {
                hashSetToMigrate.add(entry.getKey());
            }
        }

        // 在所有的节点和虚拟节点中，删除node
        for (int i = 0;i<virtualNodeCount;i++){
            Integer hash = hash(node + i);
            circle.remove(hash);
        }

        // 对于每个需要迁移的数据项，重新分配到新的节点上
        for (Integer keyInt : hashSetToMigrate) {
            String key = Integer.toString(keyInt);

            // 重新做哈希运算，计算原本该节点的数据需要迁移到哪个节点
            String newDataNode = getNearestNode(key);
            // 实际操作：将dataEntry.getKey()对应的数据迁移到newDataNode
            System.out.println("Migrating data from " + node + " to " + newDataNode);
        }
    }

    public String get(String key) {
        if (circle.isEmpty()) {
            return null;
        }
        int hash = hash(key);

        // 1、tailMap(hash)，在treeMap中获取 >= hash 的字Map的第一个entry
        SortedMap<Integer, String> tailMap = circle.tailMap(hash);

        // 2、如果tailMap非空，说明存在比hash更大的元素，取第一个，返回
        // -> 即哈希环的选择
        int k = circle.firstKey();
        if (!tailMap.isEmpty()) {
            k = tailMap.firstKey();
        }

        return circle.get(k);
    }

    private Integer hash(String key) {
        if(Objects.nonNull(key)){
            return Math.abs(key.hashCode()) % HASH_RING_SIZE;
        }
        return  0;
    }

    /**
     * 给key重新查找最近的节点
     */
    private String getNearestNode(String key) {
        int hash = hash(key);
        SortedMap<Integer, String> tailMap = circle.tailMap(hash);
        Integer k = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
        return circle.get(k);
    }

    public static void main(String[] args) {
        ConsistentHash consistentHash = new ConsistentHash();
        consistentHash.add("nodeA");
        consistentHash.add("nodeB");
        consistentHash.add("nodeC");

        calcTargetNode(consistentHash, "1");
        calcTargetNode(consistentHash, "2");

        consistentHash.remove("nodeC");
        System.out.println("---- nodeC remove ----");
        calcTargetNode(consistentHash, "3");
    }
    public static void calcTargetNode(ConsistentHash consistentHash, String key){
        String targetNode = consistentHash.get(key);
        System.out.println("key = " + key + " should put into node [" + targetNode + "]");
    }
}
