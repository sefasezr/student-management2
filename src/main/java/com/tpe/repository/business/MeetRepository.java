package com.tpe.repository.business;

import com.tpe.entity.concretes.business.Meet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository//buraya yazdığımız repository sayesinde exception fırlattığında hibernate bize açıklayıcı bir bilgi verir
public interface MeetRepository extends JpaRepository<Meet, Long> {
    List<Meet> getByAdvisoryTeacher_IdEquals(Long advisoryTeacherId);

    List<Meet> findByStudentList_IdEquals(Long studentId);

    Page<Meet> findByAdvisoryTeacher_IdEquals(Long id, Pageable pageable);
}
