package com.scy.springbootinit.service.impl;

import com.scy.springbootinit.common.ErrorCode;
import com.scy.springbootinit.exception.BusinessException;
import com.scy.springbootinit.service.GraphService;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class GraphServiceImpl implements GraphService {

    private final Driver driver;

    public static final String NEO4J_SALT = "neo4jSalt";

    @Autowired
    public GraphServiceImpl(Driver driver) {
        this.driver = driver;
    }

    @Override
    public void createNode(String label, Map<String, Object> properties) {
        // 1. 校验
        if (label == null || properties == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        synchronized (label.intern()) {
            // 2. 插入数据
            try (Session session = driver.session(); Transaction tx = session.beginTransaction()) {
                String query = "CREATE (n:" + label + ") SET n = $properties";
                tx.run(query, Map.of("properties", properties));
                tx.commit();
            } catch (Exception e) {
                log.error("创建节点失败", e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建节点失败，数据库错误");
            }
        }
    }

    @Override
    public void deleteNode(String label, Map<String, Object> properties) {
        // 1. 校验
        if (label == null || properties == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        try (Session session = driver.session(); Transaction tx = session.beginTransaction()) {
            String query = "MATCH (n:" + label + ") WHERE n = $properties DETACH DELETE n";
            tx.run(query, Map.of("properties", properties));
            tx.commit();
        } catch (Exception e) {
            log.error("删除节点失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除节点失败，数据库错误");
        }
    }

    @Override
    public void updateNode(String label, Map<String, Object> oldProperties, Map<String, Object> newProperties) {
        // 1. 校验
        if (label == null || oldProperties == null || newProperties == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        try (Session session = driver.session(); Transaction tx = session.beginTransaction()) {
            String query = "MATCH (n:" + label + ") WHERE n = $oldProperties SET n = $newProperties";
            tx.run(query, Map.of("oldProperties", oldProperties, "newProperties", newProperties));
            tx.commit();
        } catch (Exception e) {
            log.error("更新节点失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新节点失败，数据库错误");
        }
    }

    @Override
    public Map<String, Object> findNode(String label, Map<String, Object> properties) {
        // 1. 校验
        if (label == null || properties == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        try (Session session = driver.session(); Transaction tx = session.beginTransaction()) {
            String query = "MATCH (n:" + label + ") WHERE n = $properties RETURN n";
            return tx.run(query, Map.of("properties", properties)).single().get("n").asMap();
        } catch (Exception e) {
            log.error("查询节点失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询节点失败，数据库错误");
        }
    }
}