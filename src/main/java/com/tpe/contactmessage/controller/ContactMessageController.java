package com.tpe.contactmessage.controller;

import com.tpe.contactmessage.dto.ContactMessageRequest;
import com.tpe.contactmessage.dto.ContactMessageResponse;
import com.tpe.contactmessage.entity.ContactMessage;
import com.tpe.contactmessage.service.ContactMessageService;
import com.tpe.payload.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/contactMessages")
@RequiredArgsConstructor
public class ContactMessageController {

    private final ContactMessageService contactMessageService;

    @PostMapping("/save") // http://localhost:8080/contactMessages/save  + POST
    public ResponseMessage<ContactMessageResponse> saveContact(@RequestBody @Valid ContactMessageRequest contactMessageRequest){
        return contactMessageService.save(contactMessageRequest);
    }

    @GetMapping("/getAll") // http://localhost:8080/contactMessages/getAll + GET
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER')")
    public Page<ContactMessageResponse> getAll(
          @RequestParam(value = "page", defaultValue = "0") int page,
          @RequestParam(value = "size", defaultValue = "10") int size,
          @RequestParam(value = "sort", defaultValue = "dateTime") String sort,
          @RequestParam(value = "type", defaultValue = "desc") String type
    ){
        return contactMessageService.getAll(page,size,sort,type);
    }

    @GetMapping("/searchByEmail") // http://localhost:8080/contactMessages/searchByEmail?email=aaa@bbb.com
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER')")
    public Page<ContactMessageResponse> searchByEmail(
            @RequestParam(value = "email") String email,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "dateTime") String sort,
            @RequestParam(value = "type", defaultValue = "desc") String type
    ){
        return contactMessageService.searchByEmail(email,page,size,sort,type);
    }

    // Not: *************************************** searchBySubject *******************************
    @GetMapping("/searchBySubject")// http://localhost:8080/contactMessages/searchBySubject?subject=deneme
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER')")
    public Page<ContactMessageResponse> searchBySubject(
            @RequestParam(value = "subject") String subject,
            @RequestParam(value = "page",defaultValue = "0") int page,
            @RequestParam(value = "size",defaultValue = "10") int size,
            @RequestParam(value = "sort",defaultValue = "dateTime") String sort,
            @RequestParam(value = "type", defaultValue = "desc") String type){
        return contactMessageService.searchBySubject(subject,page,size,sort,type);
    }

    // Not: ODEVVV    searchByDateBetween ***************************************
    @GetMapping("/searchBetweenDates") // http://localhost:8080/contactMessages/searchBetweenDates?beginDate=2023-09-13&endDate=2023-09-15
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER')")
    public ResponseEntity<List<ContactMessage>> searchByDateBetween(
            @RequestParam(value = "beginDate") String beginDateString,
            @RequestParam(value = "endDate") String endDateString){
        List<ContactMessage>contactMessages = contactMessageService.searchByDateBetween(beginDateString, endDateString);
        return ResponseEntity.ok(contactMessages);
    }

    // Not: *********************************** getByIdWithParam ***************************************
    @GetMapping("/getByIdParam") //http://localhost:8080/contactMessages/getByIdParam?contactMessageId=1
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER')")
    public ResponseEntity<ContactMessage> getById(@RequestParam(value = "contactMessageId") Long contactMessageId){
        return ResponseEntity.ok(contactMessageService.getContactMessageById(contactMessageId));
    }

    // Not: ************************************ getByIdWithPath ***************************************
    @GetMapping("/getById/{contactMessageId}")//http://localhost:8080/contactMessages/getById/1
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER')")
    public ResponseEntity<ContactMessage> getByIdPath(@PathVariable Long contactMessageId){
        return ResponseEntity.ok(contactMessageService.getContactMessageById(contactMessageId));
    }

    // Not: *********************************** deleteByIdParam ***************************************
    @DeleteMapping("/deleteByIdParam")  //http://localhost:8080/contactMessages/deleteByIdParam?contactMessageId=1
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER')")
    public ResponseEntity<String> deleteById(@RequestParam(value = "contactMessageId") Long contactMessageId){
        return ResponseEntity.ok(contactMessageService.deleteById(contactMessageId));
    }

    // Not: ***************************************** deleteById ***************************************
    @DeleteMapping("/deleteById/{contactMessageId}")//http://localhost:8080/contactMessages/deleteById/2
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER')")
    public ResponseEntity<String> deleteByIdPath(@PathVariable Long contactMessageId){
        return ResponseEntity.ok(contactMessageService.deleteById(contactMessageId));
    }
}
