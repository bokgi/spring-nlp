package com.adacho.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.adacho.entity.RestaurantInfo;

@Repository
public interface RestaurantRepository extends JpaRepository<RestaurantInfo, Integer> {
	Optional<RestaurantInfo> findByPlaceName(String placeName);
}
