package com.adacho.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.adacho.entity.RestaurantInfo;
import com.adacho.repository.RestaurantRepository;

@RestController
@RequestMapping("/restaurant")
public class RestaurantController {
	private RestaurantRepository restaurantRepository;
	
	public RestaurantController(RestaurantRepository restaurantRepository) {
		this.restaurantRepository = restaurantRepository;
	}
	
    @GetMapping("/search")
    public ResponseEntity<RestaurantInfo> getRestaurantById(@RequestParam int id) {
    	System.out.println("*** /restaurant/search에서 응답보냄 ***");
        return restaurantRepository.findByRestaurantId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
