package com.example.search.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Article {
    private String id;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private List<String> tags;
}
