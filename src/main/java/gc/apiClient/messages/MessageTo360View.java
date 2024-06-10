package gc.apiClient.messages;

import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Slf4j
public class MessageTo360View {

	public static WebClient webClient = null;
	public static ConnectionProvider connectionProvider = null;
	public static ReactorClientHttpConnector clientHttpConnector = null;

	public static void SendMsgTo360View(String towhere, String massage) {

		log.info(" ");
		log.info("====== ClassName : MessageTo360View & Method : SendMsgTo360View ======");

		String jsonString = massage;

		log.info("'{}' - Data : {}",towhere, jsonString);

		connectionProvider = ConnectionProvider.builder("myConnectionPool").maxConnections(100000)
				.pendingAcquireMaxCount(100000).build();
		clientHttpConnector = new ReactorClientHttpConnector(HttpClient.create(connectionProvider));

		webClient = WebClient.builder()
				.clientConnector(clientHttpConnector)
				.baseUrl("http://localhost:8081")
				.defaultHeader("Accept", "application/json")
				.defaultHeader("Content-Type", "application/json").build();

		String endpointUrl = "/360view/" + towhere;

		Mono<String> result = webClient.post().uri(endpointUrl).body(BodyInserters.fromValue(jsonString)).retrieve()
				.bodyToMono(String.class).doOnError(error -> {
					log.error("API로 요청을 보내는 과정에서 에러가 발생했습니다. : {}", error.getMessage());
					error.printStackTrace();
				}).doOnSuccess(responseBody -> {
					log.info("카프카 프로듀서로 부터 받은 응답 메시지 : {}", responseBody);
				}).doOnError(error -> {
					log.error("Error in handling response: {}", error.getMessage());
				}).doFinally(signal -> {
					log.info("요청이 성공적으로 완료되었습니다.");
				});

		// Subscribe to the Mono
		result.subscribe();

		log.info("====== End SendMsgTo360View ======");

	}

}
