package com.kmong.interfaces;

import com.kmong.support.response.APIResponse;
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


    @PostMapping("/api/esim/callback")
    public String receiveCallback(@RequestBody Map<String,Object> payload){


        log.info("콜백 수신: {}", payload);
        return "1";
    }
}
