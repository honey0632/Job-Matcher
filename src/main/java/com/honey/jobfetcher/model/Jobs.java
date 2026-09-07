// Models a job listing persisted and returned by the application.

package com.honey.jobfetcher.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
public class Jobs {
    public static final int STATUS_NO_ACTION = 0;
    public static final int STATUS_APPLIED = 1;
    public static final int STATUS_REJECTED = -1;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(columnDefinition = "TEXT", unique = true, nullable = false)
    private String externalId;
    @Column(columnDefinition = "TEXT")
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(columnDefinition = "TEXT")
    private String company;
    @Column(columnDefinition = "TEXT")
    private String location;
    @Column(columnDefinition = "TEXT")
    private String jobUrl;
    @Column(columnDefinition = "TEXT")
    private String source;
/*
    Status ={0 = no action,
              1 = applied
              -1 =rejected}
 */
    private Integer status = STATUS_NO_ACTION;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
