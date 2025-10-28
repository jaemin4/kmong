package com.kmong.interfaces.outbox;

import com.kmong.application.OrderOutBoxFacade;
import com.kmong.support.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CallBackController {

    private final OrderOutBoxFacade orderOutBoxFacade;


    @PostMapping("/api/esim/callback")
    public String receiveCallback(@RequestBody Map<String,Object> payload) throws InterruptedException {
        log.info("콜백 수신 2-5 : {}", payload);

        orderOutBoxFacade.processCallBack2_5(payload);

        return "1";
    }

    @PostMapping("/api/esim/callback/second")
    public String receiveCallback2_2(@RequestBody Map<String,Object> payload) throws InterruptedException {
        log.info("콜백 수신 2-2 : {}", JsonUtils.toJson(payload));

       orderOutBoxFacade.processCallBack2_2(payload);

        return "1";
    }

    @PostMapping("/api/esim/callback/3-2")
    public String receiveCallback3_2(
            @RequestBody Map<String,Object> payload
    ) throws InterruptedException {
        log.info("콜백 수신 3-2 : {}", JsonUtils.toJson(payload));

        orderOutBoxFacade.processCallBack3_2(payload);

        return "1";
    }


}
