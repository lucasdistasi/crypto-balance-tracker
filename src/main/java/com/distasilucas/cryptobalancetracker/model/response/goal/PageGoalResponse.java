package com.distasilucas.cryptobalancetracker.model.response.goal;

import java.io.Serializable;
import java.util.List;

public record PageGoalResponse(
    int page,
    int totalPages,
    boolean hasNextPage,
    List<GoalResponse> goals
) implements Serializable {

    public PageGoalResponse(int page, int totalPages, List<GoalResponse> goals) {
        this(page + 1, totalPages, totalPages - 1 > page, goals);
    }
}
