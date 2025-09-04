package com.kmong.infra.order;

import com.kmong.domain.order.OrderMain;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderMainMybatisRepository {
    List<OrderMain> findAllByKeyword(String keyword);
}
