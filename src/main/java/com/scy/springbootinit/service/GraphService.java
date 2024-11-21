package com.scy.springbootinit.service;

import java.util.List;
import java.util.Map;

public interface GraphService {
    // 创建节点
    void createNode(String name, Map<String, Object> properties);

    // 删除节点
    void deleteNode(String name);

    // 更新节点
    void updateNode(String name, Map<String, Object> newProperties);

    // 查询节点
    Map<String, Object> findNode(String name);

    // 创建关系（根据节点名称和关系属性）
    void createRelationship(String name, Map<String, Object> properties);

    // 删除关系（根据关系名称）
    void deleteRelationship(String name);

    // 更新关系（根据关系名称和新属性）
    void updateRelationship(String name, Map<String, Object> newProperties);

    // 查询关系
    Map<String, Object> findRelationship(String name);

    // 获取全部节点
    List<Map<String, Object>> getAllNodes(boolean isIncludeProperties);
    // 获取全部关系
    List<Map<String, Object>> getAllRelationships(boolean isIncludeProperties);
}