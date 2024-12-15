package com.nova.narrativa.domain.template.entity;

import com.nova.narrativa.domain.template.enums.Genre;
import com.nova.narrativa.domain.template.enums.TemplateType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "templates")
@Getter @Setter
@NoArgsConstructor
public class Template {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Genre genre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TemplateType type;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
}
