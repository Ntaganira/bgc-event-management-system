package com.bgc.event.service.impl;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.service.impl
 * - File       : BccOfficeServiceImpl.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * </pre>
 */

import com.bgc.event.dto.BccOfficeDto;
import com.bgc.event.entity.BccOffice;
import com.bgc.event.entity.User;
import com.bgc.event.repository.BccOfficeRepository;
import com.bgc.event.repository.UserRepository;
import com.bgc.event.service.BccOfficeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class BccOfficeServiceImpl implements BccOfficeService {

    private final BccOfficeRepository officeRepository;
    private final UserRepository      userRepository;

    @Override
    public BccOffice create(BccOfficeDto dto) {
        String code = dto.getCode().toUpperCase().trim();
        if (officeRepository.existsByCode(code))
            throw new RuntimeException("Office code already exists: " + code);

        return officeRepository.save(toEntity(new BccOffice(), dto));
    }

    @Override
    public BccOffice update(Long id, BccOfficeDto dto) {
        BccOffice office = officeRepository.findById(id).orElseThrow();
        String code = dto.getCode().toUpperCase().trim();
        if (officeRepository.existsByCodeAndIdNot(code, id))
            throw new RuntimeException("Office code already used: " + code);
        return officeRepository.save(toEntity(office, dto));
    }

    @Override
    public void delete(Long id) {
        // Unlink all users before deleting
        userRepository.findAll().stream()
            .filter(u -> u.getOffice() != null && u.getOffice().getId().equals(id))
            .forEach(u -> { u.setOffice(null); userRepository.save(u); });
        officeRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BccOffice> findById(Long id) {
        return officeRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BccOffice> findAll() {
        return officeRepository.findAllByOrderByCountryAscNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BccOffice> findActive() {
        return officeRepository.findByActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findAllCountries() {
        return officeRepository.findAllCountries();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findStaffByOffice(Long officeId) {
        return userRepository.findAll().stream()
            .filter(u -> u.getOffice() != null && u.getOffice().getId().equals(officeId))
            .toList();
    }

    @Override
    public void assignUserToOffice(Long userId, Long officeId) {
        User user = userRepository.findById(userId).orElseThrow();
        BccOffice office = officeRepository.findById(officeId).orElseThrow();
        user.setOffice(office);
        userRepository.save(user);
    }

    @Override
    public void removeUserFromOffice(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setOffice(null);
        userRepository.save(user);
    }

    @Override
    public void toggleActive(Long id) {
        officeRepository.findById(id).ifPresent(office -> {
            office.setActive(!office.isActive());
            officeRepository.save(office);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public long countStaff(Long officeId) {
        return officeRepository.countStaffByOfficeId(officeId);
    }

    // ── private helpers ───────────────────────────────────────────────────────
    private BccOffice toEntity(BccOffice office, BccOfficeDto dto) {
        office.setCode(dto.getCode().toUpperCase().trim());
        office.setName(dto.getName().trim());
        office.setCountry(dto.getCountry().trim());
        office.setCity(dto.getCity().trim());
        office.setAddress(dto.getAddress());
        office.setPhone(dto.getPhone());
        office.setEmail(dto.getEmail());
        office.setHeadOfOffice(dto.getHeadOfOffice());
        office.setActive(dto.isActive());
        return office;
    }
}
