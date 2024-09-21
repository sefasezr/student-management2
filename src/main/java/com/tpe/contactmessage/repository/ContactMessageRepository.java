package com.tpe.contactmessage.repository;

import com.tpe.contactmessage.entity.ContactMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    Page<ContactMessage> findByEmailEquals(String email, Pageable pageable);

    Page<ContactMessage>findBySubjectEquals(String subject, Pageable pageable);

    @Query("select c from ContactMessage c where FUNCTION('DATE', c.dateTime) between ?1 and ?2")
    List<ContactMessage> findMessagesBetweenDates(LocalDate beginDate, LocalDate endDate);
}
