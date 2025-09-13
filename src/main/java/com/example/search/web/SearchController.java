package com.example.search.web;

import com.example.search.model.Article;
import com.example.search.service.SearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService service;

    @PostMapping("/index")
    public ResponseEntity<?> index(@Valid @RequestBody Article article) throws IOException {
        String id = service.index(article);
        return ResponseEntity.ok().body("{\"id\":\"" + id + "\"}");
    }

    @GetMapping("/search")
    public ResponseEntity<List<Article>> search(@RequestParam("q") String q,
                                                @RequestParam(value = "size", defaultValue = "20") int size)
            throws IOException {
        return ResponseEntity.ok(service.search(q, size));
    }
}
