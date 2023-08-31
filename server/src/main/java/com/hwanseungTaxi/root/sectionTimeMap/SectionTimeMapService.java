package com.hwanseungTaxi.root.sectionTimeMap;

import com.hwanseungTaxi.root.kakaoMobility.TaxiInfoEntity;
import com.hwanseungTaxi.root.mockData.entity.Leg;
import com.hwanseungTaxi.root.mockData.entity.MockEntity;
import com.hwanseungTaxi.root.mockData.entity.Station;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

@Service
public class SectionTimeMapService {

    private static final String baseUrl = "http://apis.data.go.kr/6410000/busarrivalservice/getBusArrivalItem?";
    @Value("${dataPortal.apiKey}")
    private String apiKey;

    public HashMap<String, Integer> getSectionTimeMap(String destination, MockEntity mockEntity) throws IOException, JDOMException {
        HashMap<String, Integer> hashMap = new HashMap<>();
        int sectionTime = 0;

        // 첫 출발지 대기 시간은 우선 제외하고 구현하기

        List<Leg> legs = mockEntity.metaData.plan.itineraries.get(0).legs;

        int firstWaitTime = getFirstWaitTime(destination);
        boolean isAddFirstWaitTime = false;
        for(Leg leg : legs) {
            if(leg.mode.equals("WALK")) { // 걷기 시간인 경우 전부 더하기로 진행
                sectionTime += leg.sectionTime;
                if(!isAddFirstWaitTime) {
                    sectionTime += firstWaitTime;
                    isAddFirstWaitTime = true;
                }
            }else {
                List<Station> stationList = leg.passStopList.stationList;
                for(Station station : stationList) {
                    sectionTime += station.duration;
                    hashMap.put(station.stationName, sectionTime);
                }
            }
        }
        return hashMap;
    }

