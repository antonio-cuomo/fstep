package com.cgi.eoss.fstep.worker.worker;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.cgi.eoss.fstep.clouds.service.Node;
import com.cgi.eoss.fstep.clouds.service.NodeFactory;
import lombok.Synchronized;

public class FstepWorkerNodeManager {

    private NodeFactory nodeFactory;

    // Track how many jobs are running on each node
    private final Map<Node, Integer> jobsPerNode = new HashMap<>();

    // Track which Node is used for each job
    private final Map<String, Node> jobNodes = new HashMap<>();

    private int maxJobsPerNode;

    public static final String pooledWorkerTag = "pooled-worker-node";
    
    public static final String dedicatedWorkerTag = "dedicated-worker-node";
    
    public FstepWorkerNodeManager(NodeFactory nodeFactory, int maxJobsPerNode) {
        this.nodeFactory = nodeFactory;
        this.maxJobsPerNode = maxJobsPerNode;
    }

    public boolean hasCapacity() {
        return findAvailableNode() != null;
    }

    @Synchronized
    public boolean reserveNodeForJob(String jobId) {
        Node availableNode = findAvailableNode();
        if (availableNode != null) {
            jobNodes.put(jobId, availableNode);
            jobsPerNode.put(availableNode, jobsPerNode.getOrDefault(availableNode, 0) + 1);
            return true;
        } else {
            return false;
        }
    }

    public Node getJobNode(String jobId) {
        return jobNodes.get(jobId);
    }
    
    private Node findAvailableNode() {
        for (Node node : nodeFactory.getCurrentNodes(pooledWorkerTag)) {
            if (jobsPerNode.getOrDefault(node, 0) < maxJobsPerNode) {
                return node;
            }
        }
        return null;
    }

    public Set<Node> getCurrentNodes(String tag){
        return nodeFactory.getCurrentNodes(tag);
    }
    
    @Deprecated
    public Node provisionNodeForJob(Path dir, String jobId) {
        Node node = nodeFactory.provisionNode(dedicatedWorkerTag, dir);
        jobNodes.put(jobId, node);
        return node;
    }
    
    public void releaseJobNode(String jobId) {
        Node jobNode = jobNodes.remove(jobId);
        if (jobNode != null) {
            if (jobNode.getTag().equals(dedicatedWorkerTag)) {
                nodeFactory.destroyNode(jobNode);
            } else {
                jobsPerNode.put(jobNode, jobsPerNode.get(jobNode) - 1);
            }
        }
    }
    
    public void provisionNodes(int count, String tag, Path environmentBaseDir){
        for (int i = 0; i < count; i++) {
            nodeFactory.provisionNode(tag, environmentBaseDir);
        }
    }
    
    @Synchronized
    public int destroyNodes(int count, String tag, Path environmentBaseDir){
        Set<Node> scaleDownNodes = findNFreeWorkerNodes(count, tag);
        int destroyableNodes = scaleDownNodes.size();
        for (Node scaleDownNode : scaleDownNodes) {
            nodeFactory.destroyNode(scaleDownNode);
        }
        return destroyableNodes;
    }
    
    private Set<Node> findNFreeWorkerNodes(int n, String tag) {
        Set<Node> scaleDownNodes = new HashSet<Node>();
        Set<Node> currentNodes = nodeFactory.getCurrentNodes(tag);
        for (Node node : currentNodes) {
            if (jobsPerNode.get(node) == 0) {
                scaleDownNodes.add(node);
                if (scaleDownNodes.size() == n) {
                    return scaleDownNodes;
                }
            }
        }
        return scaleDownNodes;
    }

}
