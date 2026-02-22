package com.clinic.config;

import com.clinic.entity.Doctor;
import com.clinic.entity.User;
import com.clinic.repository.DoctorRepository;
import com.clinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    @Bean
    @Profile({"dev", "test"})
    public CommandLineRunner initData(DoctorRepository doctorRepository,
                                      UserRepository userRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            log.info("Initializing sample data...");

            // Create admin user
            if (!userRepository.existsByUsername("admin")) {
                userRepository.save(User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("Admin@123"))
                    .email("admin@clinic.com")
                    .role(User.Role.ADMIN)
                    .build());
            }

            // Create doctor user
            if (!userRepository.existsByUsername("doctor1")) {
                userRepository.save(User.builder()
                    .username("doctor1")
                    .password(passwordEncoder.encode("Doctor@123"))
                    .email("doctor1@clinic.com")
                    .role(User.Role.DOCTOR)
                    .build());
            }

            // Create sample doctors
            if (doctorRepository.count() == 0) {
                doctorRepository.save(Doctor.builder()
                    .nameEn("Dr. Ahmed Al-Rashid")
                    .nameAr("د. أحمد الراشد")
                    .specialty("Cardiology")
                    .yearsOfExperience(15)
                    .consultationDurationMinutes(30)
                    .build());

                doctorRepository.save(Doctor.builder()
                    .nameEn("Dr. Sarah Johnson")
                    .nameAr("د. سارة جونسون")
                    .specialty("General Practice")
                    .yearsOfExperience(8)
                    .consultationDurationMinutes(20)
                    .build());

                doctorRepository.save(Doctor.builder()
                    .nameEn("Dr. Mohammed Al-Kaabi")
                    .nameAr("د. محمد الكعبي")
                    .specialty("Orthopedics")
                    .yearsOfExperience(12)
                    .consultationDurationMinutes(45)
                    .build());

                doctorRepository.save(Doctor.builder()
                    .nameEn("Dr. Fatima Al-Mansoori")
                    .nameAr("د. فاطمة المنصوري")
                    .specialty("Pediatrics")
                    .yearsOfExperience(10)
                    .consultationDurationMinutes(25)
                    .build());
            }

            log.info("Sample data initialization complete");
        };
    }
}