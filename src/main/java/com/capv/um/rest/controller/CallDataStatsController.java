package com.capv.um.rest.controller;

import java.text.ParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.capv.um.model.CallDataStats;
import com.capv.um.model.CallDataStatsDTO;
import com.capv.um.service.CallStateService;

@RestController
@RequestMapping("/callDataStats")
public class CallDataStatsController {

	@Autowired
	private CallStateService callStatesService;
	
	@RequestMapping(value="/allClientsCallStatsReport/{startDate}/{endDate}/{draw}/{length}/{start}",method= RequestMethod.GET, produces={"application/json"})
	public CallDataStatsDTO getAllClientsCallStatsReport(@PathVariable("startDate") String startDate,@PathVariable("endDate") String endDate,
																												@PathVariable("draw") String draw,@PathVariable("length") String length, 
																												@PathVariable("start") String start)
	{
			int startIndex = Integer.parseInt(start);
			int countIndex = Integer.parseInt(length);
			
			List<CallDataStats> callDataStatsList = callStatesService.getAllClientsCallStats(startDate, endDate,startIndex,countIndex);
			
			Integer totalRecords = callStatesService.getListOfAllClientsCallStats(startDate, endDate);
			
			CallDataStatsDTO callDataStatsDTO = new CallDataStatsDTO();
			callDataStatsDTO.setDraw(Integer.parseInt(draw));
			callDataStatsDTO.setData(callDataStatsList);
			callDataStatsDTO.setRecordsFiltered(totalRecords);
			callDataStatsDTO.setRecordsTotal(totalRecords);
			
			return callDataStatsDTO;
	}
	
	@RequestMapping(value="/byClientCallStatsReport/{clientId}/{startDate}/{endDate}/{draw}/{length}/{start}/{searchParam}",
											method= RequestMethod.GET, produces={"application/json"})
	public CallDataStatsDTO getAllClientsCallStatsReportUsingSearch(@PathVariable("clientId") String clientId, @PathVariable("startDate") String startDate,
																									@PathVariable("endDate") String endDate, @PathVariable("draw") String draw, 
																									@PathVariable("length") String length, @PathVariable("start") String start,
																									@PathVariable("searchParam") String searchParam)
	{
			int startIndex = Integer.parseInt(start);
			int countIndex = Integer.parseInt(length);
			
			List<CallDataStats> CallDataStatsListUsingSearch = callStatesService.getAllClientsCallStatsReportUsingSearch(clientId, startDate, endDate, startIndex, countIndex, searchParam);
			
			Integer totalRecords = callStatesService.getListOfCallStatsForSearch(clientId, startDate, endDate, searchParam);
			
			CallDataStatsDTO callDataStatsDTO = new CallDataStatsDTO();
			callDataStatsDTO.setDraw(Integer.parseInt(draw));
			callDataStatsDTO.setData(CallDataStatsListUsingSearch);
			callDataStatsDTO.setRecordsFiltered(totalRecords);
			callDataStatsDTO.setRecordsTotal(totalRecords);
			
			return callDataStatsDTO;
	}

	@RequestMapping(value="/byClientCallStatsReport/{clientId}/{startDate}/{endDate}/{draw}/{length}/{start}",method= RequestMethod.GET, produces={"application/json"})
	public CallDataStatsDTO getByClientCallStatsReport(@PathVariable("clientId") String clientId,@PathVariable("startDate") String startDate,@PathVariable("endDate") String endDate,
										@PathVariable("draw") String draw,@PathVariable("length") String length, @PathVariable("start") String start) throws ParseException
	{
		int startIndex = Integer.parseInt(start);
		int countIndex = Integer.parseInt(length);
		
		List<CallDataStats> CallDataStatsList = callStatesService.getByClientCallStats(clientId,startDate, endDate,startIndex,countIndex);
		
		Integer totalRecords = callStatesService.getListOfCallStats(clientId, startDate, endDate);
		
		CallDataStatsDTO callDataStatsDTO = new CallDataStatsDTO();
		callDataStatsDTO.setDraw(Integer.parseInt(draw));
		callDataStatsDTO.setData(CallDataStatsList);
		callDataStatsDTO.setRecordsFiltered(totalRecords);
		callDataStatsDTO.setRecordsTotal(totalRecords);
		
		return callDataStatsDTO;
	}
	
