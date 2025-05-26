package com.swjtu;

import com.swjtu.service.GraphVisualService;
import com.swjtu.service.impl.GraphVisualServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@SpringBootTest
class BackendApplicationTests {

    private GraphVisualServiceImpl graphVisualServiceImpl;

    @Test
    void contextLoads() {
    }

    @Autowired
    private GraphVisualService graphVisualService;

    @Test
    public void Graph() {
        graphVisualService.getFullGraph();
    }

    @Test
    public void reload() {
        graphVisualService.reload();
    }

    @Test
    public void reqTriple() {
        List<List<List<List<String>>>> res = graphVisualService.reqTriple();
        res.stream().forEach(str -> System.out.println(str));
    }

    @Test
    public void getPath() {
        Map<String, Object> path = graphVisualService.getPath();
        System.out.println(path);
    }

    @Test
    public void getTranport(){
        List<List<List<String>>> transport = GraphVisualServiceImpl.getTransport("TERR范围不一致消息");
        System.out.println(transport);
        transport.stream().forEach(str -> System.out.println(str));

    }


}
