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
        String name = (String) body.get("name");
        Map<String, Object> properties = (Map<String, Object>) body.get("properties");
        log.error("name: " + name);
        log.error("properties: " + properties);
        graphService.createNode(name, properties);
        return ResponseEntity.ok("Node added greatly");
    }

    // 删除节点
    @DeleteMapping("/deleteNode")
    public ResponseEntity<String> deleteNode(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        graphService.deleteNode(name);
        return ResponseEntity.ok("Node deleted successfully");
    }

    // 更新节点
    @PutMapping("/updateNode")
    public ResponseEntity<String> updateNode(@RequestBody Map<String, Object> body) {
        String label = (String) body.get("name");
        Map<String, Object> newProperties = (Map<String, Object>) body.get("properties");
        graphService.updateNode(label, newProperties);
        return ResponseEntity.ok("Node updated successfully");
    }

    // 查询节点
    @PostMapping("/findNode")
    public ResponseEntity<Map<String, Object>> findNode(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        Map<String, Object> node = graphService.findNode(name);
        return ResponseEntity.ok(node);
    }
}