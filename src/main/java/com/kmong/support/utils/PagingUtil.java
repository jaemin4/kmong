package com.kmong.support.utils;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Pageable;
import java.util.List;

public class PagingUtil {

    public static void getOrderByPagingCall(Pageable pageable, List<OrderByMap> orderByList) {
        String orderBy = pageable.getSort().stream()
                .findFirst()
                .map(order -> {
                    String property = order.getProperty();

                    String mapped = orderByList.stream()
                            .filter(m -> m.getOrderBy().equalsIgnoreCase(property))
                            .map(OrderByMap::getQuery)
                            .findFirst()
                            .orElse(property);

                    return mapped + " " + order.getDirection().name();
                })
                .orElse("");

        int pageNum = pageable.getPageNumber() + 1;
        int pageSize = pageable.getPageSize();

        PageHelper.startPage(pageNum, pageSize, orderBy);
    }

    public static void getPagingCall(Pageable pageable) {
        int pageNum = pageable.getPageNumber() + 1;
        int pageSize = pageable.getPageSize();

        PageHelper.startPage(pageNum, pageSize);
    }

    public static PagingCommResult ofPagingCommResult(PageInfo<?> pageInfo) {
        return PagingCommResult.of(
                pageInfo.getTotal(),
                pageInfo.getPages(),
                pageInfo.getPageNum() - 1,
                pageInfo.getPageSize(),
                pageInfo.isIsFirstPage(),
                pageInfo.isIsLastPage(),
                pageInfo.getList().isEmpty(),
                pageInfo.getList().size()
        );
    }


    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class OrderByMap{
        private String orderBy;
        private String query;
    }

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class PagingCommResult{
        private long totalElements;
        private int totalPages;
        private int number;
        private int size;
        private boolean first;
        private boolean last;
        private boolean empty;
        private int numberOfElements;
    }
}
