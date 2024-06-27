package gc.apiClient.controller;

import java.util.List;
import java.util.function.Function;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.scheduler.Schedulers;

import gc.apiClient.customproperties.CustomProperties;
import gc.apiClient.entity.oracleH.*;
import gc.apiClient.entity.oracleM.*;
import gc.apiClient.interfaceCollection.InterfaceDBOracle;
import gc.apiClient.interfaceCollection.InterfaceDBPostgreSQL;
import gc.apiClient.interfaceCollection.InterfaceMsgObjOrcl;
import gc.apiClient.interfaceCollection.InterfaceWebClient;
import gc.apiClient.messages.MessageTo360View;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@Profile("oracleH")
public class Controller360view2 {

    private final InterfaceDBOracle serviceOracle;
    private final InterfaceMsgObjOrcl serviceMsgObjOrcl;

    public Controller360view2(InterfaceDBPostgreSQL serviceDb, InterfaceDBOracle serviceOracle,
                             InterfaceWebClient serviceWeb, CustomProperties customProperties, InterfaceMsgObjOrcl serviceMsgObjOrcl) {
        this.serviceOracle = serviceOracle;
        this.serviceMsgObjOrcl = serviceMsgObjOrcl;
    }

    @Scheduled(fixedRate = 30000)
    public void scheduledMethod() {
        scheduleMessageTasks(
                "from_clcc_hmcepcalldt_message", Entity_DataCall.class, serviceMsgObjOrcl::DataCallMsg,
                "from_clcc_mblcepcalldt_message", Entity_MDataCall.class, serviceMsgObjOrcl::DataCallMsg,
                "from_clcc_hmcepcalldtcust_message", Entity_DataCallCustomer.class, serviceMsgObjOrcl::DataCallCustomerMsg,
                "from_clcc_mblcepcalldtcust_message", Entity_MDataCallCustomer.class, serviceMsgObjOrcl::DataCallCustomerMsg,
                "from_clcc_hmcepcallsvccd_message", Entity_DataCallService.class, serviceMsgObjOrcl::DataCallService,
                "from_clcc_mblcepcallsvccd_message", Entity_MDataCallService.class, serviceMsgObjOrcl::DataCallService,
                "from_clcc_hmcepcallmstrsvccd_message", Entity_MasterServiceCode.class, serviceMsgObjOrcl::MstrSvcCdMsg,
                "from_clcc_mblcepcallmstrsvccd_message", Entity_MMasterServiceCode.class, serviceMsgObjOrcl::MstrSvcCdMsg,
                "from_clcc_hmcepwacalldt_message", Entity_WaDataCall.class, serviceMsgObjOrcl::WaDataCallMsg,
                "from_clcc_mblcepwacalldt_message", Entity_MWaDataCall.class, serviceMsgObjOrcl::WaDataCallMsg,
                "from_clcc_hmcepwacallopt_message", Entity_WaDataCallOptional.class, serviceMsgObjOrcl::WaDataCallOptionalMsg,
                "from_clcc_mblcepwacallopt_message", Entity_MWaDataCallOptional.class, serviceMsgObjOrcl::WaDataCallOptionalMsg,
                "from_clcc_hmcepwacalltr_message", Entity_WaDataCallTrace.class, serviceMsgObjOrcl::WaDataCallTraceMsg,
                "from_clcc_mblcepwacalltr_message", Entity_MWaDataCallTrace.class, serviceMsgObjOrcl::WaDataCallTraceMsg,
                "from_clcc_hmcepcallmtrcode_message", Entity_WaMTracecode.class, serviceMsgObjOrcl::WaMTracecodeMsg,
                "from_clcc_mblcepwacallmtrcode_message", Entity_MWaMTracecode.class, serviceMsgObjOrcl::WaMTracecodeMsg
        );
    }

    private void scheduleMessageTasks(Object... args) {
        for (int i = 0; i < args.length; i += 3) {
            String topicId = (String) args[i];
            Class<?> entityClass = (Class<?>) args[i + 1];
            Function<Object, String> messageFunction = (Function<Object, String>) args[i + 2];

            Mono.fromCallable(() -> processMessages(topicId, entityClass, messageFunction))
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe();
        }
    }

    private <T> Mono<ResponseEntity<String>> processMessages(String topicId, Class<T> entityClass, Function<T, String> messageFunction) {
        try {
            int numberOfRecords = serviceOracle.getRecordCount(topicId);
            log.info("({}) 레코드의 개수 : {}", entityClass.getSimpleName(), numberOfRecords);

            if (numberOfRecords > 0) {
                List<T> entityList = serviceOracle.getAll(entityClass);

                for (T entity : entityList) {
                    String crudType = ((BaseEntity) entity).getCmd();
                    int orderId = ((BaseEntity) entity).getOrderid();

                    MessageTo360View.SendMsgTo360View(topicId, messageFunction.apply(entity));
                    serviceOracle.deleteAll(entityClass, orderId);
                }
            }
        } catch (Exception e) {
            log.error("Error processing messages for {}: {}", entityClass.getSimpleName(), e.getMessage(), e);
        }
        return Mono.just(ResponseEntity.ok("'" + entityClass.getSimpleName() + "' got message successfully."));
    }

    @GetMapping("/360view")
    @Transactional
    public Mono<ResponseEntity<String>> triggerMessageProcessing() {
        return processMessages(
                "from_clcc_hmcepcalldt_message", Entity_DataCall.class, serviceMsgObjOrcl::DataCallMsg
        );
    }
}
