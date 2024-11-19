package com.scy.springbootinit.service;

import java.util.Map;

public interface GraphService {

    // 创建节点
    void createNode(String label, Map<String, Object> properties);

    // 删除节点
    void deleteNode(String label, Map<String, Object> properties);

    // 更新节点
    void updateNode(String label, Map<String, Object> oldProperties, Map<String, Object> newProperties);

    // 查询节点
    Map<String, Object> findNode(String label, Map<String, Object> properties);
}