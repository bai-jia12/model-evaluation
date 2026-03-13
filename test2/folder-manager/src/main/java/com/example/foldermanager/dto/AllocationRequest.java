package com.example.foldermanager.dto;

import lombok.Data;
import java.util.List;

@Data
public class AllocationRequest {
    private List<AllocationCondition> conditions;
}
