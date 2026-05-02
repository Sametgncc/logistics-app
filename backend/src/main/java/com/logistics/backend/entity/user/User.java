package com.logistics.backend.entity.user;

import com.logistics.backend.entity.business.Load;
import com.logistics.backend.entity.business.RoutePlan;
import com.logistics.backend.entity.business.Vehicle;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String lastName;

    private String username;

    private String password;

    @Email(message = "Geçerli bir mail adresi giriniz")
    @NotBlank(message = "Mail adresi boş olamaz")
    @Column(unique = true, nullable = false)
    private String email;

    private String phone;
    private String address;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private RoleType role;

    private boolean emailVerified = false;

    private String verificationCode;

    private LocalDateTime verificationCodeExpire;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Vehicle> vehicles = new ArrayList<>();

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL)
    @Builder.Default
    private List<RoutePlan> routePlans = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Load> loads = new ArrayList<>();

    @Builder.Default
    private Boolean driverApproved = false;

}