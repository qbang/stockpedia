package com.qbang.stockpedia.impl;

import com.qbang.stockpedia.domain.CommentCount;
import com.qbang.stockpedia.persistence.TierDAOJPA;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("TierService")
@RequiredArgsConstructor
public class TierService {
	private final TierDAOJPA tierDAOJPA;
	
	public void updateTier() {
		List<CommentCount> list = tierDAOJPA.selectCommentCount();
		if (list != null) {
			tierDAOJPA.deleteTier();
			tierDAOJPA.updateTier(list);
		}
	}
}
