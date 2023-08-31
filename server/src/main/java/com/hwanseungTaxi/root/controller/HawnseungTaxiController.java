package com.hwanseungTaxi.root.controller;

import com.hwanseungTaxi.root.kakaoMobility.KakaoMobilityService;
import com.hwanseungTaxi.root.kakaoMobility.TaxiInfoEntity;
import com.hwanseungTaxi.root.mockData.MockDataService;
import com.hwanseungTaxi.root.mockData.entity.MockEntity;
import com.hwanseungTaxi.root.response.ResponseBodyService;
import com.hwanseungTaxi.root.response.entity.ResponseBody;
import com.hwanseungTaxi.root.sectionTimeMap.SectionTimeMapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdom2.JDOMException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class HawnseungTaxiController {

    private final MockDataService mockDataService;
    private final KakaoMobilityService kakaoMobilityService;
    private final SectionTimeMapService sectionTimeMapService;
    private final ResponseBodyService responseBodyService;

    @GetMapping("/directions")
    public ResponseEntity getDirections(@RequestParam String destination, @RequestParam int minFare, @RequestParam int maxFare) throws IOException, JDOMException {
        /**
         * 0. destination에 해당하는 json 파일 불러와서 객체로 저장해두기
         * */
        MockEntity mockEntity = mockDataService.getEntity(destination);
        /**
         * 1. 택시 요금 범위 카카오 모빌리티 API 처리 객체에 넘겨줘서 택시비 범위 내의 목적지 지점들 얻기
         * 얻은 목적지 지점들은 좌표값, 목적지명, 택시비, 이동시간을 저장하도록한다.
         * */
        List<TaxiInfoEntity> taxiInfoEntities = kakaoMobilityService.getTaxiInfoEntities(mockEntity, maxFare);
        /**
         * 2. mockEntity에서 각 구간까지의 걸리는 시간 계산해서 Map 형태로 저장
         * 가장 첫번째 정류장에서의 대기시간 추가하기
         * */
        HashMap<String, Integer> sectionTimeMap = sectionTimeMapService.getSectionTimeMap(destination, mockEntity);
        /**
         * 3. 1번에서 얻은 값에 2번에서 얻은 Map을 이용해서 효율성 구하기
         * 효율성 = 택시비(W)/대중교통이동시간(s) -> 값이 낮을수록 좋은 효율성을 가진다는 의미다.
         * 효율성 좋은 순으로 정렬해서 반환하도록 하자
         * */
        LinkedHashMap<String, Double> efficiencyMap = sectionTimeMapService.getEfficiencyMap(taxiInfoEntities, sectionTimeMap);
        /**
         * 4. 2번에서 얻은 값과 3번에서 얻은 값의 차를 구한 후 응답 객체로 변환하기
         * */
        ResponseBody responseBody = responseBodyService.getResponseBody(mockEntity, taxiInfoEntities, sectionTimeMap, efficiencyMap);
        /**
         * 5. 응답 객체 반환
         * */
        return new ResponseEntity(responseBody, HttpStatus.OK);
    }

}
