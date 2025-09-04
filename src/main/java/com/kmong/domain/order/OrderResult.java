package com.kmong.domain.order;

import com.kmong.support.utils.PagingUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

public class OrderResult {

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class GetOrderMainPaging{
        private List<OrderMain> orderMainList;
        private PagingUtil.PagingCommResult pagingCommResult;
    }
}
