package gc.apiClient.interfaceCollection;

import gc.apiClient.entity.postgresql.Entity_CampMa;

public interface InterfaceKafMsg {
	
	String maMassage (Entity_CampMa enCampMa, String datachgcd) throws Exception;

}
