package com.kmong.interfaces;

import com.kmong.application.OrderOutBoxFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        log.info("콜백 수신: {}", payload);

        orderOutBoxFacade.processOfCallBack(payload);

        return "1";
    }
}
