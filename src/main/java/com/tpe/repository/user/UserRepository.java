package com.tpe.repository.user;

import com.tpe.entity.concretes.user.User;
import com.tpe.entity.enums.RoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsernameEquals(String username);

    boolean existsByUsername(String username);

    boolean existsBySsn(String ssn);

    boolean existsByPhoneNumber(String phone);

    boolean existsByEmail(String email);

    @Query("select u from User u where u.userRole.roleName = :roleName")
    Page<User> findByUserByRole(String roleName, Pageable pageable);

    User findByUsername(String userName);

    List<User> getUserByNameContaining(String name);

    @Query(value = "select count(u) from User u where u.userRole.roleType = ?1")
    long countAdmin(RoleType roleType);

    List<User> findByAdvisorTeacherId(Long id);

    @Query("SELECT u from User u where u.isAdvisor =?1")
    List<User> findAllByAdvisor(Boolean aTrue);

    @Query(value = "select max (u.studentNumber) from User u")
    int getMaxStudentNumber();

    @Query(value = "select (count (u)>0) from User u where u.userRole.roleType = ?1")
    boolean findStudent(RoleType roleType);

}
