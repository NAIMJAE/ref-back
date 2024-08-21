package kr.co.reference.signUp;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "user")
public class User {
    @Id
    private String uid;
    private String password;
    private String name;
    private String role;
    private String serviceKey;
    @CreationTimestamp
    private LocalDate rdate;
}
