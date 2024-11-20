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
    public void createNode(String name, Map<String, Object> properties) {
        // 校验
        if (name == null || properties == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        synchronized (name.intern()) {
            try (Session session = driver.session(); Transaction tx = session.beginTransaction()) {
                // 构建属性字符串
                StringBuilder propertiesBuilder = new StringBuilder();
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    // 转义属性名和属性值，避免特殊字符导致的问题
                    String key = entry.getKey().replace("\\", "\\\\").replace("'", "\\'");
                    String value = entry.getValue().toString().replace("\\", "\\\\").replace("'", "\\'");
                    propertiesBuilder.append(", ").append(key).append(": '").append(value).append("'");
                }
                String propertiesString = propertiesBuilder.toString();

                // 构建最终的Cypher查询
                String query = "CREATE (n:AMSNet  {name: $name" + propertiesString + "})";
                tx.run(query, Map.of("name", name));
                tx.commit();
            } catch (Exception e) {
                log.error("创建节点失败", e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建节点失败，数据库错误");
            }
        }
    }


    @Override
    public void deleteNode(String name) {
        // 1. 校验
        if (name == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        try (Session session = driver.session(); Transaction tx = session.beginTransaction()) {
            String query = "MATCH (n:AMSNet {name: $name}) DETACH DELETE n";
            tx.run(query, Map.of("name", name));
            tx.commit();
        } catch (Exception e) {
            log.error("删除节点失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除节点失败，数据库错误");
        }
    }

    @Override
    public void updateNode(String name, Map<String, Object> properties) {
        // 1. 校验
        if (name == null || properties == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        try (Session session = driver.session(); Transaction tx = session.beginTransaction()) {
            StringBuilder propertiesBuilder = new StringBuilder();
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                // 转义属性名和属性值，避免特殊字符导致的问题
                String key = entry.getKey().replace("\\", "\\\\").replace("'", "\\'");
                String value = entry.getValue().toString().replace("\\", "\\\\").replace("'", "\\'");
                propertiesBuilder.append(", ").append(key).append(": '").append(value).append("'");
            }
            String propertiesString = propertiesBuilder.toString();

            // 构建最终的Cypher查询
            String query = "MATCH (n:AMSNet {name: $name}) SET n += {name:$name" + propertiesString + "}";
            tx.run(query, Map.of("name", name));
            tx.commit();
        } catch (Exception e) {
            log.error("更新节点失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新节点失败，数据库错误");
        }
    }

    @Override
    public Map<String, Object> findNode(String name) {
        // 1. 校验
        if (name == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        try (Session session = driver.session(); Transaction tx = session.beginTransaction()) {
            String query = "MATCH (n:AMSNet {name: $name}) RETURN n";
            return tx.run(query, Map.of("name", name)).single().get("n").asMap();
        } catch (Exception e) {
            log.error("查询节点失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询节点失败，数据库错误");
        }
    }
}