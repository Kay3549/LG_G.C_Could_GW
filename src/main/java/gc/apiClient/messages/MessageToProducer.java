package gc.apiClient.messages;

import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageToProducer {
	
	public void sendMsgToProducer (String towhere, String jsonString) {
		
		
		log.info(" ");
		log.info("====== ClassName : MessageToProducer & Method : sendMsgToProducer ======");
		log.info("Producer로 보낼 EndPoint & 메시지 : '{}' / {}",towhere,jsonString);
		
		WebClient webClient = WebClient.builder().baseUrl("http://localhost:8081").build();

	    String endpointUrl = towhere;  
	    
	    webClient.post()
	            .uri(endpointUrl)
	            .body(BodyInserters.fromValue(jsonString))
	            .retrieve()
	            .bodyToMono(String.class)
	            .doOnError(error -> {
	                log.error("API로 요청을 보내는 과정에서 에러가 발생했습니다. : {}", error.getMessage());
	                error.printStackTrace();
	            })
	            .subscribe(responseBody -> {
	                log.info("카프카 프로듀서로 부터 받은 응답 메시지 : {}", responseBody);
	            }, error -> {
	                log.error("Error in handling response: {}", error.getMessage());
	            }, () -> {
	                log.info("요청이 성공적으로 완료되었습니다.");
	            });
	    
		log.info("====== End sendMsgToProducer ======");
	}
	
	

}
