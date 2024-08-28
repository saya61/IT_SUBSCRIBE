//package com.sw.journal.journalcrawlerpublisher.controller;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Objects;
//
//@RestController
//@RequiredArgsConstructor
//@EnableRedisRepositories
//public class RedisController {
//    private RedisTemplate<String, String> redisTemplate;
//
//    @Value("${spring.data.redis.host}") // Redis 서버 호스트
//    private String host;
//
//    @Value("${spring.data.redis.port}") // Redis 서버 포트
//    private String port;
//
//    // 아래는 관리자 동작으로, 특정 서비스 로직과는 분리하면 좋음
//    // 여기에는 void로 Scheduled 사용 후에 함수를 형변환하여 service으로 만들어 관리자가 수동으로도 사용할 수 있도록 만듬
////    @Scheduled(cron = "* */5 * * * * ")
//    public void healthCheck() {
//        // redis health check 용 함수 호출을 3회까지 시도하며 상태 반환
////        Map<String, String> healthResponse = new HashMap<>();
//        // tries : 1 / health : true
//        // tries : 3 / health : false
//        // tries : 3 / health : restart triggered
//        boolean isHealthy = false;
//        int tries = 0;
//        while (!isHealthy && tries < 3) {
//            // incr 명령으로 바꾸기 (키 없으면 생성하고 있으면 카운트 올리는 구문)
//            // exp 타임 구간 정해서 시간 내 restart 횟수 측정
//            // or exp 없이 날짜 키 사용해서 카운트 계속 올리면 시스템 추이 모니터링 가능
//            String response = redisTemplate.getConnectionFactory().getConnection().ping();
//            if("PONG".equalsIgnoreCase(response)) {
//                isHealthy = true;
//            }
//            else{
//                tries++;
//            }
////            redisTemplate.opsForHash().put("health-check", "tries", tries);
////            isHealthy = redisTemplate.opsForHash().hasKey(
////                    "health-check",
////                    // 아래 연산 바꾸기
////                    Objects.requireNonNull(redisTemplate.getConnectionFactory())
////            );
//        }
//        // 아래 레디스 시스템 구동은 별로 URL 호출로 분리하면 좋음
//        if (!isHealthy) {
//            //shellScript 호출해서 redis upstart 수행
//            // windows 환경에서 호출 안됨
//            try{
//                String command = "redis-cli -h "+host+" -p "+port;
//                Process process = Runtime.getRuntime().exec(command);
//                process.waitFor();
//            }
//            catch (Exception e){
//                e.printStackTrace(); // 예외 스택 트레이스를 콘솔에 출력
//            }
//        }
////        return new ResponseEntity<>(
////                healthResponse,
////                isHealthy? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE
////        );
//    }
//
//
//}
