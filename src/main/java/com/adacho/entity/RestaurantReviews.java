package com.adacho.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "restaurant_reviews")
@Getter
@Setter
public class RestaurantReviews {
	
	@Id
	private Integer id;
	private String review;
	
	@Column(name="restaurant_id")
	private Integer restaurantId;
}
