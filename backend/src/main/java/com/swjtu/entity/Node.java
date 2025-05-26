package com.swjtu.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Node {
    private String id;
    private String label;
    //    private String name;
    private Map<String, Object> properties;


}

