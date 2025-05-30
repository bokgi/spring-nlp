package com.adacho.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "restaurant_info")
@Getter
@Setter
public class RestaurantInfo {
	@Id
	private Integer id;
	
	@Column(name="category_name")
	private String categoryName;
	
	@Column(name="place_name")
	private String placeName;
	
	@Column(name="road_address_name")
	private String roadAddressName;
	
	private String phone;
	
	@Column(name="place_url")
	private String placeUrl;
	
	private double x;
	
	private double y;
	
	private double rating;
	
	@Column(name = "img_url", length = 1000)
	private String imgUrl;
}
