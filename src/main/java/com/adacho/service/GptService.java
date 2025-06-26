package com.adacho.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.adacho.config.OpenAiProperties;
import com.adacho.dto.GptResponseDto;
import com.adacho.entity.GptCache;
import com.adacho.entity.RestaurantInfo;
import com.adacho.entity.RestaurantReviews;
import com.adacho.repository.GptCacheRepository;
import com.adacho.repository.ReviewRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GptService {

	private final OpenAiProperties openAiProperties;
	private final ReviewRepository reviewRepository;
	private final InputService inputService;
	private final GptCacheRepository gptCacheRepository;

	public GptService(OpenAiProperties openAiProperties, ReviewRepository reviewRepository, InputService inputService,
			GptCacheRepository gptCacheRepository) {
		this.openAiProperties = openAiProperties;
		this.reviewRepository = reviewRepository;
		this.inputService = inputService;
		this.gptCacheRepository = gptCacheRepository;
	}

	private final String API_URL = "https://api.openai.com/v1/chat/completions";

	@SuppressWarnings("unchecked")
	public GptResponseDto getGptComment(String userInput) throws JsonMappingException, JsonProcessingException, JSONException {

		String hash = getHash(userInput); // 해시 생성

		Optional<GptCache> cachedOpt = gptCacheRepository.findByHash(hash);

		if (cachedOpt.isPresent()) {
	        GptCache cached = cachedOpt.get();
	        System.out.println("**해시 찾았음.. 결과 바로 리턴합니다.***");

	        try {
	            ObjectMapper objectMapper = new ObjectMapper();

	            List<RestaurantInfo> restaurantList = objectMapper.readValue(
	                cached.getRestaurantListJson(), new TypeReference<List<RestaurantInfo>>() {}
	            );

	            List<String> gptResponseList = objectMapper.readValue(
	                cached.getGptResponseListJson(), new TypeReference<List<String>>() {}
	            );

	            GptResponseDto gptResponseDto = new GptResponseDto();
	            gptResponseDto.setFilteredRestaurantList(restaurantList);
	            gptResponseDto.setGptResponseList(gptResponseList);
	            
	            return gptResponseDto;

	        } catch (Exception e) {
	            e.printStackTrace();
	            throw new RuntimeException("캐시 디코딩 실패");
	        }

		}

		int i = 0;
		
        System.out.println("**해시 못 찾았음.. 결과를 생성하고 리턴합니다.***");
        
		List<Integer> recommandList = inputService.getRecommand(userInput);
		List<RestaurantInfo> restaurantList = inputService.getRestaurantInfo(recommandList);
		restaurantList.sort((a, b) -> Double.compare(b.getRating(), a.getRating()));

		StringBuilder sb = new StringBuilder();
		sb.append("다음은 평점 내림차순으로 정렬된 후보 식당 목록입니다.\n");

		for (RestaurantInfo restaurant : restaurantList) {
			sb.append("- 이름: ").append(restaurant.getPlaceName()).append(", 주소: ")
					.append(restaurant.getRoadAddressName()).append(", 카테고리: ").append(restaurant.getCategoryName())
					.append(", 평점: ").append(restaurant.getRating()).append(", 후기: ");

			List<RestaurantReviews> reviews = reviewRepository.findByRestaurantId(restaurant.getRestaurantId());

			for (RestaurantReviews review : reviews) {
				sb.append(" ").append(review.getReview()).append(" /");
			}

			sb.append("\n");
			i++;
		}

		String gptInput = "사용자 입력: \"" + userInput + "\"\n" 
				+ "아래는 사용자 입력과 관련된 식당 목록입니다.\n"
				+ sb.toString() + "\n"
				+ "위 식당 목록은 평점 내림차순으로 정렬되어 있습니다.\n" 
				+ "1. 위 목록의 식당 중에서 사용자 입력과 연관되어 있는 식당 **10개 이상 선택**하세요.\n" 
				+ "2. 아래 조건을 참고해서 선택한 식당들에 대해 설명하세요.\n"
				+ "[설명시 반드시 지켜야하는 조건]\n " 
				+ "- **식당 설명 순서는 무조건 위 식당 목록 순서를 따르세요. 절대로 순서를 바꿔서 설명하지마세요.**"
				+ "- 첫번째 줄에 자연스럽고 친근한 인삿말을 넣습니다.\n " 
				+ "- 그 다음 줄부터 각 식당의 정보를 설명합니다.\n"
				+ "- 각 설명의 형식은 다음과 같습니다\n" 
				+ "식당 이름: 설명 내용\n" 
				+ "- 설명에 식당 평점을 포함하세요.\n"
				+ "- 친절하고 자연스러운 대화체 어조와 다양한 표현을 사용하세요.\n" 
				+ "- 첫번째 줄에 자연스럽고 친근한 인삿말을 50자 내외로 작성하세요.\n"
				+ "- 설명에 특수문자, 특수효과(기울임,굵게 등) 사용하지 마세요.\n" 
				+ "- 식당이름에 특수문자가 들어있다면 없애지 말고 그대로 출력하세요.\n"
				+ "- 식당 이름이 동일하더라도, 설명할 때 절대로 괄호나 주소을 붙이지 마세요. 구분할필요없습니다.\n"
				+ "- 식당 이름은 반드시 목록에 적힌 그대로만 출력해야됩니다.\n"
				+ "- 예를들어 이름이 '홍두깨손칼국수' 인 식당이 여러개있어도\n"
				+ "- 설명할때 절대로 '홍두깨손칼국수(도봉로)' 이렇게 출력하지마세요.\n"
				+ "- 다시 강조합니다. 식당 설명 순서는 무조건 위 식당 목록 순서를 따르세요. 절대 순서 바꾸지 마세요.\n"
				+ "- 마지막 식당 설명 뒤에 절대로 아무 말도 붙이지 말고 그 다음줄에 '감사합니다.'만 단독으로 출력하세요.\n"
				+ "설명은 아래 예시와 같이 작성하세요.\n" 
				+ "--- 설명 형식 예시 ---\n"
				+ "자연스럽고 친근한 인사말"
				+ "해물명가: 신선한 해산물 요리를 전문으로 하는 이곳은 바닷가 마을에 온 듯한 분위기가 매력적이에요. 특히 해산물 요리를 좋아하고 정겨운 분위기를 찾는 분들께 추천합니다. 평점은 4.9점으로 매우 높고, 리뷰에서는 재료 신선도와 직원들의 친절함이 호평받고 있어요. 다만 인기가 많아 웨이팅이 길 수 있다는 점은 참고하세요.\n\n"
				+ "감사합니다.";

		System.out.println(gptInput);

		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(openAiProperties.getKey());

		Map<String, Object> message = Map.of("role", "user", "content", gptInput);

		Map<String, Object> requestBody = Map.of("model", openAiProperties.getModel(), "messages", List.of(message),
				"temperature", 0.7);

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

		ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, request, Map.class);

		List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
		Map<String, Object> messageContent = (Map<String, Object>) choices.get(0).get("message");

		String gptResponse = messageContent.get("content").toString().trim();
		System.out.println(gptResponse);

		List<String> gptResponseList = splitGptResponse(gptResponse);
		List<RestaurantInfo> filteredRestaurantList = filterRestaurantsByGptResponse(gptResponse, restaurantList);

		GptResponseDto gptResponseDto = new GptResponseDto();

		gptResponseDto.setFilteredRestaurantList(filteredRestaurantList);
		gptResponseDto.setGptResponseList(gptResponseList);
		
		try {
		    ObjectMapper objectMapper = new ObjectMapper();

		    String restaurantListJson = objectMapper.writeValueAsString(filteredRestaurantList);
		    String gptResponseListJson = objectMapper.writeValueAsString(gptResponseList);

		    GptCache cache = new GptCache();
		    cache.setHash(hash); // 위에서 만든 해시값 그대로
		    cache.setRestaurantListJson(restaurantListJson);
		    cache.setGptResponseListJson(gptResponseListJson);

		    gptCacheRepository.save(cache);

		} catch (Exception e) {
		    e.printStackTrace();
		    System.out.println("GPT 응답 캐싱 실패");
		}

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

	public List<RestaurantInfo> filterRestaurantsByGptResponse(String gptResponse,
			List<RestaurantInfo> restaurantList) {
		// GPT 응답에서 추천된 식당 이름 리스트 추출
		List<String> recommendedNames = extractRecommendedRestaurantNames(gptResponse);

		// 원래 리스트에서 이름이 포함되지 않는 식당은 제거
		restaurantList.removeIf(restaurant -> !recommendedNames.contains(restaurant.getPlaceName()));

		return restaurantList;
	}

	public String getHash(String userInput) {
		
		try {
			
			userInput = userInput.trim().replaceAll("\\s+", " "); // 정규화
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashBytes = digest.digest(userInput.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(hashBytes);
			
		} catch (Exception e) {
			
			throw new RuntimeException("입력 해시 생성 실패", e);
		}
	}

}
