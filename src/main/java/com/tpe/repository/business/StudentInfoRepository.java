package com.tpe.repository.business;

import com.tpe.entity.concretes.business.StudentInfo;
import jdk.dynalink.linker.LinkerServices;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StudentInfoRepository extends JpaRepository<StudentInfo, Long> {
    List<StudentInfo> getAllByStudentId_Id(Long studentId);

    @Query("select s from StudentInfo s where s.teacher.username = ?1")
    Page<StudentInfo> findByTeacherId_UsernameEquals(String username, Pageable pageable);

    @Query("select s from StudentInfo s where s.student.username = ?1")
    Page<StudentInfo> findByStudentId_UsernameEquals(String username, Pageable pageable);
}
