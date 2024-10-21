package kr.co.reference.webAnalytics.VisitorTracking;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VisitLogMapper {
    // 방문자 기기 Count
    public List<Map<String, Object>> selectDeviceCounts();

    // 방문자 방문 국가 Count
    public List<CountryDTO> selectCountryCounts();

    // 방문자 방문 지역 Count
    public List<RegionDTO> selectRegionCounts();
}
