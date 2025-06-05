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
                "아래는 사용자 입력과 관련된 식당 목록이며, 평점 내림차순으로 정렬되어 있습니다.\n\n" +
                sb.toString() + "\n\n" + 
                "1. 위 목록에 있는 식당 중에서 userInput과 연관되어 있는 10개 정도의 식당만 선택해주세요.\n" +
                "2. 선택한 식당들에 대해 설명해주세요.\n" +
                "**중요**: 설명은 반드시 위 목록 순서를 따라서 설명합니다.\n\n" +
                "**중요**: 결론에는 반드시 '감사합니다.' 만 생성해야 합니다.\n\n"+
                "설명할때 반드시 지켜야하는 부분은 다음과같습니다.\n " +
                "- 첫번째 줄에 자연스럽고 친근한 인삿말을 넣습니다.\n " +
                "- 그 다음 줄부터 각 식당의 정보를 설명합니다.\n" +
                "- 각 설명은 [식당 이름: 식당에 대한 설명 내용] 형태로 시작하며, **친절하고 자연스러운 대화체 어조**와 다양한 표현을 사용하세요.\n" +
                "- 설명에 특수문자, 특수효과(기울임,굵게 등) 사용하지 마세요.\n" +
                "- 만약 식당이름에 특수문자가 들어있다면 특수문자를 포함해서 그대로 출력하세요.\n\n" +
        		"설명은 아래 예시와 같이작성하세요.\n\n" +
                "--- 설명 형식 예시 ---\n" + 
        		"[자연스럽고 친근한 인삿말]\n" +
                
                "식당 이름 1: [간단한 식당 정보 요약 - 예: 신선한 해산물 요리를 전문으로 하는 이곳은 바닷가 마을에 온 듯한 편안한 분위기가 매력적이에요.] 특히 [추천 대상 - 예: 해산물 요리를 좋아하고 정겨운 분위기를 찾는 분들]께 강력 추천합니다. 방문객 평점은 [평점]점으로 상당히 높아요. 많은 리뷰에서 [좋았던 점 - 예: 재료 신선도와 직원분들의 친절함]을 칭찬했고, [아쉬웠던 점 - 예: 다만, 인기가 많아 웨이팅이 있을 수 있다는 점]도 참고하시면 좋습니다.\n\n" + 

                "식당 이름 2: [간단한 식당 정보 요약 - 예: 트렌디한 분위기에서 독창적인 퓨전 요리를 즐길 수 있는 공간이에요.] [추천 대상 - 예: 새로운 맛에 도전하는 것을 즐기는 젊은 미식가]라면 꼭 방문해 보세요! 평점은 [평점]점으로 괜찮은 편이며, 리뷰를 보면 [긍정적 피드백 - 예: 메뉴 구성이 독특하고 플레이팅이 아름답다]는 이야기가 많아요. [평점이 낮은 편이라면 이유 포함 - 예: 반면에, 일부 메뉴는 호불호가 갈릴 수 있다는 의견도 있네요.]\n\n" + 
                
                "감사합니다.";

	     
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
