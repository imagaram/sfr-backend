package com.sfr.tokyo.sfr_backend.entity;

import com.sfr.tokyo.sfr_backend.user.User;
import com.sfr.tokyo.sfr_backend.entity.CouncilRole;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "council_members")
public class CouncilMember {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "term_start", nullable = false)
    private LocalDate termStart;

    @Column(name = "term_end")
    private LocalDate termEnd;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CouncilRole role;

    @Column(length = 1000)
    private String notes;
}
