package com.hwanseungTaxi.root.response.entity;

import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.List;

@AllArgsConstructor
public class ResponseBody {

    public HashMap<String, Position> stationInfo;
    public List<Info> InfoList;

}
