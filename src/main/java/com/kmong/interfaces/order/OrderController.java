package com.kmong.interfaces.order;

import com.kmong.domain.order.OrderMain;
import com.kmong.domain.order.OrderResult;
import com.kmong.domain.order.OrderService;
import com.kmong.support.response.APIPagingResponse;
import com.kmong.support.response.APIResponse;
import com.kmong.support.utils.PagingUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/register")
    @Operation(summary = "주문 등록")
    public APIResponse<Void> registerOrder(
            @RequestBody OrderRequest.Register request
    ) {
        //orderService.register(OrderCommand.Register.of());

        return APIResponse.message("주문 등록에 성공하였습니다.");
    }

    @GetMapping("/get/main/paging")
    @Operation(summary = "메인 주문 페이징 조회")
    public APIPagingResponse<List<OrderMain>, PagingUtil.PagingCommResult> getOrderMainPaging(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @PageableDefault(page = 0, size = 10, sort = "order_date", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        OrderResult.GetOrderMainPaging data = orderService.getOrderMainPaging(keyword,pageable,startDate,endDate);
        return APIPagingResponse.success("주문 등록에 성공하였습니다.",data.getOrderMainList(),data.getPagingCommResult());
    }



}
