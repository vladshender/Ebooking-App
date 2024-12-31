package com.example.ebooking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@Entity
@Table(name = "accommodations")
@NoArgsConstructor
@SQLDelete(sql = "UPDATE accommodations SET is_deleted "
        + "= true WHERE id=?")
@SQLRestriction("is_deleted = false")
public class Accommodation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String size;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Amenities> amenities = new HashSet<>();

    @Column(nullable = false)
    private BigDecimal dailyRate;

    @Column(nullable = false)
    private Integer availability;

    @Column(nullable = false)
    private boolean isDeleted = false;

    public enum Type {
        HOUSE,
        APARTMENT,
        CONDO,
        VACATION_HOME
    }

    public enum Amenities {
        Air_Conditioning,
        WiFi,
        Swimming_Pool,
        BBQ_Grill,
        Dishwasher,
        Garage,
        Fireplace,
        Elevator,
        Parking,
        Heating,
        Balcony
    }
}
