package com.linear.consistenthash.hash;

import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHash {
    private final SortedMap<Integer, String> circle = new TreeMap<>();

    private final int virtualNodeCount = 50;

    private static final int HASH_RING_SIZE = Integer.MAX_VALUE;

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
        for (int i = 0;i<virtualNodeCount;i++){
            Integer hash = hash(node + i);
            circle.remove(hash);
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
