package gc.apiClient.datamapping;

import java.util.HashMap;
import java.util.Map;

/**
 * 센터구분 코드를 매핑해주는 클래스이다. 제네시스에서 센터구분 코드가 정수형태로 되어있는데 카프카로 메시지를 보낼 때는 
 * 받은 정수와 매핑되는 코드를 전달해줘야한다. 
 */
public class MappingCenter {//홈 매핑 클래스.
	
	 // Define your mapping data
    private Map<String, String> idToCentercodeMapping;

    // Constructor to initialize data
    public MappingCenter() {
    	idToCentercodeMapping = new HashMap<>(); 
    	idToCentercodeMapping.put("21", "H1");
    	idToCentercodeMapping.put("23", "H2");
    	idToCentercodeMapping.put("22", "H3");
    	idToCentercodeMapping.put("26", "H4");
    	idToCentercodeMapping.put("24", "H7");
    	idToCentercodeMapping.put("16", "H11");
    	idToCentercodeMapping.put("13", "H10");
    	idToCentercodeMapping.put("12", "H9");
    	idToCentercodeMapping.put("11", "H8");
    	idToCentercodeMapping.put("25", "H12");
    	idToCentercodeMapping.put("31", "T1");
    	idToCentercodeMapping.put("33", "T2");
    	idToCentercodeMapping.put("36", "T3");
    	idToCentercodeMapping.put("35", "T4");
    	idToCentercodeMapping.put("34", "T7");
    	idToCentercodeMapping.put("37", "T6");
    	idToCentercodeMapping.put("32", "T8");
    	idToCentercodeMapping.put("59", "T9");
    }

    public String getCentercodeById(String id) {
        return idToCentercodeMapping.get(id);
    }

}
