package com.tpe.repository.business;

import com.tpe.entity.concretes.business.Meet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository//buraya yazdığımız repository sayesinde exception fırlattığında hibernate bize açıklayıcı bir bilgi verir
public interface MeetRepository extends JpaRepository<Meet, Long> {
}
