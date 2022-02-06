package com.qbang.stockpedia.controller;

import com.qbang.stockpedia.domain.Board;
import com.qbang.stockpedia.domain.Stock;
import com.qbang.stockpedia.impl.ProcessStockService;
import com.qbang.stockpedia.impl.RedisService;
import com.qbang.stockpedia.persistence.CommunityDAOJPA;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class HomeController {
	private final ProcessStockService processStockService;
	private final CommunityDAOJPA communityDAOJPA;
	private final RedisService redisService;

	@GetMapping("/")
	public String home(Model model, HttpServletRequest req) {
//		redisService.test();
		HttpSession session = req.getSession();
		//세션에서 닉네임 가져오기
		Object unick = session.getAttribute("unick");
		model.addAttribute("unick", unick == null ? "게스트" : unick.toString());

		//오늘 날짜의 주식정보 가져오기
		List<Stock> stock = processStockService.searchTodayStock();

		if (stock == null || stock.size() == 0) {
			stock = processStockService.searchPastStock();
		}

		//가져온 정보에서 개수만 빼주고 모델에 넣기
		ArrayList<HashMap<String, Integer>>[] arr = processStockService.classifyItemInfo(stock);
		for (int i = 0; i < arr.length; i++) {
			String mapName = "map" + (i + 1);
			model.addAttribute(mapName, arr[i].size());
		}

		//인기글 가져오기
		Optional<List<Board>> board = communityDAOJPA.selectContentTopList();
		if (board.isPresent()) { // null일 때 콘텐츠를 어떻게 보여줄건지 처리 필요
			model.addAttribute("list", board.get());
		}

		return "main";
	}

	@GetMapping("/stock")
	public String stock(Model model, @RequestParam int idx) {
		//금액별 주식 리스트 조회 
		List<Stock> list = processStockService.searchIdxStock(idx);
		model.addAttribute("list", list);

		return "stock";
	}
}
