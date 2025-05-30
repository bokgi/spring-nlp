package com.adacho.controller;

import java.util.ArrayList;
import java.util.List;

//✅ 바르게 변경
import org.json.JSONException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adacho.dto.GptResponseDto;
import com.adacho.dto.RequestGptDto;
import com.adacho.service.GptService;
import com.adacho.service.InputService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

@RestController
@RequestMapping("/api/gpt")
public class GptController {
	private final GptService gptService;
	private final InputService inputService;
	private GptResponseDto gptResponseDto;

	public GptController(GptService gptService, InputService inputService) {
		this.gptService = gptService;
		this.inputService = inputService;
	}

	@PostMapping("/input")
	public ResponseEntity<GptResponseDto> requestGpt(@RequestBody RequestGptDto requestGptDto,
			@RequestHeader("Authorization") String authHeader)
			throws JsonMappingException, JsonProcessingException, JSONException {

		System.out.println("유효한 토큰임을 확인했습니다.");
		
		String token = authHeader.replace("Bearer ", "");
		
		System.out.println("GptController authHeader: " + authHeader);
		System.out.println("GptController token: " + token);
		
		String userInput = requestGptDto.getInput();
		List<Integer> recommandList = new ArrayList<Integer>();
		try {
			recommandList = inputService.getRecommand(userInput);
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		gptResponseDto = gptService.getGptComment(userInput, recommandList);

		for (int i = 0; i < gptResponseDto.getGptResponseList().size(); i++) {
			System.out.println(i + "번째 요소 : " + gptResponseDto.getGptResponseList().get(i));
		}

		for (int i = 0; i < gptResponseDto.getFilteredRestaurantList().size(); i++) {
			System.out.println(i + "번째 요소 : " + gptResponseDto.getFilteredRestaurantList().get(i).getPlaceName());
		}

		return ResponseEntity.ok(gptResponseDto);
	}

}
