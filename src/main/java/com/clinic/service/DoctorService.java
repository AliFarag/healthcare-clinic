package com.clinic.service;

import com.clinic.dto.response.DoctorResponse;
import java.util.List;

public interface DoctorService {
    List<DoctorResponse> getAllActiveDoctors();
    DoctorResponse getDoctorById(Long id);
}