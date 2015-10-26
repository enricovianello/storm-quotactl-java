package it.grid.storm.api.filesystem.quota;

public interface QuotaManager {

	QuotaInfo getQuotaInfo(QuotaInputData inputData) throws QuotaException;
	
}
