package com.ddkolesnik.avitotraiding.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @author Alexandr Stegnin
 */

@Getter
@Setter
@MappedSuperclass
public class AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    @Column(name = "modified_time")
    private LocalDateTime modifiedTime;

    @PrePersist
    public void prePersist() {
        this.creationTime = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.modifiedTime = LocalDateTime.now();
    }

}
