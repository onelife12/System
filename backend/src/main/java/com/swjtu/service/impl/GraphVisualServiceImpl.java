// GraphVisualServiceImpl.java
package com.swjtu.service.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.swjtu.entity.Edge;
import com.swjtu.entity.Node;
import com.swjtu.service.GraphVisualService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.neo4j.driver.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.neo4j.driver.Values.parameters;
@Slf4j
@Service
public class GraphVisualServiceImpl implements GraphVisualService {

    private final Driver driver;

    @Autowired
    public GraphVisualServiceImpl(Driver driver) {
        this.driver = driver;
    }

    @Override
    public Map<String, Object> getFullGraph() {
        try (Session session = driver.session()) {
            String query = "MATCH (n)-[r]-(m) WITH n, r, m ORDER BY rand() LIMIT 100 RETURN n, r, m";
            Result result = session.run(query);

            List<Node> nodes = new ArrayList<>();
            List<Edge> edges = new ArrayList<>();
            Set<String> seen = new HashSet<>();

            while (result.hasNext()) {
                Record record = result.next();
                Node nodeN = formatNode(record.get("n").asNode());
                if (!seen.contains(nodeN.getId())) {
                    nodes.add(nodeN);
                    seen.add(nodeN.getId());
                }

                Node nodeM = formatNode(record.get("m").asNode());
                if (!seen.contains(nodeM.getId())) {
                    nodes.add(nodeM);
                    seen.add(nodeM.getId());
                }

                edges.add(formatEdge(record.get("r").asRelationship()));
            }

            // 打印节点信息
//            System.out.println("Nodes:");
//            for (Node node : nodes) {
//                System.out.println("Node ID: " + node.getId());
//                System.out.println("Node Label: " + node.getLabel());
//                System.out.println("Node Properties: " + node.getProperties());
//                System.out.println("-------------------");
//            }

            // 打印边信息
//            System.out.println("Edges:");
//            for (Edge edge : edges) {
//                System.out.println("Edge ID: " + edge.getId());
//                System.out.println("Edge Source: " + edge.getSource());
//                System.out.println("Edge Target: " + edge.getTarget());
//                System.out.println("Edge Type: " + edge.getType());
//                System.out.println("-------------------");
//            }


            Map<String, Object> response = new HashMap<>();
            response.put("nodes", nodes);
            response.put("edges", edges);
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Node formatNode(org.neo4j.driver.types.Node node) {
        String id = String.valueOf(node.id());
        String label;
        Value nameValue = node.get("name");

        if (nameValue.isNull()) {
            label = "Unnamed Node";
        } else {
            try {
                // 尝试将值作为字符串获取
                label = nameValue.asString();
            } catch (org.neo4j.driver.exceptions.value.Uncoercible e) {
                try {
                    // 若作为字符串获取失败，尝试作为浮点数获取并转换为字符串
                    double doubleValue = nameValue.asDouble();
                    label = String.valueOf(doubleValue);
                } catch (org.neo4j.driver.exceptions.value.Uncoercible ex) {
                    // 若作为浮点数获取也失败，使用默认标签
                    label = "Unnamed Node";
                }
            }
        }

        Map<String, Object> properties = node.asMap();
        return new Node(id, label, properties);
    }

    private Edge formatEdge(org.neo4j.driver.types.Relationship relationship) {
        String id = String.valueOf(relationship.id());
        String source = String.valueOf(relationship.startNodeId());
        String target = String.valueOf(relationship.endNodeId());
        String type = relationship.type();
        return new Edge(id, source, target, type);
    }

    @Override
    public Map<String, Object> getPath() {
        try {

            List<List<List<List<String>>>> path = reqTriple();
//            System.out.println(path);
            List<List<List<String>>> triples = path.get(1);
            System.out.println(triples);

            // 解析三元组列表，构建节点和边
            List<Node> nodes = new ArrayList<>();
            List<Edge> edges = new ArrayList<>();
            Map<String, Node> nodeMap = new HashMap<>();

            for (List<List<String>> triple : triples) {
                List<String> sourceData = triple.get(0);
                List<String> targetData = triple.get(2);
                List<String> relationData = triple.get(1);

                String sourceId = sourceData.get(1);
                String sourceName = sourceData.get(0);
                String targetId = targetData.get(1);
                String targetName = targetData.get(0);
                String relationId = relationData.get(1);
                String relationType = relationData.get(0);

                // 添加源节点
                Node sourceNode = nodeMap.computeIfAbsent(sourceId, k -> {
                    Map<String, Object> properties = new HashMap<>();
                    properties.put("name", sourceName);
                    Node node = new Node();
                    node.setId(sourceId);
                    node.setLabel(sourceName);
                    node.setProperties(properties);
                    nodes.add(node);
                    return node;
                });

                // 添加目标节点
                Node targetNode = nodeMap.computeIfAbsent(targetId, k -> {
                    Map<String, Object> properties = new HashMap<>();
                    properties.put("name", targetName);
                    Node node = new Node();
                    node.setId(targetId);
                    node.setLabel(targetName);
                    node.setProperties(properties);
                    nodes.add(node);
                    return node;
                });

                // 添加边
                Edge edge = new Edge();
                edge.setId(relationId);
                edge.setSource(sourceId);
                edge.setTarget(targetId);
                edge.setType(relationType);
                edges.add(edge);
            }

            // 封装结果
            Map<String, Object> data = new HashMap<>();
            data.put("nodes", nodes);
            data.put("edges", edges);

            return data;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public Map<String, Object> limitGraph(int limit) {
        try (Session session = driver.session()) {
            String query = "MATCH (n)-[r]-(m) WITH n, r, m ORDER BY rand() LIMIT $limit RETURN n, r, m";
            Result result = session.run(query, parameters("limit", limit));

            List<Node> nodes = new ArrayList<>();
            List<Edge> edges = new ArrayList<>();
            Set<String> seen = new HashSet<>();

            while (result.hasNext()) {
                Record record = result.next();
                Node nodeN = formatNode(record.get("n").asNode());
                if (!seen.contains(nodeN.getId())) {
                    nodes.add(nodeN);
                    seen.add(nodeN.getId());
                }

                Node nodeM = formatNode(record.get("m").asNode());
                if (!seen.contains(nodeM.getId())) {
                    nodes.add(nodeM);
                    seen.add(nodeM.getId());
                }

                edges.add(formatEdge(record.get("r").asRelationship()));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("nodes", nodes);
            response.put("edges", edges);
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> reload() {
        try {
//            List<List<List<String>>> triples = mockTriples();
            List<List<List<List<String>>>> graghData = reqTriple();

            List<List<List<String>>> triples = graghData.get(0);

            // 解析三元组列表，构建节点和边
            List<Node> nodes = new ArrayList<>();
            List<Edge> edges = new ArrayList<>();
            Map<String, Node> nodeMap = new HashMap<>();

            for (List<List<String>> triple : triples) {
                List<String> sourceData = triple.get(0);
                List<String> targetData = triple.get(1);
                List<String> relationData = triple.get(2);

                String sourceId = sourceData.get(1);
                String sourceName = sourceData.get(0);
                String targetId = targetData.get(1);
                String targetName = targetData.get(0);
                String relationId = relationData.get(1);
                String relationType = relationData.get(0);

                // 添加源节点
                Node sourceNode = nodeMap.computeIfAbsent(sourceId, k -> {
                    Map<String, Object> properties = new HashMap<>();
                    properties.put("name", sourceName);
                    Node node = new Node();
                    node.setId(sourceId);
                    node.setLabel(sourceName);
                    node.setProperties(properties);
                    nodes.add(node);
                    return node;
                });

                // 添加目标节点
                Node targetNode = nodeMap.computeIfAbsent(targetId, k -> {
                    Map<String, Object> properties = new HashMap<>();
                    properties.put("name", targetName);
                    Node node = new Node();
                    node.setId(targetId);
                    node.setLabel(targetName);
                    node.setProperties(properties);
                    nodes.add(node);
                    return node;
                });

                // 添加边
                Edge edge = new Edge();
                edge.setId(relationId);
                edge.setSource(sourceId);
                edge.setTarget(targetId);
                edge.setType(relationType);
                edges.add(edge);
            }

            // 封装结果
            Map<String, Object> data = new HashMap<>();
            data.put("nodes", nodes);
            data.put("edges", edges);

            return data;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<List<List<List<String>>>> reqTriple() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://localhost:5000/get_triples")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(responseBody, List.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> sendTextsToPython(String text1, String text2) {
        OkHttpClient client = new OkHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // 创建包含两个文本数据的 Map
            Map<String, String> data = new HashMap<>();
            data.put("text1", text1);
            data.put("text2", text2);

            // 将数据转换为 JSON 字符串
            String json = objectMapper.writeValueAsString(data);

            // 创建请求体
            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(json, JSON);

            // 创建请求
            Request request = new Request.Builder()
                    .url("http://localhost:5000/sendNode") // 假设 Flask 服务运行在本地 5000 端口
                    .post(body)
                    .build();

            // 执行请求
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    return objectMapper.readValue(responseBody, Map.class);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, Object> handleTransportPath(String faultPhenomenon) {
        try {

//            List<List<List<String>>> triples = mockTriples();
            List<List<List<String>>> triples = getTransport(faultPhenomenon);

            // 解析三元组列表，构建节点和边
            List<Node> nodes = new ArrayList<>();
            List<Edge> edges = new ArrayList<>();
            Map<String, Node> nodeMap = new HashMap<>();

            for (List<List<String>> triple : triples) {
                List<String> sourceData = triple.get(0);
                List<String> targetData = triple.get(1);
                List<String> relationData = triple.get(2);

                String sourceId = sourceData.get(1);
                String sourceName = sourceData.get(0);
                String targetId = targetData.get(1);
                String targetName = targetData.get(0);
                String relationId = relationData.get(1);
                String relationType = relationData.get(0);

                // 添加源节点
                Node sourceNode = nodeMap.computeIfAbsent(sourceId, k -> {
                    Map<String, Object> properties = new HashMap<>();
                    properties.put("name", sourceName);
                    Node node = new Node();
                    node.setId(sourceId);
                    node.setLabel(sourceName);
                    node.setProperties(properties);
                    nodes.add(node);
                    return node;
                });

                // 添加目标节点
                Node targetNode = nodeMap.computeIfAbsent(targetId, k -> {
                    Map<String, Object> properties = new HashMap<>();
                    properties.put("name", targetName);
                    Node node = new Node();
                    node.setId(targetId);
                    node.setLabel(targetName);
                    node.setProperties(properties);
                    nodes.add(node);
                    return node;
                });

                // 添加边
                Edge edge = new Edge();
                edge.setId(relationId);
                edge.setSource(sourceId);
                edge.setTarget(targetId);
                edge.setType(relationType);
                edges.add(edge);
            }

            // 封装结果
            Map<String, Object> data = new HashMap<>();
            data.put("nodes", nodes);
            data.put("edges", edges);

            return data;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<List<List<String>>> getTransport(String fault) {
       /* CompletableFuture<List<List<List<String>>>> future = getTransportAsync(fault);
        future.thenAccept(result -> {
            // 处理结果
            System.out.println(result);
        }).exceptionally(ex -> {
            // 处理异常
            ex.printStackTrace();
            return null;
        });*/

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS) // 连接超时时间设置为60秒
                .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)    // 读取超时时间设置为120秒
                .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)   // 写入超时时间设置为120秒
                .build();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // 创建包含故障信息的 Map
            Map<String, String> data = new HashMap<>();
            data.put("fault", fault);

            // 将数据转换为 JSON 字符串
            String json = objectMapper.writeValueAsString(data);

            // 创建请求体
            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(json, JSON);

            // 创建请求
            Request request = new Request.Builder()
                    .url("http://localhost:5000/transport") // Python 的 transport 接口
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            // 执行请求并获取响应
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    return objectMapper.readValue(responseBody, List.class);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();

    }

    /*public static CompletableFuture<List<List<List<String>>>> getTransportAsync(String fault) {
        OkHttpClient client = new OkHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();
        CompletableFuture<List<List<List<String>>>> future = new CompletableFuture<>();

        try {
            // 创建包含故障信息的 Map
            Map<String, String> data = new HashMap<>();
            data.put("fault", fault);

            // 将数据转换为 JSON 字符串
            String json = objectMapper.writeValueAsString(data);

            // 创建请求体
            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(json, JSON);

            // 创建请求
            Request request = new Request.Builder()
                    .url("http://localhost:5000/transport") // Python 的 transport 接口
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            // 执行异步请求
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    future.completeExceptionally(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        List<List<List<String>>> result = objectMapper.readValue(responseBody, List.class);
                        future.complete(result);
                    } else {
                        future.completeExceptionally(new IOException("Python 接口响应失败，状态码：" + response.code()));
                    }
                }
            });
        } catch (IOException e) {
            future.completeExceptionally(e);
        }

        return future;
    }
*/
    @Override
    public Map<String, Object> getEval(String faultPhenomenon) {
        OkHttpClient client = new OkHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // 创建包含故障现象信息的 Map
            Map<String, String> data = new HashMap<>();
            data.put("faultPhenomenon", faultPhenomenon);

            System.out.println(data);
            log.info("请求数据: {}", data);

            // 将数据转换为 JSON 字符串
            String json = objectMapper.writeValueAsString(data);

            // 创建请求体
            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(json, JSON);

            // 创建请求
            Request request = new Request.Builder()
                    .url("http://localhost:5000/get_eval") // Python 端提供的评估结果接口
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            // 执行请求并获取响应
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Python 接口响应失败，状态码：" + response.code());
                }

                // 解析 Python 返回的 JSON 数据
                String responseBody = response.body().string();
                System.out.println(responseBody);

                return objectMapper.readValue(responseBody, Map.class);
            }
        } catch (IOException e) {
            throw new RuntimeException("调用 Python 评估接口失败：" + e.getMessage());
        }
    }

    // 模拟三元组数据
    private List<List<List<String>>> mockTriples() {
        List<List<List<String>>> mockData = new ArrayList<>();

        // 第一个三元组
        List<List<String>> triple1 = new ArrayList<>();
        triple1.add(Arrays.asList("机组氧气压力低", "58967"));
        triple1.add(Arrays.asList("氧气系统", "58315"));
        triple1.add(Arrays.asList("对应", "74327"));
        mockData.add(triple1);

        // 第二个三元组
        List<List<String>> triple2 = new ArrayList<>();
        triple2.add(Arrays.asList("两个示廓灯都不工作。", "61025"));
        triple2.add(Arrays.asList("接线有缺陷", "61031"));
        triple2.add(Arrays.asList("揭示", "78252"));
        mockData.add(triple2);

        // 第三个三元组
        List<List<String>> triple3 = new ArrayList<>();
        triple3.add(Arrays.asList("发动机起动电门在 55%N2 转速时无法自动脱开", "59292"));
        triple3.add(Arrays.asList("更换起动电门", "59295"));
        triple3.add(Arrays.asList("采取", "74927"));
        mockData.add(triple3);

        // 第一个三元组
        List<List<String>> triple4 = new ArrayList<>();
        triple4.add(Arrays.asList("机组氧气压力低", "58315"));
        triple4.add(Arrays.asList("氧气系统", "39678"));
        triple4.add(Arrays.asList("对应", "74328"));
        mockData.add(triple4);

        // 第二个三元组
        List<List<String>> triple5 = new ArrayList<>();
        triple5.add(Arrays.asList("两个示廓灯都不工作。", "39678"));
        triple5.add(Arrays.asList("接线有缺陷", "61031"));
        triple5.add(Arrays.asList("揭示", "78262"));
        mockData.add(triple5);

        // 第三个三元组
        List<List<String>> triple6 = new ArrayList<>();
        triple6.add(Arrays.asList("发动机起动电门在 55%N2 转速时无法自动脱开", "61031"));
        triple6.add(Arrays.asList("更换起动电门", "49295"));
        triple6.add(Arrays.asList("采取", "74827"));
        mockData.add(triple6);

        return mockData;
    }

    private List<List<List<String>>> mockPathTriples() {
        List<List<List<String>>> mockData = new ArrayList<>();

        // 第一个三元组
        List<List<String>> triple1 = new ArrayList<>();
        triple1.add(Arrays.asList("机组氧气压力低", "58967"));
        triple1.add(Arrays.asList("氧气系统", "58315"));
        triple1.add(Arrays.asList("对应", "74327"));
        mockData.add(triple1);


        // 第一个三元组
        List<List<String>> triple4 = new ArrayList<>();
        triple4.add(Arrays.asList("机组氧气压力低", "58315"));
        triple4.add(Arrays.asList("氧气系统", "39678"));
        triple4.add(Arrays.asList("对应", "74328"));
        mockData.add(triple4);

        // 第二个三元组
        List<List<String>> triple5 = new ArrayList<>();
        triple5.add(Arrays.asList("两个示廓灯都不工作。", "39678"));
        triple5.add(Arrays.asList("接线有缺陷", "61031"));
        triple5.add(Arrays.asList("揭示", "78262"));
        mockData.add(triple5);

        // 第三个三元组
        List<List<String>> triple6 = new ArrayList<>();
        triple6.add(Arrays.asList("发动机起动电门在 55%N2 转速时无法自动脱开", "61031"));
        triple6.add(Arrays.asList("更换起动电门", "49295"));
        triple6.add(Arrays.asList("采取", "74827"));
        mockData.add(triple6);

        return mockData;
    }

}