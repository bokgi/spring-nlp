package com.adacho.dto;

import lombok.Getter;

@Getter
public class RestaurantDto {
	private int id;
    private double x;
    private double y;
    private String url;
    private String address;
    private String phone;
    private String placeName;
    private String category;
    private double rating;
    private String imgUrl;
    
    public RestaurantDto (int id, double x, double y, String placeName, String url, String address, String phone, String category, double rating, String imgUrl) {
    	this.id = id;
        this.x = x;
        this.y = y;
        this.url = url;
        this.address = address;
        this.phone = phone;
        this.placeName = placeName;
        this.category = category;
        this.rating = rating;
        this.imgUrl = imgUrl;
    }
}