	@RequestMapping(value="/allUserCallStatsReport/{startDate}/{endDate}/{draw}/{length}/{start}",method= RequestMethod.GET, produces={"application/json"})
	public CallDataStatsDTO getAllUsersCallStatsReport(@PathVariable("startDate") String startDate,@PathVariable("endDate") String endDate,
																												@PathVariable("draw") String draw,@PathVariable("length") String length, @PathVariable("start") String start)
	{
		int startIndex = Integer.parseInt(start);
		int countIndex = Integer.parseInt(length);
		
		List<CallDataStats> CallDataStatsList = callStatesService.getAllUserCallStats(startDate, endDate,startIndex,countIndex);
		CallDataStatsDTO callDataStatsDTO = new CallDataStatsDTO();
		callDataStatsDTO.setDraw(Integer.parseInt(draw));
		callDataStatsDTO.setData(CallDataStatsList);
		callDataStatsDTO.setRecordsFiltered(CallDataStatsList.size());
		callDataStatsDTO.setRecordsTotal(CallDataStatsList.size());
		
		return callDataStatsDTO;
	}

	@RequestMapping(value="/byUserCallStatsReport/{clientId}/{userName}/{startDate}/{endDate}/{draw}/{length}/{start}",method= RequestMethod.GET, produces={"application/json"})
	public CallDataStatsDTO getByUserCallStatsReport(@PathVariable("clientId") String clientId,@PathVariable("userName") String userName,@PathVariable("startDate") String startDate,@PathVariable("endDate") String endDate,
																											@PathVariable("draw") String draw,@PathVariable("length") String length, @PathVariable("start") String start) throws ParseException
	{
		int startIndex = Integer.parseInt(start);
		int countIndex = Integer.parseInt(length);

		List<CallDataStats> CallDataStatsList = callStatesService.getByUserCallStatsReport(clientId,userName,startDate, endDate,startIndex,countIndex);
		
		Integer totalRecords = callStatesService.getListOfCallStatsUsingClientIdAndUserName(clientId, startDate, endDate, userName);
		
		CallDataStatsDTO callDataStatsDTO = new CallDataStatsDTO();
		callDataStatsDTO.setDraw(Integer.parseInt(draw));
		callDataStatsDTO.setData(CallDataStatsList);
		callDataStatsDTO.setRecordsFiltered(totalRecords);
		callDataStatsDTO.setRecordsTotal(totalRecords);
		
		return callDataStatsDTO;		
	}
	
	@RequestMapping(value="/byUserCallStatsReport/{clientId}/{userName}/{startDate}/{endDate}/{draw}/{length}/{start}/{searchParam}",
											method= RequestMethod.GET, produces={"application/json"})
	public CallDataStatsDTO getByUserCallStatsReportUsingSearch(@PathVariable("clientId") String clientId,@PathVariable("userName") String userName,
																																   @PathVariable("startDate") String startDate,@PathVariable("endDate") String endDate,
																																   @PathVariable("draw") String draw, @PathVariable("length") String length, 
																																   @PathVariable("start") String start, @PathVariable("searchParam") String searchParam)
	{
		int startIndex = Integer.parseInt(start);
		int countIndex = Integer.parseInt(length);
		
		List<CallDataStats> CallDataStatsList = callStatesService.getByUserCallStatsReportUsingSearch(clientId,searchParam,startDate, endDate,startIndex,countIndex);
		
		Integer totalRecords = callStatesService.getListOfCallStatsUsingClientIdAndSearchParam(clientId, startDate, endDate, searchParam);
		
		CallDataStatsDTO callDataStatsDTO = new CallDataStatsDTO();
		callDataStatsDTO.setDraw(Integer.parseInt(draw));
		callDataStatsDTO.setData(CallDataStatsList);
		callDataStatsDTO.setRecordsFiltered(totalRecords);
		callDataStatsDTO.setRecordsTotal(totalRecords);
		
		return callDataStatsDTO;		
	}

	@RequestMapping(value="/byClientCallStateReport/{clientId}/{state}/{draw}/{length}/{start}",method= RequestMethod.GET, produces={"application/json"})
	public CallDataStatsDTO getByClientCallState(@PathVariable("clientId") String clientId,@PathVariable("state") String state,
																								@PathVariable("draw") String draw,@PathVariable("length") String length, @PathVariable("start") String start)
	{
		int startIndex = Integer.parseInt(start);
		int countIndex = Integer.parseInt(length);
		
		List<CallDataStats> CallDataStatsList = callStatesService.getByClientCallStateReport(clientId,state,startIndex,countIndex);
		
		CallDataStatsDTO callDataStatsDTO = new CallDataStatsDTO();
		callDataStatsDTO.setDraw(Integer.parseInt(draw));
		callDataStatsDTO.setData(CallDataStatsList);
		callDataStatsDTO.setRecordsFiltered(CallDataStatsList.size());
		callDataStatsDTO.setRecordsTotal(CallDataStatsList.size());
		
		return callDataStatsDTO;		
	}
	
}
