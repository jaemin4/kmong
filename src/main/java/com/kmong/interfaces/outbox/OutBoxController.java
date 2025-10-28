package com.kmong.interfaces.outbox;

import com.kmong.aop.log.RequestFlowLogger;
import com.kmong.application.OrderOutBoxFacade;
import com.kmong.scheduler.OrderApiCall;
import com.kmong.support.response.APIResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/infra")
public class OutBoxController {

    private final OrderOutBoxFacade orderOutBoxFacade;
    private final OrderApiCall orderApiCall;


    @PostMapping("/resend/email/{orderId}")
    public APIResponse<Void> resendEmail(
            @PathVariable String orderId)
    {
        log.info("[{}] /infra/resend/email/{}", RequestFlowLogger.getCurrentUUID(),orderId);

        orderOutBoxFacade.resendEmail(orderId);

        return APIResponse.success();
    }

    @PostMapping("/resend/sms/{orderId}")
    public APIResponse<Void> resendSms(@PathVariable String orderId) {
        log.info("[{}] /infra/resend/sms/{}", RequestFlowLogger.getCurrentUUID(),orderId);

        orderOutBoxFacade.resendSms(orderId);

        return APIResponse.success();
    }

    @PostMapping("/resend/order/local/{orderId}")
    public APIResponse<Void> resendOrderLocal(
            @PathVariable String orderId
    ) throws InterruptedException {
        log.info("[{}] /resend/order/local/{}", RequestFlowLogger.getCurrentUUID(),orderId);

        orderOutBoxFacade.resendOrderLocal(orderId);

        return APIResponse.success();
    }


    // 2.4 주문 재시도
}
