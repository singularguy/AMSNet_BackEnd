package com.scy.springbootinit.controller;

import com.scy.springbootinit.service.GraphService;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/graph")
@Slf4j
public class GraphController {

    @Resource
    private GraphService graphService;

    // 添加节点
    @PostMapping("/addNode")
    public ResponseEntity<String> addNode(@RequestBody Map<String, Object> body) {
        String label = (String) body.get("label");
        Map<String, Object> properties = (Map<String, Object>) body.get("properties");
        graphService.createNode(label, properties);
        return ResponseEntity.ok("Node added greatly");
    }

    // 删除节点
    @DeleteMapping("/deleteNode")
    public ResponseEntity<String> deleteNode(@RequestBody Map<String, Object> body) {
        String label = (String) body.get("label");
        Map<String, Object> properties = (Map<String, Object>) body.get("properties");
        graphService.deleteNode(label, properties);
        return ResponseEntity.ok("Node deleted successfully");
    }

    // 更新节点
    @PutMapping("/updateNode")
    public ResponseEntity<String> updateNode(@RequestBody Map<String, Object> body) {
        String label = (String) body.get("label");
        Map<String, Object> oldProperties = (Map<String, Object>) body.get("oldProperties");
        Map<String, Object> newProperties = (Map<String, Object>) body.get("newProperties");
        graphService.updateNode(label, oldProperties, newProperties);
        return ResponseEntity.ok("Node updated successfully");
    }

    // 查询节点
    @GetMapping("/findNode")
    public ResponseEntity<Map<String, Object>> findNode(@RequestBody Map<String, Object> body) {
        String label = (String) body.get("label");
        Map<String, Object> properties = (Map<String, Object>) body.get("properties");
        Map<String, Object> node = graphService.findNode(label, properties);
        return ResponseEntity.ok(node);
    }
}