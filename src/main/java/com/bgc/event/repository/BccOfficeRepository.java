package com.bgc.event.repository;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.repository
 * - File       : BccOfficeRepository.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * </pre>
 */

import com.bgc.event.entity.BccOffice;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BccOfficeRepository extends JpaRepository<BccOffice, Long> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    Optional<BccOffice> findByCode(String code);

    @EntityGraph(attributePaths = "staff")
    List<BccOffice> findAllByOrderByCountryAscNameAsc();

    List<BccOffice> findByActiveTrue();

    List<BccOffice> findByCountryIgnoreCaseOrderByNameAsc(String country);

    @Query("SELECT DISTINCT o.country FROM BccOffice o ORDER BY o.country ASC")
    List<String> findAllCountries();

    @Query("SELECT COUNT(u) FROM User u WHERE u.office.id = :officeId")
    long countStaffByOfficeId(Long officeId);

    List<BccOffice> findAll();
}
