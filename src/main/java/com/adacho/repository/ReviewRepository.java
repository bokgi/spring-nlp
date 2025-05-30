package com.adacho.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.adacho.entity.RestaurantReviews;

@Repository
public interface ReviewRepository extends JpaRepository<RestaurantReviews, Integer>{
	List<RestaurantReviews> findByRestaurantId(Integer restaurantId);
}
