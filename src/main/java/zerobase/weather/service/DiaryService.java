package zerobase.weather.service;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.WeatherApplication;
import zerobase.weather.domain.DateWeather;
import zerobase.weather.domain.Diary;
import zerobase.weather.repository.DateWeatherRepository;
import zerobase.weather.repository.DiaryRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DiaryService {
    @Value("${openweathermap.key}")
    private String apiKey;

    private final DiaryRepository diaryRepository;
    private final DateWeatherRepository dateWeatherRepository;
    private static final Logger logger = LoggerFactory.getLogger(WeatherApplication.class);

    public DiaryService(DiaryRepository diaryRepository, DateWeatherRepository dateWeatherRepository) {
        this.diaryRepository = diaryRepository;
        this.dateWeatherRepository = dateWeatherRepository;
    }

    @Transactional
    @Scheduled(cron = "0 0 1 * * *")
    public void saveWeatherDate() {
        dateWeatherRepository.save(getWeatherFromApi());
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void createDiary(LocalDate date, String text) {
        logger.info("started to create diary");
        /*
         * 세부기능
         * open weather map에서 날씨 데이터 가져오기  = getWeatherString()
         * 받아온 날씨 json 파싱하기 = parseWeather(weatherData)
         * 파싱된 데이터 + 일기 값을 레퍼지토리를 통해 DB에 넣기
         */

        //String weatherData = getWeatherString();
        // Map<String, Object> parseWeather = parseWeather(weatherData);

        // 날씨 데이터 가져오기(API에서 가져오기? or DB에서 가져오기?)
        DateWeather dateWeather = getDateWeather(date);

        Diary nowDiary = new Diary();
        nowDiary.setDateWeather(dateWeather);
        nowDiary.setText(text);
        diaryRepository.save(nowDiary);
        logger.info("end to create diary");
    }

    private DateWeather getWeatherFromApi() {
        String weatherData = getWeatherString();
        Map<String, Object> parseWeather = parseWeather(weatherData);
        DateWeather dateWeather = new DateWeather();

        dateWeather.setDate(LocalDate.now());
        dateWeather.setWeather(parseWeather.get("main").toString());
        dateWeather.setIcon(parseWeather.get("icon").toString());
        dateWeather.setTemperature((Double) parseWeather.get("temp"));

        return dateWeather;
    }

    private DateWeather getDateWeather(LocalDate date) {
        List<DateWeather> dateWeatherListFromDB = dateWeatherRepository.findAllByDate(date);
        if(dateWeatherListFromDB.isEmpty()) {
            // 새로 api에서 날씨 정보를 가져와야한다.
            // 정책상,,, 현재 날씨를 가져오도록 하거나,, 날씨없이 일기를 쓰도륵,,
            return getWeatherFromApi();
        }else {
            return dateWeatherListFromDB.get(0);
        }
    }
    @Transactional(readOnly = true)
    public List<Diary> readDiary(LocalDate date) {
        return diaryRepository.findAllByDate(date);
    }

    public List<Diary> readDiaries(LocalDate startDate, LocalDate endDate) {
        return diaryRepository.findAllByDateBetween(startDate, endDate);
    }

    public void updateDiary(LocalDate date, String text) {
        Diary nowDiary = diaryRepository.getFirstByDate(date);
        nowDiary.setText(text);
        diaryRepository.save(nowDiary);

    }

    public void deleteDiary(LocalDate date) {
        diaryRepository.deleteAllByDate(date);
    }

    private String getWeatherString() {
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=seoul&appid=" + apiKey;
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET"); // apiUrl을 http 형식으로 연결하고, Get으로 요청
            int responseCode = connection.getResponseCode(); // 200, 400..
            BufferedReader br;
            if(responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            }else {
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }
            String inputLine;
            StringBuilder response = new StringBuilder();
            while((inputLine = br.readLine()) != null ) {
                response.append(inputLine);
            }

            br.close();
            return response.toString();

        } catch (Exception e) {
            return "failed to get response";
        }
    }

    private Map<String, Object> parseWeather(String jsonString){
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;

        try {
            jsonObject = (JSONObject) jsonParser.parse(jsonString);
        }catch(ParseException e) {
            throw new RuntimeException();
        }

        Map<String, Object> resultMap = new HashMap<>();

        JSONObject mainData = (JSONObject) jsonObject.get("main"); // main 객체의 데이터를 가져옴
        resultMap.put("temp", mainData.get("temp"));
        JSONArray weatherArray = (JSONArray) jsonObject.get("weather");
        JSONObject weatherData = (JSONObject) weatherArray.get(0); // 리스트 값이 1개라 가능
        resultMap.put("main" , weatherData.get("main"));
        resultMap.put("icon" , weatherData.get("icon"));
        return resultMap;
    }
}
