package com.example.search.service;

import com.example.search.model.Article;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch._types.mapping.TextProperty;
import org.opensearch.client.opensearch._types.mapping.KeywordProperty;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.IndexResponse;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch._types.Refresh;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final OpenSearchClient client;
    private final com.example.search.config.OpenSearchConfig properties;

    @PostConstruct
    public void ensureIndex() {
        if (!properties.isCreateIndexOnStart()) return;
        final String index = properties.getIndex();
        try {
            boolean exists = client.indices().exists(e -> e.index(index)).value();
            if (!exists) {
                TypeMapping mapping = new TypeMapping.Builder()
                        .properties("title", p -> p.text(TextProperty.of(t -> t)))
                        .properties("content", p -> p.text(TextProperty.of(t -> t)))
                        .properties("tags", p -> p.keyword(KeywordProperty.of(k -> k)))
                        .build();

                client.indices().create(c -> c.index(index).mappings(mapping));
                log.info("Created index: {}", index);
            } else {
                log.info("Index already exists: {}", index);
            }
        } catch (Exception e) {
            log.warn("Index bootstrap skipped/failed (continuing). {}", e.getMessage());
        }
    }

    public String index(Article doc) throws IOException {
        final String index = properties.getIndex();
        final String id = (doc.getId() == null || doc.getId().isBlank())
                ? UUID.randomUUID().toString()
                : doc.getId();

        Article toSave = Article.builder()
                .id(id)
                .title(doc.getTitle())
                .content(doc.getContent())
                .tags(doc.getTags())
                .build();

        IndexResponse resp = client.index(i -> i
                .index(index)
                .id(id)
                .document(toSave)
                .refresh(Refresh.WaitFor) // or Refresh.True / Refresh.False
        );
        return resp.id();
    }

    public List<Article> search(String q, int size) throws IOException {
        final String index = properties.getIndex();

        // multi_match on title and content; title boosted
        Query query = new Query.Builder()
                .multiMatch(mm -> mm.query(q).fields("title^3", "content"))
                .build();

        SearchResponse<Article> resp = client.search(s -> s
                .index(index)
                .size(size)
                .query(query), Article.class);

        return resp.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList();
    }
}
