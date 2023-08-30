package com.hwanseungTaxi.root.controller;

import com.hwanseungTaxi.root.mockData.MockDataService;
import com.hwanseungTaxi.root.mockData.entity.MockEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@Slf4j
@RequiredArgsConstructor
public class HawnseungTaxiController {

    private final MockDataService mockDataService;

    @GetMapping("/directions")
    public ResponseEntity getDirections(@RequestParam String destination, @RequestParam int minFare, @RequestParam int maxFare) throws IOException {
        /**
         * 0. destination에 해당하는 json 파일 불러와서 객체로 저장해두기
         * */
        MockEntity entity = mockDataService.getEntity(destination);
        /**
         * 1. 택시 요금 범위 카카오 모빌리티 API 처리 객체에 넘겨줘서 택시비 범위 내의 목적지 지점들 얻기
         * 얻은 목적지 지점들은 좌표값, 목적지명, 택시비, 이동시간을 저장하도록한다.
         * */

        /**
         * 2. 1번에서 얻은 리스트를 버스 도착 예정 시간 API 처리 객체에 넘겨줘서 택시 이동시간 후 도착했을 때의 예상 대기 시간을 얻기
         * */
        /**
         * 3. 1번에서 얻은 리스트의 목적지 지점들에 대해 대중교통 예상 이동 시간 얻기
         * 목적지 지점까지의 이동시간을 구하려면 구간 시간에 거리 비례로 계산해야할 것으로 파악됨
         * */
        /**
         * 4. 2번에서 얻은 값과 3번에서 얻은 값의 차를 구한 후 응답 객체로 변환하기
         * */
        /**
         * 5. 응답 객체 반환
         * */
        return new ResponseEntity(HttpStatus.OK);
    }

}
