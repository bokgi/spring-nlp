package com.adacho.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.adacho.config.OpenAiProperties;
import com.adacho.dto.GptResponseDto;
import com.adacho.dto.RestaurantDto;
import com.adacho.entity.RestaurantReviews;
import com.adacho.repository.ReviewRepository;

@Service
public class GptService {
	
	private final OpenAiProperties openAiProperties;
	private final ReviewRepository reviewRepository;
	private final InputService inputService;
	
	public GptService(OpenAiProperties openAiProperties, ReviewRepository reviewRepository, InputService inputService) {
		this.openAiProperties = openAiProperties;
		this.reviewRepository = reviewRepository;
		this.inputService = inputService;
	}

	private final String API_URL = "https://api.openai.com/v1/chat/completions";

	@SuppressWarnings("unchecked")
	public GptResponseDto getGptComment(String userInput, List<Integer> recommandList) {
		
		int i = 0;
		
		List<RestaurantDto> restaurantList = inputService.getRestaurantInfo(recommandList);
		restaurantList.sort((a, b) -> Double.compare(b.getRating(), a.getRating()));
		
		StringBuilder sb = new StringBuilder();
		sb.append("다음은 평점 내림차순으로 정렬된 후보 식당 목록입니다.\n");
		
		for(RestaurantDto restaurant : restaurantList) {
			sb.append("- 이름: ").append(restaurant.getPlaceName())
			.append(", 주소: ").append(restaurant.getAddress()).append(", 카테고리: ").append(restaurant.getCategory())
			.append(", 평점: ").append(restaurant.getRating()).append(", 후기: ");
			
			List<RestaurantReviews> reviews = reviewRepository.findByRestaurantId(recommandList.get(i));

			for(RestaurantReviews review : reviews) {
				sb.append(" ").append(review.getReview()).append(" /");
			}
			
			sb.append("\n");
			i++;
		}
		
	    String gptInput = "사용자 입력: \"" + userInput + "\"\n" +
                sb.toString() + "목록에 있는 식당들의 카테고리, 주소, 정보, 후기, 평점들을 참고해서 사용자 입력에 있는 음식(사용자가 원하는 음식)을 판매하는 식당들 혹은 사용자 입력에 있는 음식(사용자가 원하는 음식)과 연관있는 식당들을 최소 10개 이상 선택해주세요.\n"
                		+ "만약 식당 목록에 있는 식당이 10개 미만이라면, 최소 10개 이상 선택하지 않아도 됩니다.\n"
                		+ "식당들을 선택했다면, 선택한 식당들을 설명하세요.\n"
                		+ "설명순서는 반드시 위에 있는 평점 내림차순으로 설명해야합니다.\n\n"
                		+ "설명형식은 아래와 같습니다. 반드시 지켜주세요.\n "
                		+ "- 첫번째 줄에 자연스러운 인삿말을 넣습니다.\n "
                		+ "- 그 다음줄부터 식당이름: 식당에 대한 설명\n"
                		+ "- 식당에 대한 설명에는 가장 먼저 식당의 정보를 요약한 문단을 포함할 것.\n"
                		+ "- 어떤 사람에게 추천할 만한 식당인지에 대한 내용을 포함할 것.\n"
                		+ "- 식당의 정보 다음으로 평점과 리뷰를 참고한 내용을 포함할 것. "
                		+ "- 낮은 편이라면, 낮은 이유를 포함할 것.\n"
                		+ "- 설명에 특수문자(* 등),특수효과(기울임,굵게 등) 사용금지\n "
                		+ "- 절대로 설명할때 번호 붙이지 말 것.\n"
                		+ "- 마지막 식당까지 설명이 끝났으면 즉시 생성을 종료.\n "
                		+ "- 절대로 마무리멘트, 결론, 요약 문장들을 추가하면 안됩니다.";
	    
	    System.out.println(gptInput);
		
		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(openAiProperties.getKey());

		Map<String, Object> message = Map.of("role", "user", "content",
				gptInput);

		Map<String, Object> requestBody = Map.of(
				"model", openAiProperties.getModel(), 
				"messages", List.of(message), 
				"temperature", 0.7
				);

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

		ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, request, Map.class);

		List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
		Map<String, Object> messageContent = (Map<String, Object>) choices.get(0).get("message");

		String gptResponse = messageContent.get("content").toString().trim();
		System.out.println(gptResponse);
		
		List<String> gptResponseList = splitGptResponse(gptResponse);
		List<RestaurantDto> filteredRestaurantList = filterRestaurantsByGptResponse(gptResponse, restaurantList);
		
		GptResponseDto gptResponseDto = new GptResponseDto();
		
		gptResponseDto.setFilteredRestaurantList(filteredRestaurantList);
		gptResponseDto.setGptResponseList(gptResponseList);
		
		return gptResponseDto;
	}
	
	public List<String> splitGptResponse(String gptResponse) {
	    List<String> result = new ArrayList<>();
	    String[] lines = gptResponse.split("\n");

	    StringBuilder currentEntry = new StringBuilder();
	    boolean firstLineAdded = false;

	    for (int i = 0; i < lines.length; i++) {
	        String line = lines[i].trim();

	        // 인삿말 (첫 번째 줄)
	        if (!firstLineAdded && !line.contains(":") && !line.isEmpty()) {
	            result.add(line); // 인삿말을 리스트의 첫 요소로 추가
	            firstLineAdded = true;
	            continue;
	        }

	        // 식당 이름으로 시작하는 줄: "식당이름: 설명"
	        if (line.contains(":")) {
	            if (currentEntry.length() > 0) {
	                result.add(currentEntry.toString().trim());
	                currentEntry.setLength(0);
	            }
	            currentEntry.append(line);
	        } else {
	            // 식당 설명 이어붙이기
	            if (currentEntry.length() > 0 && !line.isEmpty()) {
	                currentEntry.append(" ").append(line);
	            }
	        }
	    }

	    // 마지막 식당 항목 처리
	    if (currentEntry.length() > 0) {
	        result.add(currentEntry.toString().trim());
	    }

	    // 마무리 멘트 (마지막 줄)
	    String lastLine = lines[lines.length - 1].trim();
	    if (!lastLine.contains(":") && lastLine.length() > 5 && !result.get(result.size() - 1).equals(lastLine)) {
	        result.add(lastLine);
	    }

	    return result;
	}

	
	public List<String> extractRecommendedRestaurantNames(String gptResponse) {
	    List<String> names = new ArrayList<>();
	    String[] lines = gptResponse.split("\n");

	    for (String line : lines) {
	        line = line.trim();
	        // "식당이름: 설명" 형식만 추출
	        if (line.contains(":")) {
	            int colonIndex = line.indexOf(":");
	            if (colonIndex > 0) {
	                String name = line.substring(0, colonIndex).trim();
	                names.add(name);
	            }
	        }
	    }

	    return names;
	}

	
	public List<RestaurantDto> filterRestaurantsByGptResponse(String gptResponse, List<RestaurantDto> restaurantList) {
	    // GPT 응답에서 추천된 식당 이름 리스트 추출
	    List<String> recommendedNames = extractRecommendedRestaurantNames(gptResponse);

	    // 원래 리스트에서 이름이 포함되지 않는 식당은 제거
	    restaurantList.removeIf(restaurant -> !recommendedNames.contains(restaurant.getPlaceName()));

	    return restaurantList;
	}


}
