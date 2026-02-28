package com.bgc.event.service;

import com.bgc.event.dto.BccOfficeDto;
import com.bgc.event.entity.BccOffice;
import com.bgc.event.entity.User;

import java.util.List;
import java.util.Optional;

public interface BccOfficeService {
    BccOffice create(BccOfficeDto dto);

    BccOffice update(Long id, BccOfficeDto dto);

    void delete(Long id);

    Optional<BccOffice> findById(Long id);

    List<BccOffice> findAll();

    List<BccOffice> findActive();

    List<String> findAllCountries();

    List<User> findStaffByOffice(Long officeId);

    void assignUserToOffice(Long userId, Long officeId);

    void removeUserFromOffice(Long userId);

    void toggleActive(Long id);

    long countStaff(Long officeId);
}
