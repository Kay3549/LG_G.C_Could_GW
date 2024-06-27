package gc.apiClient.messages;

import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class MessageToApim {
	
	public void sendMsgToApim (String towhere, String entity) {
		
		log.info("====== Method : 'ToApim' ======");
		
		WebClient webClient = WebClient.builder()
				.baseUrl("http://gckafka.lguplus.co.kr:8084")
				.defaultHeader("Accept", "application/json")
				.defaultHeader("Content-Type", "application/json").build();

	    String endpointUrl = towhere;

	    log.info("ToApim Endpoint : {}",endpointUrl);
	    log.info("Apim으로 보낼 메시지: {}",entity);
	    
	    webClient.post()
	            .uri(endpointUrl)
	            .body(BodyInserters.fromValue(entity))
	            .retrieve()
	            .bodyToMono(String.class)
	            .onErrorResume(error -> {
	                log.error("Error making API request: {}",error.getMessage()) ;
	                return Mono.empty();
	            })
	            .block(); // Wait for the result

	}
	
}
