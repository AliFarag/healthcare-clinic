package com.clinic.service.impl;

import com.clinic.dto.response.DoctorResponse;
import com.clinic.entity.Doctor;
import com.clinic.exception.ResourceNotFoundException;
import com.clinic.repository.DoctorRepository;
import com.clinic.service.DoctorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;

    @Override
    @Cacheable(value = "doctors", key = "'all-active'")
    public List<DoctorResponse> getAllActiveDoctors() {
        log.debug("Fetching all active doctors");
        return doctorRepository.findByActiveTrue().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "doctors", key = "#id")
    public DoctorResponse getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));
        return toResponse(doctor);
    }

    @Override
    @Cacheable(value = "doctors", key = "'specialty-' + #specialty")
    public List<DoctorResponse> getDoctorsBySpecialty(String specialty) {
        return doctorRepository.findBySpecialtyIgnoreCase(specialty).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    private DoctorResponse toResponse(Doctor doctor) {
        return DoctorResponse.builder()
            .id(doctor.getId())
            .nameEn(doctor.getNameEn())
            .nameAr(doctor.getNameAr())
            .specialty(doctor.getSpecialty())
            .yearsOfExperience(doctor.getYearsOfExperience())
            .consultationDurationMinutes(doctor.getConsultationDurationMinutes())
            .active(doctor.getActive())
            .build();
    }
}