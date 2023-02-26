package com.dragon.weather.service;

import com.dragon.weather.dto.WeatherDto;
import com.dragon.weather.dto.WeatherAPIResponseDto;
import com.dragon.weather.entity.Weather;
import com.dragon.weather.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

@RequiredArgsConstructor
@Slf4j
@Service
public class WeatherService {

    private final WeatherRepository weatherRepository;

    /**
     * 초단기예보 요청 URL
     */
    private final String baseUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";

    /**
     * 초단기예보 요청 secret key
     */
    private final String secertKey = "8M8yDLSqJiSVYp%2BdJZZeFv6GT3AKhy8z%2B9BQNy%2BZHdbgt4QvSELBBcdLldbylW3k65c4NrxhfNddK3q%2BCq6WxA%3D%3D";

    /**
     * 월계동 X좌표
     */
    private final String nx = "61";

    /**
     * 월계동 Y좌표
     */
    private final String ny = "128";

    private final String dataType = "JSON";

    public WeatherDto get() throws UnsupportedEncodingException {

        log.info("service start");
        List<WeatherAPIResponseDto> responseDtoList = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        sb.append(baseUrl); // baseURL set
        List<String> queries = this.queryParamList();

        for (int i = 0; i < queries.size(); i++) {
            if (i == 0) {
                sb.append("?").append(queries.get(i));
            }
            else {
                sb.append("&").append(queries.get(i));
            }
        }

        try {
            URL url = new URL(sb.toString());
            log.info("req url : " + sb.toString());
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setConnectTimeout(5000); // 서버 연결 Time Out
            httpURLConnection.setReadTimeout(5000); // 데이터 read Time Out
            httpURLConnection.setRequestMethod("GET"); // 서버 Request
            httpURLConnection.setRequestProperty("Content-type", "application/json");

            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                sb = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "utf-8"));
                String line;

                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();

                JSONObject responseData = new JSONObject(sb.toString());
                JSONObject response = responseData.getJSONObject("response");
                JSONObject header = response.getJSONObject("header");

                // 응답코드 정상 아닐 경우 Null 반환
                if (!header.get("resultCode").equals("00")) {
                    return null;
                }

                JSONArray item = response.getJSONObject("body").getJSONObject("items").getJSONArray("item");
                log.info(item.toString());

                for (int i = 0; i < item.length(); i++) {
                    JSONObject jsonObject = item.getJSONObject(i);

                    WeatherAPIResponseDto builder = WeatherAPIResponseDto.builder()
                            .baseDate(jsonObject.getString("baseDate"))
                            .fcstTime(jsonObject.getString("fcstTime"))
                            .fcstValue(jsonObject.getString("fcstValue"))
                            .nx(jsonObject.getInt("nx"))
                            .ny(jsonObject.getInt("ny"))
                            .category(jsonObject.getString("category"))
                            .baseTime(jsonObject.getString("baseTime"))
                            .fcstDate(jsonObject.getString("fcstDate"))
                            .build();

                    responseDtoList.add(builder);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException je) {
            je.printStackTrace();
            throw new JSONException(je.getMessage());
        }

        WeatherDto weatherDto = null;

        if (responseDtoList.size() > 0) {
            weatherDto = this.calculateWeatherInfo(responseDtoList);
        }

        if (weatherDto != null) {
            Weather weather = Weather.builder()
                    .highestTemperature(weatherDto.getHighestTemperature())
                    .minimumTemperature(weatherDto.getMinimumTemperature())
                    .nx(weatherDto.getNx())
                    .ny(weatherDto.getNy())
                    .ptyState(weatherDto.getPtyState())
                    .date(weatherDto.getDate())
                    .build();

            this.weatherRepository.save(weather);
        }

        return weatherDto;
    }

    private List<String> queryParamList() throws UnsupportedEncodingException {

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Date today = new Date();
        String todayStr = format.format(today);

        /* query param set */
        List<String> paramList = new ArrayList<>();
        paramList.add(URLEncoder.encode("serviceKey","UTF-8") + "=" + this.secertKey);
        paramList.add(URLEncoder.encode("numOfRows","UTF-8") + "=" + "218");
        paramList.add(URLEncoder.encode("pageNo","UTF-8") + "=" + "1");
        paramList.add(URLEncoder.encode("dataType","UTF-8") + "=" + this.dataType);
        paramList.add(URLEncoder.encode("base_date","UTF-8") + "=" + todayStr);
        paramList.add(URLEncoder.encode("base_time","UTF-8") + "=" + "0500");
        paramList.add(URLEncoder.encode("nx","UTF-8") + "=" + this.nx);
        paramList.add(URLEncoder.encode("ny","UTF-8") + "=" + this.ny);

        return paramList;
    }

    private WeatherDto calculateWeatherInfo(List<WeatherAPIResponseDto> list) {

        /**
         * 최고기온, 최저기온, 위치, 날짜, 날씨
         */
        double highestTmp = Integer.MIN_VALUE;
        double minimumTmp = Integer.MAX_VALUE;
        Integer nx = list.get(0).getNx();
        Integer ny = list.get(0).getNy();
        Map<String, Integer> ptyMap = new HashMap<>(); // 강수형태
        String ptyState = null;
        String date = list.get(0).getBaseDate();

        // init ptyMap
        for (int i = 0; i < 5; i++) {
            ptyMap.put(Integer.toString(i), 0);
        }

        for (WeatherAPIResponseDto item : list) {

            if (item.getCategory().equals("TMX") ||
                item.getCategory().equals("TMN") ||
                item.getCategory().equals("TMP")) {
                // 기온
                double currentTmp = Double.parseDouble(item.getFcstValue());

                if (currentTmp > highestTmp) {
                    highestTmp = currentTmp;
                }

                if (currentTmp < minimumTmp) {
                    minimumTmp = currentTmp;
                }
            }

            // 강수형태
            if (item.getCategory().equals("PTY")) {
                /* 0 : 없음 */
                /* 1 : 비 */
                /* 2 : 비/눈 */
                /* 3 : 눈 */
                /* 4 : 소나기 */
                ptyMap.put(item.getFcstValue(), ptyMap.getOrDefault(item.getCategory(), 0) + 1);
            }
        }

        // 하늘
        String maxPtyKey = "0";
        int maxPtyValue = 0;

        for (int i = 1; i < 5; i++) {
            int frequency = ptyMap.get(Integer.toString(i));

            if (frequency > maxPtyValue) {
                maxPtyValue = frequency;
                maxPtyKey = Integer.toString(i);
            }
        }

        switch (maxPtyKey) {

            case "0":
                ptyState = "강수강우없음";
                break;
            case "1":
                ptyState = "비";
                break;
            case "2":
                ptyState = "눈비";
                break;
            case "3":
                ptyState = "눈";
                break;
            case "4":
                ptyState = "소나기";
                break;
            default:
                ptyState = "없음";
                break;
        }

        // dto 생성
        WeatherDto weatherDto = WeatherDto.builder()
                .highestTemperature(highestTmp)
                .minimumTemperature(minimumTmp)
                .nx(nx)
                .ny(ny)
                .ptyState(ptyState)
                .date(date)
                .build();

        return weatherDto;
    }
}
