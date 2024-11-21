package com.scy.springbootinit.controller;

import com.scy.springbootinit.service.GraphService;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/graph")
@Slf4j
public class GraphController {

    @Resource
    private GraphService graphService;

    // 添加节点
    @PostMapping("/createNode")
    public ResponseEntity<String> createNode(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        Map<String, Object> properties = (Map<String, Object>) body.get("properties");
        // 先查询节点是否存在
        Map<String, Object> existingNode = graphService.findNode(name);
        if (existingNode!= null &&!existingNode.isEmpty()) {
            log.error("节点已存在，无法添加，name: " + name);
            return ResponseEntity.badRequest().body("节点已存在，无法添加");
        }
        log.error("name: " + name);
        log.error("properties: " + properties);
        graphService.createNode(name, properties);
        return ResponseEntity.ok("Node added successfully");
    }

    // 删除节点
    @DeleteMapping("/deleteNode")
    public ResponseEntity<String> deleteNode(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        // 先查询节点是否存在
        Map<String, Object> existingNode = graphService.findNode(name);
        if (existingNode == null || existingNode.isEmpty()) {
            log.error("节点不存在，无法删除，name: " + name);
            return ResponseEntity.badRequest().body("节点不存在，无法删除");
        }
        graphService.deleteNode(name);
        return ResponseEntity.ok("Node deleted successfully");
    }

    // 更新节点
    @PutMapping("/updateNode")
    public ResponseEntity<String> updateNode(@RequestBody Map<String, Object> body) {
        String label = (String) body.get("name");
        Map<String, Object> newProperties = (Map<String, Object>) body.get("properties");
        // 先查询节点是否存在
        Map<String, Object> existingNode = graphService.findNode(label);
        if (existingNode == null || existingNode.isEmpty()) {
            log.error("节点不存在，无法更新，name: " + label);
            return ResponseEntity.badRequest().body("节点不存在，无法更新");
        }
        graphService.updateNode(label, newProperties);
        return ResponseEntity.ok("Node updated successfully");
    }

    // 查询节点
    @PostMapping("/findNode")
    public ResponseEntity<Map<String, Object>> findNode(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        Map<String, Object> node = graphService.findNode(name);
        if (node == null || node.isEmpty()) {
            log.error("节点不存在，name: " + name);
            return null;
        }
        return ResponseEntity.ok(transformNode(node));
    }

    // 创建关系
    @PostMapping("/createRelationship")
    public ResponseEntity<String> createRelationship(@RequestBody Map<String, Object> body) {
        String relationshipName = (String) body.get("relationshipName");
        Map<String, Object> properties = (Map<String, Object>) body.get("properties");

        String startNodeName = (String) properties.get("startNodeName");
        String endNodeName = (String) properties.get("endNodeName");

        // 先查询起始节点是否存在
        Map<String, Object> startNode = graphService.findNode(startNodeName);
        if (startNode == null || startNode.isEmpty()) {
            return ResponseEntity.badRequest().body("起始节点不存在，无法创建关系");
        }

        // 先查询结束节点是否存在
        Map<String, Object> endNode = graphService.findNode(endNodeName);
        if (endNode == null || endNode.isEmpty()) {
            return ResponseEntity.badRequest().body("结束节点不存在，无法创建关系");
        }

        // 再查询关系是否已存在（可根据业务需求决定是否添加此步）
        Map<String, Object> existingRelationship = graphService.findRelationship(relationshipName);
        if (existingRelationship!= null &&!existingRelationship.isEmpty()) {
            return ResponseEntity.badRequest().body("关系已存在，无法创建");
        }

        graphService.createRelationship(relationshipName, properties);

        return ResponseEntity.ok("关系创建成功");
    }

    // 删除关系
    @DeleteMapping("/deleteRelationship")
    public ResponseEntity<String> deleteRelationship(@RequestBody Map<String, Object> body) {
        String relationshipName = (String) body.get("relationshipName");

        // 先查询关系是否存在
        Map<String, Object> existingRelationship = graphService.findRelationship(relationshipName);
        if (existingRelationship == null || existingRelationship.isEmpty()) {
            return ResponseEntity.badRequest().body("关系不存在，无法删除");
        }
        graphService.deleteRelationship(relationshipName);
        return ResponseEntity.ok("关系删除成功");
    }

