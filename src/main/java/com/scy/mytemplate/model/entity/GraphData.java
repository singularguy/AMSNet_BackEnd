package com.scy.springbootinit.model.entity;

import lombok.Data;

import java.util.Map;
@Data
public class GraphData {

    // 节点内部类
    public static class Node {
        private String name;
        private Map<String, Object> properties;

        public Node(String name, String shape, Map<String, Object> properties) {
            this.name = name;
            this.properties = properties;
        }
    }

    @Data
    // 关系内部类
    public static class Relationship {
        private String source;
        private String target;
        private String name;
        private Map<String, Object> properties;

        public Relationship(String source, String target, String name, Map<String, Object> properties) {
            this.source = source;
            this.target = target;
            this.name = name;
            this.properties = properties;
        }

        // 省略getter和setter方法，可根据实际情况添加，如使用Lombok可省略手动编写
        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, Object> properties) {
            this.properties = properties;
        }
    }
}
