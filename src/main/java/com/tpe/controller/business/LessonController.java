package com.tpe.controller.business;

import com.tpe.entity.concretes.business.Lesson;
import com.tpe.payload.request.business.LessonRequest;
import com.tpe.payload.response.ResponseMessage;
import com.tpe.payload.response.business.LessonResponse;
import com.tpe.service.business.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Set;

@RestController
@RequestMapping("/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @PostMapping("/save") // http://localhost:8080/lessons/save  + POST + JSON
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER')")
    public ResponseMessage<LessonResponse> saveLesson(@RequestBody @Valid LessonRequest lessonRequest){
        return lessonService.saveLesson(lessonRequest);
    }

    @DeleteMapping("/delete/{id}")// http://localhost:8080/lessons/delete/2
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER')")
    public ResponseMessage deleteLesson(@PathVariable Long id){
        return lessonService.deleteLesson(id);
    }

    @GetMapping("/getLessonByName") // http://localhost:8080/lessons/getLessonByName?lessonName=java
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER')")
    public ResponseMessage<LessonResponse> getLessonByLessonName(@RequestParam String lessonName){
        return lessonService.getLessonByLessonName(lessonName);
    }

    @GetMapping("/findLessonByPage") // http://localhost:8080/lessons/findLessonByPage?page=0&size=10&sort=lessonName&type=desc
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER')")
    public Page<LessonResponse> findLessonByPage(
            @RequestParam(value = "page") int page,
            @RequestParam(value = "size") int size,
            @RequestParam(value = "sort") String sort,
            @RequestParam(value = "type") String type
    ){
        return lessonService.findLessonByPage(page,size,sort,type);
    }

    @GetMapping("/getAllLessonByLessonId")// http://localhost:8080/lessons/getAllLessonByLessonId?lessonId=1,2,3
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER')")
    public Set<Lesson> getAllLessonByLessonId(@RequestParam(name = "lessonId") Set<Long> idSet){
        return lessonService.getLessonByLessonIdSet(idSet);
    }

    // Not: ODEVVV UpdateById() *************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER')")
    @PutMapping("/update/{lessonId}") // http://localhost:8080/lessons/update/1
    public ResponseEntity<LessonResponse> updateLessonById(@PathVariable Long lessonId,
                                                           @RequestBody LessonRequest lessonRequest){
        return ResponseEntity.ok(lessonService.updateLessonById(lessonId, lessonRequest));
    }
}
