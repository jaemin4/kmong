package com.kmong.domain.user;

import com.kmong.aop.log.AfterCommitLogger;
import com.kmong.aop.log.RequestFlowLogger;
import com.kmong.support.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public UserResult.Register register(UserCommand.Register command) {
        validDuplicateEmail(command.getEmail());
        validDuplicatePhoneNumber(command.getPhoneNumber());
        String encodedPassword = bCryptPasswordEncoder.encode(command.getPassword());
        User saved =  userRepository.save(command.toEntity(encodedPassword));

        AfterCommitLogger.logInfoAfterCommit(() ->
                String.format("[%s] saved User: %s", RequestFlowLogger.getCurrentUUID(), JsonUtils.toJson(saved))
        );

        return UserResult.Register.of(saved);
    }

    private void validDuplicateEmail(String email){
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("이미 등록된 이메일입니다.");
        }
    }

    private void validDuplicatePhoneNumber(String phoneNumber){
        if(userRepository.findByPhoneNumber(phoneNumber).isPresent()){
            throw new RuntimeException("이미 등록된 휴대전화입니다.");
        }
    }




}
