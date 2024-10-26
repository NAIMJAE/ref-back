package kr.co.reference.webAnalytics.VisitorTracking;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "vt_visit_log")
public class VisitLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vt_id")
    private Long id;

    @Column(name = "vt_date")
    private LocalDate date;

    @Column(name = "vt_time")
    private LocalTime time;

    @Column(name = "vt_Language")
    private String language;

    @Column(name = "vt_referer")
    private String referer;

    @Column(name = "vt_agent")
    private String vtAgent;

    @Column(name = "vt_city")
    private String vtCity;

    @Column(name = "vt_country")
    private String vtCountry;

    @Column(name = "vt_device")
    private String vtDevice;
}