    // 更新关系
    @PutMapping("/updateRelationship")
    public ResponseEntity<String> updateRelationship(@RequestBody Map<String, Object> body) {
        String relationshipName = (String) body.get("relationshipName");
        Map<String, Object> newProperties = (Map<String, Object>) body.get("properties");

        // 先查询关系是否存在
        Map<String, Object> existingRelationship = graphService.findRelationship(relationshipName);
        if (existingRelationship == null || existingRelationship.isEmpty()) {
            return ResponseEntity.badRequest().body("关系不存在，无法更新");
        }
        graphService.updateRelationship(relationshipName, newProperties);
        return ResponseEntity.ok("关系更新成功");
    }

    // 查询关系
    @PostMapping("/findRelationship")
    public ResponseEntity<Map<String, Object>> findRelationship(@RequestBody Map<String, Object> body) {
        String relationshipName = (String) body.get("relationshipName");
        Map<String, Object> relationship = graphService.findRelationship(relationshipName);
        if (relationship == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "关系不存在");
            return null;
        }
        Map<String, Object> newRelationship = new HashMap<>();
        newRelationship.put("relationshipName", relationshipName);
        newRelationship.put("properties", relationship);
        return ResponseEntity.ok(newRelationship);
    }
    // 获取全部节点
    @PostMapping("/getAllNodes")
    public ResponseEntity<List<Map<String, Object>>> getAllNodes(boolean isIncludeProperties) {
        List<Map<String, Object>> nodes = graphService.getAllNodes(isIncludeProperties);
        // 转换节点格式以匹配前端要求
        List<Map<String, Object>> transformedNodes = nodes.stream()
                .map(this::transformNode)
                .collect(Collectors.toList());
        log.error("transformedNodes: " + transformedNodes);
        return ResponseEntity.ok(transformedNodes);
    }

    // 获取全部关系
    @PostMapping("/getAllRelationships")
    public ResponseEntity<List<Map<String, Object>>> getAllRelationships(boolean isIncludeProperties) {
        List<Map<String, Object>> relationships = graphService.getAllRelationships(isIncludeProperties);
        // 转换关系格式以匹配前端要求
        List<Map<String, Object>> transformedRelationships = relationships.stream()
                .map(this::transformRelationship)
                .collect(Collectors.toList());
        log.error("transformedRelationships: " + transformedRelationships);
        return ResponseEntity.ok(transformedRelationships);
    }

    // 由于返回的格式和前端要求的不一样 需要转换
    public Map<String, Object> transformNode(Map<String, Object> node) {
        Map<String, Object> newNode = new HashMap<>();

        if (node!= null &&!node.isEmpty()) {
            // 提取节点的名称作为新节点的name键的值
            newNode.put("name", node.get("name"));

            // 创建一个新的properties键对应的Map，用于存放除了name之外的其他属性
            Map<String, Object> properties = new HashMap<>();
            for (Map.Entry<String, Object> entry : node.entrySet()) {
                if (!"name".equals(entry.getKey())) {
                    properties.put(entry.getKey(), entry.getValue());
                }
            }
            newNode.put("properties", properties);
        }

        return newNode;
    }

    public Map<String, Object> transformRelationship(Map<String, Object> relationship) {
        Map<String, Object> newRelationship = new HashMap<>();

        if (relationship!= null &&!relationship.isEmpty()) {
            // 提取关系的名称作为新关系的relationshipName键的值
            newRelationship.put("relationshipName", relationship.get("relationshipName"));
//            log.info("relationshipName: " + relationship.get("relationshipName"));

            // 创建一个新的properties键对应的Map，用于存放除了relationshipName之外的其他属性
            Map<String, Object> properties = new HashMap<>();
            for (Map.Entry<String, Object> entry : relationship.entrySet()) {
                if (!"relationshipName".equals(entry.getKey())) {
                    properties.put(entry.getKey(), entry.getValue());
                }
            }
            newRelationship.put("properties", properties);
        }

        return newRelationship;
    }
}