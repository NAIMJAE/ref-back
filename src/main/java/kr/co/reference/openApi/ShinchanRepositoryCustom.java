package kr.co.reference.openApi;


import java.util.List;

public interface ShinchanRepositoryCustom {

    public List<Shinchan> selectShinchanList(int numOfRows, int pageNo);
}
