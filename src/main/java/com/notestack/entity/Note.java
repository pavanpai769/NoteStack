package com.notestack.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document( collection = "notes")
@Data
@Builder
@EqualsAndHashCode
public class Note {
    @Id
    private ObjectId id;
    @NonNull
    private String title;
    @NonNull
    private String content;

}
