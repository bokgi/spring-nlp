package com.adacho.dto;

import java.util.List;

import com.adacho.entity.RestaurantInfo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GptResponseDto {
    private List<RestaurantInfo> filteredRestaurantList;
    private List<String> gptResponseList;
    private int code;
}
