// GraphVisualController.java
package com.swjtu.controller;

import com.swjtu.common.Result;
import com.swjtu.service.GraphVisualService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@CrossOrigin(origins = "http://localhost:9090")
@RestController
@RequestMapping("/api")
public class GraphVisualController {

    @Autowired
    private GraphVisualService graphVisualService;

    //    图谱数据的查询
    @GetMapping("/graph")
    public Result<Map<String, Object>> getFullGraph() {
        try {
            Map<String, Object> data = graphVisualService.getFullGraph();
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    // 节点路径推理
    @GetMapping("/path")
    public Result<Map<String, Object>> getPath() {
        try {
            Map<String, Object> data = graphVisualService.getPath();
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    // 有条数限制的加载图谱数据
    @GetMapping("/limit")
    public Result<Map<String, Object>> limitGraph(@RequestParam int limit) {
        try {
            Map<String, Object> data = graphVisualService.limitGraph(limit);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    // 从python中导入已保存的图谱数据并可视化到网页上，后续任务就是路径推理
    @GetMapping("/reload")
    public Result<Map<String, Object>> reload() {
        try {
            Map<String, Object> data = graphVisualService.reload();
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    // 接收前端发送的两个文本数据并发送到 Python 端
    @PostMapping("/sendNodes")
    public Result<Map<String, Object>> sendTexts(@RequestBody Map<String, String> request) {
        try {
            String text1 = request.get("startNode");
            String text2 = request.get("endNode");
            Map<String, Object> data = graphVisualService.sendTextsToPython(text1, text2);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    // 处理 transport 路径请求
    @PostMapping("/transport")
    public Result<Map<String, Object>> handleTransportPath(@RequestBody Map<String, String> request) {
        try {
            String faultPhenomenon = request.get("faultPhenomenon");
            if (faultPhenomenon == null) {
                return Result.error(400, "Missing 'faultPhenomenon' in request body");
            }
            Map<String, Object> data = graphVisualService.handleTransportPath(faultPhenomenon);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    // 获取模型评估结果
    @PostMapping("/eval")
    public Result<Map<String, Object>> getEval(@RequestBody Map<String, String> request) {
        try {
            String faultPhenomenon = request.get("faultPhenomenon");
            if (faultPhenomenon == null) {
                return Result.error(400, "Missing 'faultPhenomenon' in request body");
            }
            Map<String, Object> data = graphVisualService.getEval(faultPhenomenon);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }


}