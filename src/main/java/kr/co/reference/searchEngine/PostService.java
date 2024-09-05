package kr.co.reference.searchEngine;

import com.querydsl.core.Tuple;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final SearchIndexRepository searchIndexRepository;
    private final KomoranIndexRepository komoranIndexRepository;
    private final ModelMapper modelMapper;

    // 게시글 작성
    @Transactional
    public int insertPost(PostDTO postDTO) {

        // 게시글 저장
        Post post = postRepository.save(modelMapper.map(postDTO, Post.class));

        // 특수문자 제거하기
        String cleanedContent = postDTO.getContents().replaceAll("[^a-zA-Z0-9가-힣\\s]", "");
        String cleanedTitle = postDTO.getTitle().replaceAll("[^a-zA-Z0-9가-힣\\s]", "");

        // 게시글 내용에서 단어 추출하기
        String[] contentsWords = cleanedContent.split("\\s+");
        String[] titleWords = cleanedTitle.split("\\s+");

        // 스톱워드를 제거한 단어를 저장할 리스트
        Map<String, Integer> wordFrequency = new HashMap<>();

        // 각 단어의 끝에 스톱워드가 붙어 있는지 확인 후 제거
        for (String word : contentsWords) {
            String filteredWord = removeStopword(word);
            if (!filteredWord.isEmpty()) {
                wordFrequency.put(filteredWord, wordFrequency.getOrDefault(filteredWord, 0) + 1);
            }
        }

        // 제목에서 단어 추출 후 중요도 증가
        for (String word : titleWords) {
            String filteredWord = removeStopword(word);
            if (!filteredWord.isEmpty()) {
                wordFrequency.put(filteredWord, wordFrequency.getOrDefault(filteredWord, 0) + 10);
            }
        }

        // 값을 내림차순으로 정렬
        List<Map.Entry<String, Integer>> sortedEntries = wordFrequency.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .toList();

        log.info("sortedEntries : " + sortedEntries);

        // searchindex 테이블에 저장
        int maxTerms = Math.min(10, sortedEntries.size());  // 최대 10개의 단어까지만 처리
        for (int i = 0; i < maxTerms; i++) {
            Optional<SearchIndex> optSearchIndex = searchIndexRepository.findById(sortedEntries.get(i).getKey());

            if (optSearchIndex.isPresent()) {
                SearchIndex searchIndex = optSearchIndex.get();
                String newList = searchIndex.getPNoList() + "_" + post.getPNo();
                searchIndex.setPNoList(newList);
                searchIndexRepository.save(searchIndex);
            } else {
                SearchIndex searchIndex = new SearchIndex();
                searchIndex.setTerm(sortedEntries.get(i).getKey());
                searchIndex.setPNoList(String.valueOf(post.getPNo()));
                searchIndexRepository.save(searchIndex);
            }
        }
        return post.getPNo();
    }

    // 단어 끝에 스톱워드가 붙어 있으면 제거하는 메서드
    private static String removeStopword(String word) {
        Set<String> stopwords = new HashSet<>(Arrays.asList("이", "은", "을", "를", "는", "가", "으로", "에", "에서", "의", "와", "과", "처럼", "에게"));
        // 단어에서 스톱워드(조사)만큼 잘라냄
        for (String stopword : stopwords) {
            if (word.endsWith(stopword)) {
                return word.substring(0, word.length() - stopword.length());
            }
        }
        // 스톱워드가 붙어 있지 않다면 원래 단어를 반환
        return word;
    }


    // 게시글 작성2 (with KOMORAN)
    public void insertPostKomoran(PostDTO postDTO) {
        Komoran komoran = new Komoran(DEFAULT_MODEL.FULL);

        KomoranResult titleResult = komoran.analyze(postDTO.getTitle());
        KomoranResult contentsResult = komoran.analyze(postDTO.getContents());

        Map<String, Integer> titleWordFrequency = resultAnalyze(titleResult);
        Map<String, Integer> contentsWordFrequency = resultAnalyze(contentsResult);

        // 제목에 포함된 단어들의 중요도 조정
        for (Map.Entry<String, Integer> entry : contentsWordFrequency.entrySet()) {
            String word = entry.getKey();
            if (titleWordFrequency.containsKey(word)) {
                contentsWordFrequency.put(word, entry.getValue() + 10);
            }
        }

        List<Map.Entry<String, Integer>> sortedEntries = contentsWordFrequency.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .toList();

        log.info("sortedEntries : " + sortedEntries);

        // komoranIndex 테이블에 저장
        int maxTerms = Math.min(10, sortedEntries.size());  // 최대 10개의 단어까지만 처리
        for (int i = 0; i < maxTerms; i++) {
            Optional<KomoranIndex> optKomoranIndex = komoranIndexRepository.findById(sortedEntries.get(i).getKey());

            if (optKomoranIndex.isPresent()) {
                KomoranIndex komoranIndex = optKomoranIndex.get();
                String newList = komoranIndex.getPNoList() + "_" + postDTO.getPNo();
                komoranIndex.setPNoList(newList);
                komoranIndexRepository.save(komoranIndex);
            } else {
                KomoranIndex komoranIndex = new KomoranIndex();
                komoranIndex.setTerm(sortedEntries.get(i).getKey());
                komoranIndex.setPNoList(String.valueOf(postDTO.getPNo()));
                komoranIndexRepository.save(komoranIndex);
            }
        }
    }

    // 형태소 분석 결과 처리 메서드
    private Map<String, Integer> resultAnalyze(KomoranResult result) {
        Map<String, Integer> wordFrequency = new HashMap<>();

        // 형태소 분석 결과에서 명사, 동사, 형용사만 추출하고 빈도수 계산
        for (Token token : result.getTokenList()) {
            String pos = token.getPos();
            String morph = token.getMorph();

            // 명사(NNG, NNP)는 그대로 추가
            if (pos.startsWith("NN")) {
                wordFrequency.put(morph, wordFrequency.getOrDefault(morph, 0) + 1);
            }
            // 동사(VV)는 어간 추출 후 기본형 "다"로 변환해서 Map에 추가
            else if (pos.equals("VV")) {
                String stem = morph.replaceAll("(는|어|아|서|고)$", "");  // 동사 어미 제거
                String lemma = stem + "다";  // 기본형 변환
                wordFrequency.put(lemma, wordFrequency.getOrDefault(lemma, 0) + 1);
            }
            // 형용사(VA)도 어간 추출 후 기본형 "다"로 변환해서 Map에 추가
            else if (pos.equals("VA")) {
                String stem = morph.replaceAll("(는|어|아|서|고)$", "");  // 형용사 어미 제거
                String lemma = stem + "다";  // 기본형 변환
                wordFrequency.put(lemma, wordFrequency.getOrDefault(lemma, 0) + 1);
            }
        }
        return wordFrequency;
    }

    // 게시글 목록 조회
    public ResponseEntity<?> selectPostList(int pg, String type, String keyword) {
        PageRequestDTO pageRequestDTO = new PageRequestDTO();
        pageRequestDTO.setPg(pg);
        pageRequestDTO.setType(type);
        pageRequestDTO.setKeyword(keyword);

        Pageable pageable = pageRequestDTO.getPageable("no");

        if (type.equals("") && keyword.equals("")) { // 검색이 아닌 경우
            Page<Tuple> tuples = postRepository.selectPostList(pageable);

            List<PostDTO> postList = tuples.getContent().stream().map(
                    tuple -> {
                        Post post = tuple.get(0, Post.class);
                        String name = tuple.get(1, String.class);
                        PostDTO postDTO = modelMapper.map(post, PostDTO.class);
                        postDTO.setName(name);
                        return postDTO;
                    }
            ).toList();

            int total = (int) tuples.getTotalElements();
            PageResponseDTO pageResponseDTO = PageResponseDTO.builder()
                    .pageRequestDTO(pageRequestDTO)
                    .dtoList(postList)
                    .total(total)
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(pageResponseDTO);
            
        }else if(!type.equals("")) { // 검색인 경우
            int totalWord = 0;
            Map<String, String[]> searchMap = new HashMap<>();

            // 일반 검색 로직
            if(type.equals("normal")) {
                String cleanedKeyword = keyword.replaceAll("[^a-zA-Z0-9가-힣\\s]", "");
                String[] contentsWords = cleanedKeyword.split("\\s+");
                Map<String, Integer> wordFrequency = new HashMap<>();

                for (String word : contentsWords) {
                    String filteredWord = removeStopword(word);
                    if (!filteredWord.isEmpty()) {
                        wordFrequency.put(filteredWord, wordFrequency.getOrDefault(filteredWord, 0) + 1);
                    }
                }
                totalWord = wordFrequency.size();

                for (String key : wordFrequency.keySet()) {
                    Optional<SearchIndex> optSearchIndex = searchIndexRepository.findById(key);
                    if (optSearchIndex.isPresent()) {
                        String[] pNoList = optSearchIndex.get().getPNoList().split("_");
                        searchMap.put(key, pNoList);
                    }
                }
            // KOMORAN 검색 로직
            }else if(type.equals("komoran")) {
                Komoran komoran = new Komoran(DEFAULT_MODEL.FULL);

                KomoranResult keywordResult = komoran.analyze(keyword);

                Map<String, Integer> keywordFrequency = resultAnalyze(keywordResult);
                totalWord = keywordFrequency.size();

                for (String key : keywordFrequency.keySet()) {
                    Optional<KomoranIndex> optKomoranIndex = komoranIndexRepository.findById(key);
                    if (optKomoranIndex.isPresent()) {
                        String[] pNoList = optKomoranIndex.get().getPNoList().split("_");
                        searchMap.put(key, pNoList);
                    }
                }
            }

            // pNo 값을 카운트하기 위한 Map 생성
            Map<String, Integer> pNoCountMap = new HashMap<>();

            // searchMap에서 pNo 리스트들을 순회하며 카운트
            for (String[] pNoList : searchMap.values()) {
                for (String pNo : pNoList) {
                    pNoCountMap.put(pNo, pNoCountMap.getOrDefault(pNo, 0) + 1);
                }
            }

            // pNo가 몇 번 등장했는지에 따라 그룹화하여 출력
            Map<Integer, List<String>> pNoGroupedByCount = new HashMap<>();
            for (Map.Entry<String, Integer> entry : pNoCountMap.entrySet()) {
                String pNo = entry.getKey();
                Integer count = entry.getValue();
                pNoGroupedByCount.computeIfAbsent(count, k -> new ArrayList<>()).add(pNo);
            }

            List<Integer> sortedKeys = new ArrayList<>(pNoGroupedByCount.keySet());
            sortedKeys.sort(Collections.reverseOrder());  // 내림차순 정렬

            // 내림차순으로 그룹화된 pNo 출력 & 연관도 계산
            List<PostDTO> postDTOList = new ArrayList<>();
            for (Integer count : sortedKeys) {
                for (String pNo : pNoGroupedByCount.get(count)) {
                    Optional<Post> optPost = postRepository.findById(Integer.parseInt(pNo));
                    PostDTO postDTO = modelMapper.map(optPost.get(), PostDTO.class);
                    double related = ((double) count / (double) totalWord) * 100;
                    related = Math.round(related * 100.0) / 100.0; // 소수점 2자리까지 반올림
                    postDTO.setRelated(related);
                    postDTOList.add(postDTO);
                }
            }

            // 페이지네이션 처리
            int start = (int) pageable.getOffset(); // 현재 페이지의 시작 인덱스
            int end = Math.min((start + pageable.getPageSize()), postDTOList.size()); // 페이지의 끝 인덱스 계산
            List<PostDTO> pagedPostList = postDTOList.subList(start, end); // 리스트의 일부분만 반환

            // 전체 페이지 수 계산
            Page<PostDTO> page = new PageImpl<>(pagedPostList, pageable, postDTOList.size());

            // 페이지 응답 객체 생성
            PageResponseDTO pageResponseDTO = PageResponseDTO.builder()
                    .pageRequestDTO(pageRequestDTO)
                    .dtoList(page.getContent())
                    .total((int) page.getTotalElements())
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(pageResponseDTO);
        }else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("BAD REQUEST");
        }
    }

    // 게시글 조회
    public ResponseEntity<?> selectPost(int pno) {

        Optional<Post> optPost = postRepository.findById(pno);

        if (optPost.isPresent()) {
            PostDTO postDTO = modelMapper.map(optPost.get(), PostDTO.class);
            return ResponseEntity.status(HttpStatus.OK).body(postDTO);
        }else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("NOT FOUND POST NUMBER");
        }
    }



    ///////////////////

    /*

        // 전체 문서 수 조회
        int totalDocuments = (int) postRepository.count();

        // 1. TF 계산 (각 단어 빈도 / 총 단어 수)
        Map<String, Double> tfMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : wordFrequency.entrySet()) {
            double tf = (double) entry.getValue() / contentsWords.length;
            tfMap.put(entry.getKey(), tf);
        }

        // 2. IDF 계산
        Map<String, Double> idfMap = computeIDF(wordFrequency, totalDocuments);

        // 3. TF-IDF 계산
        Map<String, Double> tfIdfMap = new HashMap<>();
        for (Map.Entry<String, Double> entry : tfMap.entrySet()) {
            String term = entry.getKey();

            // IDF 값이 없으면 기본 보정값 적용 (ex: log(전체 문서 수))
            double idf = idfMap.getOrDefault(term, Math.log(totalDocuments));  // IDF값이 없으면 log(전체 문서 수)로 보정

            // TF-IDF 계산
            double tfIdf = entry.getValue() * idf;
            tfIdfMap.put(term, tfIdf);
        }

        // 4. TF-IDF 값을 내림차순으로 정렬
        List<Map.Entry<String, Double>> sortedTfIdfEntries = tfIdfMap.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .toList();

        log.info("TF-IDF sorted : " + sortedTfIdfEntries);
    */
    // IDF 계산 메서드
    private Map<String, Double> computeIDF(Map<String, Integer> wordFrequency, int totalDocuments) {
        Map<String, Double> idfMap = new HashMap<>();

        // 각 문서에서 단어를 추출해 등장하는지 체크
        for (String word : wordFrequency.keySet()) {
            Optional<SearchIndex> searchIndex = searchIndexRepository.findById(word);

            if (searchIndex.isPresent()) {
                String[] pNoArray = searchIndex.get().getPNoList().split("_");
                int documentCount = pNoArray.length;  // 단어가 등장한 문서 수

                // IDF 계산: log(전체 문서 수 / (단어가 등장한 문서 수 + 1))
                double idf = Math.log((double) totalDocuments / (1 + documentCount));
                idfMap.put(word, idf);
            } else {
                // 전체 문서 수에 비례한 기본 IDF를 부여해 의미 있는 TF-IDF 값 생성
                double idf = Math.log((double) totalDocuments + 1);  // 전체 문서 수에 대한 기본 보정값 적용
                idfMap.put(word, idf);
            }
        }

        return idfMap;
    }
}
