package com.kmong.interfaces.order;

import com.kmong.domain.order.OrderCommand;
import com.kmong.domain.order.OrderService;
import com.kmong.support.response.APIResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
