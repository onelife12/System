// GraphVisualService.java
package com.swjtu.service;

import java.util.List;
import java.util.Map;

public interface GraphVisualService {
    Map<String, Object> getFullGraph();

    Map<String, Object> getPath();

    Map<String, Object> limitGraph(int limit);

    Map<String, Object> reload();

    List<List<List<List<String>>>> reqTriple();

    Map<String, Object> sendTextsToPython(String text1, String text2);

    Map<String, Object> handleTransportPath(String faultPhenomenon);

    // 获取模型评估结果（类别及概率），接收故障现象信息
    Map<String, Object> getEval(String faultPhenomenon);

}