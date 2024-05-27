package kafMsges;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import gc.apiClient.datamapping.MappingCenter;
import gc.apiClient.entity.Entity_CampMaJsonUcrm;
import gc.apiClient.entity.postgresql.Entity_CampMa;
import gc.apiClient.entity.postgresql.Entity_CampRt;
import gc.apiClient.interfaceCollection.InterfaceDBPostgreSQL;
import gc.apiClient.interfaceCollection.InterfaceKafMsg;
import gc.apiClient.service.ServiceJson;
import gc.apiClient.service.ServiceWebClient;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MsgUcrm implements InterfaceKafMsg {

	private InterfaceDBPostgreSQL serviceDb;

	public MsgUcrm(InterfaceDBPostgreSQL serviceDb) {
		this.serviceDb = serviceDb;
	}

	public MsgUcrm() {
	}

	@Override
	public String maMassage(Entity_CampMa enCampMa, String datachgcd) throws Exception {

		log.info(" ");
		log.info("====== ClassName : MsgUcrm & Method : maMassage ======");
		Entity_CampMaJsonUcrm enCampMaJson = new Entity_CampMaJsonUcrm();
		ObjectMapper objectMapper = new ObjectMapper();
		String jsonString = "";

		Date now = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSSSSS");
		String topcDataIsueDtm = "";
		String coid = "";
		MappingCenter mappingData = new MappingCenter();

		switch (datachgcd) {

		case "insert":
		case "update":

			coid = mappingData.getCentercodeById(Integer.toString(enCampMa.getCoid()));
			coid = coid != null ? coid : "EX";
			enCampMaJson.setCenterCd(coid);
			enCampMaJson.setCmpnId(enCampMa.getCpid());
			enCampMaJson.setCmpnNm(enCampMa.getCpna());

			topcDataIsueDtm = formatter.format(now);

			enCampMaJson.setDataChgCd(datachgcd);
			enCampMaJson.setDataDelYn("N");
			enCampMaJson.setTopcDataIsueDtm(topcDataIsueDtm);

			break;

		default:

			coid = mappingData.getCentercodeById(Integer.toString(enCampMa.getCoid()));
			coid = coid != null ? coid : "EX";
			enCampMaJson.setCenterCd(coid);
			enCampMaJson.setCmpnId(enCampMa.getCpid());
			enCampMaJson.setCmpnNm("");

			topcDataIsueDtm = formatter.format(now);

			enCampMaJson.setDataChgCd(datachgcd);
			enCampMaJson.setDataDelYn("Y");
			enCampMaJson.setTopcDataIsueDtm(topcDataIsueDtm);
			break;
		}

		jsonString = objectMapper.writeValueAsString(enCampMaJson);
		log.info("jsonString : {}", jsonString);
		log.info("====== End maMassage ======");
		return jsonString;
	}

	@Override
	public String rtMassage(Entity_CampRt enCampRt) throws Exception {

		JSONObject obj = new JSONObject();
		try {
			Date now = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSSSSS");
			String topcDataIsueDtm = formatter.format(now);

			long hubId = enCampRt.getHubid();
			int dirt = enCampRt.getDirt();
			int dict = enCampRt.getDict();
			String coid = "";
			String campid = enCampRt.getCpid();
			String didt = "";

			SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			outputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			String formattedDateString = outputFormat.format(enCampRt.getDidt());
			didt = formattedDateString;

			dirt = enCampRt.getDirt();

			ServiceWebClient crmapi = new ServiceWebClient();
			String result = crmapi.GetStatusApiRequet("campaign_stats", campid);
			dict = ServiceJson.extractIntVal("ExtractDict", result);

			Entity_CampMa enCampMa = new Entity_CampMa();

			enCampMa = serviceDb.findCampMaByCpid(campid);
			coid = Integer.toString(enCampMa.getCoid());
			MappingCenter mappingData = new MappingCenter();
			coid = mappingData.getCentercodeById(coid);
			coid = coid != null ? coid : "EX";

			String dateString = didt;
			DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
			LocalDateTime dateTime = LocalDateTime.parse(dateString, format);
			LocalDateTime adjustedDateTime = dateTime.plusHours(9);

			ZonedDateTime desiredTime = adjustedDateTime.atZone(ZoneId.of("UTC+09:00"));
			String formattedTime = desiredTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

			obj.put("topcDataIsueDtm", topcDataIsueDtm);
			obj.put("ibmHubId", hubId);
			obj.put("centerCd", coid);
			obj.put("lastAttempt", formattedTime);
			obj.put("totAttempt", dict);
			obj.put("lastResult", dirt);

		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error Message : {}", e.getMessage());
		}

		return obj.toString();
	}

}