package com.scy.springbootinit.service.impl;

import com.scy.springbootinit.common.ErrorCode;
import com.scy.springbootinit.exception.BusinessException;
import com.scy.springbootinit.service.GraphService;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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
    public void updateNode(String name, Map<String, Object> newProperties) {
        // 1. 校验
        if (name == null || newProperties == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        try (Session session = driver.session(); Transaction tx = session.beginTransaction()) {
            StringBuilder propertiesBuilder = new StringBuilder();
            for (Map.Entry<String, Object> entry : newProperties.entrySet()) {
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
            Result result = tx.run(query, Map.of("name", name));
            // 判断是否有查询结果
            if (result.hasNext()) {
                return result.single().get("n").asMap();
            } else {
                // 未找到节点，返回null表示节点不存在
                log.error("查询节点失败，节点不存在");
                return null;
            }
        } catch (Exception e) {
            log.error("查询节点失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询节点失败，数据库错误");
        }
    }

    // 修改后的createRelationship方法
    @Override
    public void createRelationship(String name, Map<String, Object> properties) {
        // 校验
        if (name == null || properties == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        String startNodeName = (String) properties.get("startNodeName");
        String endNodeName = (String) properties.get("endNodeName");

        if (startNodeName == null || endNodeName == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "节点名称为空");
        }

        try (Session session = driver.session(); Transaction tx = session.beginTransaction()) {
            StringBuilder propertiesBuilder = new StringBuilder();
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue() instanceof String ? "'" + entry.getValue() + "'" : entry.getValue().toString();
                propertiesBuilder.append(", ").append(key).append(": ").append(value);
            }
            String propertiesString = propertiesBuilder.toString().replaceFirst(", ", "");

            String query = "MATCH (a:AMSNet {name: $startNodeName}), (b:AMSNet {name:$endNodeName}) " +
                    "CREATE (a)-[r:" + name + " {" + propertiesString + "}]->(b)";
            tx.run(query, Map.of("startNodeName", startNodeName, "endNodeName", endNodeName));
            tx.commit();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建关系失败，数据库错误");
        }
    }


    @Override
    public void deleteRelationship(String name) {
        // 校验
        if (name == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "关系名称参数为空");
        }
        try (Session session = driver.session(); Transaction tx = session.beginTransaction()) {
            String query = "MATCH ()-[r:" + name + "]-() DELETE r";
            tx.run(query);
            tx.commit();
        } catch (Exception e) {
            log.error("删除关系失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除关系失败，数据库错误");
        }
    }

    @Override
    public void updateRelationship(String name, Map<String, Object> newProperties) {
        // 校验
        if (name == null || newProperties == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        try (Session session = driver.session(); Transaction tx = session.beginTransaction()) {
            StringBuilder propertiesBuilder = new StringBuilder();
            for (Map.Entry<String, Object> entry : newProperties.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue() instanceof String ? "'" + entry.getValue() + "'" : entry.getValue().toString();
                propertiesBuilder.append(", ").append(key).append(": ").append(value);
            }
            String propertiesString = propertiesBuilder.toString().replaceFirst(", ", "");

            String query = "MATCH ()-[r:" + name + "]-() SET r += {" + propertiesString + "}";
            tx.run(query);
            tx.commit();
        } catch (Exception e) {
            log.error("更新关系失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新关系失败，数据库错误");
        }
    }

    @Override
    public Map<String, Object> findRelationship(String name) {
        // 校验
        if (name == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "关系参数为空");
        }
        try (Session session = driver.session(); Transaction tx = session.beginTransaction()) {
            String query = "MATCH ()-[r:" + name + "]-() RETURN r LIMIT 1";
            Result result = tx.run(query);
            if (result.hasNext()) {
                return result.single().get("r").asMap();
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("查询关系失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询关系失败，数据库错误");
        }
    }

    @Override
    public List<Map<String, Object>> getAllNodes(boolean isIncludeProperties) {
        try (Session session = driver.session(); Transaction tx = session.beginTransaction()) {
            String query = "MATCH (n:AMSNet) RETURN n";
            Result result = tx.run(query);
            List<Map<String, Object>> nodes = new ArrayList<>();
            while (result.hasNext()) {
                nodes.add(result.next().get("n").asMap());
            }
            return nodes;
        } catch (Exception e) {
            log.error("获取全部节点失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取全部节点失败，数据库错误");
        }
    }

    @Override
    public List<Map<String, Object>> getAllRelationships(boolean isIncludeProperties) {
        try (Session session = driver.session(); Transaction tx = session.beginTransaction()) {
            // 修改后的查询语句，增加了返回关系的起始节点名称和结束节点名称
            String query = "MATCH ()-[r]->() RETURN type(r) AS name, r.startNodeName AS startNodeName, r.endNodeName AS endNodeName";
            Result result = tx.run(query);

            List<Map<String, Object>> relationships = new ArrayList<>();
            while (result.hasNext()) {
                Map<String, Object> relationship = new HashMap<>(result.next().asMap());
                // 添加关系名称
                String name = (String) relationship.get("name");
                relationship.put("name", name);
                relationships.add(relationship);
            }
            return relationships;
        } catch (Exception e) {
            log.error("获取全部关系失败");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取全部关系失败，数据库错误");
        }
    }



}