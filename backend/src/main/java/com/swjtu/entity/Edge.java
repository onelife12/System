package com.swjtu.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Edge {
    private String id;
    private String source;
    private String target;
    private String type;


}