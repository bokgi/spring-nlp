package com.adacho.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//✅ 바르게 변경
import org.json.JSONObject;
import org.json.JSONException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.adacho.dto.RestaurantDto;
import com.adacho.entity.RestaurantInfo;
import com.adacho.repository.RestaurantRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class InputService {

	private final RestaurantRepository restaurantRepository;

	public InputService(RestaurantRepository restaurantRepository) {
		this.restaurantRepository = restaurantRepository;
	}

	public List<Integer> getRecommand(String nlp) throws JsonMappingException, JsonProcessingException, JSONException {

		// RestTemplate 사용 예시
		RestTemplate restTemplate = new RestTemplate();
		String query = nlp;
		String url = "find-recommand-app/search";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		JSONObject requestJson = new JSONObject();
		try {
			requestJson.put("query", query);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		HttpEntity<String> request = new HttpEntity<>(requestJson.toString(), headers);

		ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
		System.out.println(response.getBody());

		ObjectMapper objectMapper = new ObjectMapper();
		List<Integer> recommandList = objectMapper.readValue(response.getBody(), new TypeReference<List<Integer>>() {
		});

		// recommandList = response.getBody();

		return recommandList;
	}

	public List<RestaurantInfo> getRestaurantInfo(List<Integer> recommandList) {

		List<RestaurantInfo> restaurantList = new ArrayList<>();

		for (Integer id : recommandList) {
			System.out.println("InputService id: "+ id);
			Optional<RestaurantInfo> optionalRestaurantInfo = restaurantRepository.findById(id);
			RestaurantInfo restaurantInfo = null;
			System.out.println("**InputService**: " + optionalRestaurantInfo.isPresent());
			if (optionalRestaurantInfo.isPresent()) {
				restaurantInfo = optionalRestaurantInfo.get();
				restaurantList.add(restaurantInfo);
			}
		}

		return restaurantList;
	}
	
}
