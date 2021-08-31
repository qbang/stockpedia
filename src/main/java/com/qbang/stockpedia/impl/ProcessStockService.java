package com.qbang.stockpedia.impl;

import java.sql.Date;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.qbang.stockpedia.domain.Stock;
import com.qbang.stockpedia.persistence.StockDAOJPA;

@Service("ProcessStockService")
public class ProcessStockService {
	@Resource(name="StockDAOJPA")
	private StockDAOJPA stockDAOJPA;
	
	private HashMap<String, Integer> infoMap = new HashMap<String, Integer>();
	
	// 가격대별로 분류해서 arraylist에 넣어주기
	public ArrayList<HashMap<String, Integer>>[] classifyItemInfo(List<Stock> list) {
		ArrayList<HashMap<String, Integer>>[] ret = new ArrayList[6];
		for(int i=0; i<ret.length; i++) {
			ret[i] = new ArrayList<HashMap<String, Integer>>();
		}
		
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		int idx = 0;
		
		for(int i=0; i<list.size(); i++) {
			Stock stock = list.get(i);
			
			String name = stock.getName();
			int value = stock.getValue();
			
			if(value < 5000) {
        		idx = 0;
        	}else if(value >= 5000 && value < 10000) {
        		idx = 1;
        	}else if(value >= 10000 && value < 20000) {
        		idx = 2;
        	}else if(value >= 20000 && value < 50000) {
        		idx = 3;
        	}else if(value >= 50000 && value < 100000) {
        		idx = 4;
        	}else {
        		idx = 5;
        	}
			
			map.put(name, value);
        	ret[idx].add(map);
        	
        	map.clear();
		}
		
		return ret;
	}
	
	// 정보 String 받아서 종목 이름이랑 현재 주가만 파싱
	public HashMap<String, Integer> parseItemInfo(JSONArray stockInfoArr) {
		for(int i=0; i<stockInfoArr.length(); i++) {
			JSONObject object = (JSONObject) stockInfoArr.get(i);
			JSONObject tblStockInfo = object.getJSONObject("stockprice").getJSONObject("TBL_StockInfo");
			
			if(!tblStockInfo.get("CurJuka").toString().equals("")) {
				String jongName = tblStockInfo.get("JongName").toString();
				String curJuka = tblStockInfo.get("CurJuka").toString().replace(",", "");
				infoMap.put(jongName, Integer.parseInt(curJuka));
			}
		}
		return infoMap;
	}
	
	// 오늘 날짜와 함께 주식 정보 등록 
	public void registerStock(HashMap<String, Integer> map) {
		Date sqlDate = new Date(new java.util.Date().getTime());
		stockDAOJPA.insertStock(map, sqlDate);
	}
	
	// 오늘 날짜로 등록된 주식 리스트 가져오기 
	public List<Stock> searchTodayStock(){
		Date today = new Date(new java.util.Date().getTime());
		List<Stock> list = stockDAOJPA.selectStock(today);
		return list;
	}
	
	
	public List<Stock> searchPastStock(){
		Date date = new Date(new java.util.Date().getTime());
		Date past;
		List<Stock> list = null;
		
		for(int i=1; i<=365; i++) {
			past = new Date(date.getTime()+(1000*60*60*24*-i));
			list = stockDAOJPA.selectStock(past);
			if(list.size() != 0) {
				break;
			}
		}
		
		return list;
	}
	
	// 특정 가격대 주식 정보 가져오기
	public List<Stock> searchIdxStock(int idx) {
		Date date = null;
		if(searchTodayStock().size() == 0) {
			Date today = new Date(new java.util.Date().getTime());
			for(int i=1; i<=365; i++) {
				date = new Date(today.getTime()+(1000*60*60*24*-i));
				if(stockDAOJPA.selectStock(date).size() != 0) {
					break;
				}
			}
//			date = new Date(today.getTime()+(1000*60*60*24*-1));
		}else {
			date = new Date(new java.util.Date().getTime());
		}
		
		
		int start = 0;
		int end = 0;
		
		if(idx == 0) {
			end = 5000;
		}else if(idx == 1) {
			start = 5000;
			end = 10000;
		}else if(idx == 2) {
			start = 10000;
			end = 20000;
		}else if(idx == 3) {
			start = 20000;
			end = 50000;
		}else if(idx == 4) {
			start = 50000;
			end = 100000;
		}else {
			start = 100000;
			end = 5000000;
		}
		List<Stock> list = stockDAOJPA.selectIdxStock(start, end, date);
		return list;
	}
}
