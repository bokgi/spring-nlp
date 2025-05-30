package com.adacho.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GptResponseDto {
    private List<RestaurantDto> filteredRestaurantList;
    private List<String> gptResponseList;
    private int code;
}
