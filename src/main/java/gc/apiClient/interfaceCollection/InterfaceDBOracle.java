package gc.apiClient.interfaceCollection;

import java.util.List;

import gc.apiClient.entity.oracleH.Entity_WaDataCallOptional;
import gc.apiClient.entity.oracleM.Entity_MWaDataCallOptional;

public interface InterfaceDBOracle {

	// insert
	Entity_WaDataCallOptional InsertWaDataCallOptional(Entity_WaDataCallOptional entityWaDataCallOptional,int wcseq);
	Entity_MWaDataCallOptional InsertMWaDataCallOptional(Entity_MWaDataCallOptional entityWaDataCallOptional,int wcseq);

	int getRecordCount(String topic_id)throws Exception;

//	List<Entity_WaDataCallOptional> getAllWaDataCallOptional();
	
	<T> List<T> getAll(Class<T> clazz)throws Exception;
	<T> void deleteAll(Class<T> clazz,int orderid)throws Exception;

}
