package kr.co.reference.searchEngine;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "searchIndex")
public class SearchIndex {
    @Id
    private String term;
    private String pNoList;
}
