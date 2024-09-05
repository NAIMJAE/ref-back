package kr.co.reference.searchEngine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@Controller
public class PostController {

    private final PostService postService;
    
    // 게시글 작성
    @PostMapping("/se/post")
    public ResponseEntity<?> insertPost(@RequestBody PostDTO postDTO) {

        int pno = postService.insertPost(postDTO);
        postDTO.setPNo(pno);
        postService.insertPostKomoran(postDTO);
        return ResponseEntity.status(HttpStatus.OK).body(pno);
    }

    // 게시글 목록 조회
    @GetMapping("/se/post")
    public ResponseEntity<?> selectPostList(@RequestParam int pg, @RequestParam String type, @RequestParam String keyword) {

        return postService.selectPostList(pg, type, keyword);
    }

    // 게시글 조회
    @GetMapping("/se/post/{pno}")
    public ResponseEntity<?> selectPost(@PathVariable int pno) {

        return postService.selectPost(pno);
    }

}
