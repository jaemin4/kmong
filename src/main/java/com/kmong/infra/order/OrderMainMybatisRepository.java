package com.kmong.infra.order;

import com.kmong.domain.order.OrderMain;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface OrderMainMybatisRepository {
    List<OrderMain> findAllByKeyword(String keyword, LocalDate start, LocalDate end);
}