    public LinkedHashMap<String, Double> getEfficiencyMap(List<TaxiInfoEntity> taxiInfoEntities, HashMap<String, Integer> sectionTimeMap) {
        LinkedHashMap<String, Double> efficiencyMap = new LinkedHashMap<>();
        for(TaxiInfoEntity taxiInfoEntity : taxiInfoEntities) {
            efficiencyMap.put(taxiInfoEntity.getStationName(), taxiInfoEntity.getTaxiFare()/(double)sectionTimeMap.get(taxiInfoEntity.getStationName()));
        }

        // 람다식을 이용하여 값 기준으로 정렬
        List<Map.Entry<String, Double>> entries = new ArrayList<>(efficiencyMap.entrySet());
        entries.sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));

        // 스트림을 사용하여 정렬된 값들을 LinkedHashMap에 재배정
        LinkedHashMap<String, Double> sortedEfficiencyMap = entries.stream()
                .collect(LinkedHashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), LinkedHashMap::putAll);

        return sortedEfficiencyMap;
    }

    private int getFirstWaitTime(String destination) throws IOException, JDOMException {
        LocalTime currentTime = LocalTime.now();

        if(destination.equals("uijeongbu")) {
            // 홍대입구역 출발 시간표 10시 ~ 13시
            List<LocalTime> hongdae = new ArrayList<>() {{
                add(LocalTime.of(10,0)); add(LocalTime.of(10,4)); add(LocalTime.of(10,7)); add(LocalTime.of(10,10)); add(LocalTime.of(10,13));
                add(LocalTime.of(10,20)); add(LocalTime.of(10,23)); add(LocalTime.of(10,26)); add(LocalTime.of(10,32)); add(LocalTime.of(10,38));
                add(LocalTime.of(10,45)); add(LocalTime.of(10,52)); add(LocalTime.of(10,58)); add(LocalTime.of(11,3)); add(LocalTime.of(11,8));
                add(LocalTime.of(11,13)); add(LocalTime.of(11,17)); add(LocalTime.of(11,27)); add(LocalTime.of(11,31)); add(LocalTime.of(11,37));
                add(LocalTime.of(11,44)); add(LocalTime.of(11,50)); add(LocalTime.of(11,56)); add(LocalTime.of(12,2)); add(LocalTime.of(12,8));
                add(LocalTime.of(12,14)); add(LocalTime.of(12,20)); add(LocalTime.of(12,26)); add(LocalTime.of(12,32)); add(LocalTime.of(12,37));
                add(LocalTime.of(12,48)); add(LocalTime.of(12,57)); add(LocalTime.of(13,3));
            }};

            LocalTime nextArrivalTime = getNextArrivalTime(hongdae, currentTime);
            Duration duration = Duration.between(currentTime, nextArrivalTime);
            long minutes = duration.toMinutes();
            return (int) minutes*60;
        } else if (destination.equals("330")) {
            // 불광역 출발 시간표 10시 ~ 13시
            List<LocalTime> bullgwang = new ArrayList<>() {{
                add(LocalTime.of(10,0)); add(LocalTime.of(10,6)); add(LocalTime.of(10,12)); add(LocalTime.of(10,19)); add(LocalTime.of(10,26));
                add(LocalTime.of(10,33)); add(LocalTime.of(10,41)); add(LocalTime.of(10,50)); add(LocalTime.of(10,57)); add(LocalTime.of(11,3));
                add(LocalTime.of(11,9)); add(LocalTime.of(11,16)); add(LocalTime.of(11,21)); add(LocalTime.of(11,28)); add(LocalTime.of(11,34));
                add(LocalTime.of(11,41)); add(LocalTime.of(11,47)); add(LocalTime.of(11,54)); add(LocalTime.of(12,0)); add(LocalTime.of(12,6));
                add(LocalTime.of(12,13)); add(LocalTime.of(12,20)); add(LocalTime.of(12,27)); add(LocalTime.of(12,34)); add(LocalTime.of(12,41));
                add(LocalTime.of(12,48)); add(LocalTime.of(12,55)); add(LocalTime.of(13,4));
            }};
            LocalTime nextArrivalTime = getNextArrivalTime(bullgwang, currentTime);
            Duration duration = Duration.between(currentTime, nextArrivalTime);
            long minutes = duration.toMinutes();
            return (int) minutes*60;
        } else if (destination.equals("gangnam")) {
            // 용인에서 강남 가는 경로 시작 정류장
            String stationId = "228000174";
            // 11시 이전과 이후의 버스 정류장 ID가 다름
            LocalTime elevenAM = LocalTime.of(11, 0);
            String routeId;
            if(currentTime.isBefore(elevenAM)) {
                routeId = "228000389";
            }else {
                routeId = "228000182";
            }
            return getXMLGyeongiBusArrivalInfo(stationId, routeId)*60;
        } else {
            return 600;
        }
    }

    public int getXMLGyeongiBusArrivalInfo(String stationId, String routeId) throws IOException, JDOMException {
        // URL 주소 생성
        StringBuilder sb = new StringBuilder();
        sb.append(baseUrl);
        sb.append("serviceKey=");
        sb.append(apiKey);
        sb.append("&stationId="+stationId);
        sb.append("&routeId="+routeId);

        // URL 연결
        URL url = new URL(sb.toString());
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

        httpURLConnection.setRequestProperty("Content-Type", "application/xml");
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.connect();

        SAXBuilder saxBuilder = new SAXBuilder();
        Document document = saxBuilder.build(httpURLConnection.getInputStream());

        Element root = document.getRootElement();
        Element msgHeader = root.getChild("msgHeader");
        Element resultMessage = msgHeader.getChild("resultMessage");

        String predictTime = null;

        if(resultMessage.getContent(0).getValue().equals("정상적으로 처리되었습니다.")) {
            Element msgBody = root.getChild("msgBody");
            Element busArrivalItem = msgBody.getChild("busArrivalItem");
            Element predictTime1 = busArrivalItem.getChild("predictTime1");

            predictTime = predictTime1.getContent(0).getValue();
        }

        return predictTime == null ? 15 : Integer.parseInt(predictTime);
    }

    private LocalTime getNextArrivalTime (List<LocalTime> localTimes, LocalTime currentTime) {
        for(int i=0; i<localTimes.size()-1; i++) {
            if(localTimes.get(i).isAfter(currentTime) && localTimes.get(i+1).isBefore(currentTime)) {
                return localTimes.get(i+1);
            }
        }
        return currentTime.plusMinutes(10); // 사이 시간이 없는 경우 기본 대기 시간
    }

}
