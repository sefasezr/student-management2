package com.tpe.controller.business;

import com.tpe.entity.concretes.business.Meet;
import com.tpe.payload.request.business.MeetRequest;
import com.tpe.payload.response.ResponseMessage;
import com.tpe.payload.response.business.MeetResponse;
import com.tpe.service.business.MeetService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController //restApi için ve mapleme için bunu yazarız component yerine
@RequestMapping("/meet")
@RequiredArgsConstructor
public class MeetController {

    private final MeetService meetService;

    @PostMapping("/save") // http://localhost:8080/meet/save + POST + JSON
    @PreAuthorize("hasAnyAuthority('TEACHER')")
    public ResponseMessage<MeetResponse> saveMeet(HttpServletRequest httpServletRequest, @RequestBody @Valid MeetRequest meetRequest) {
        return meetService.saveMeet(httpServletRequest,meetRequest);
    }

    @GetMapping("/getAll")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public List<MeetResponse> getAll(){
        return meetService.getAll();
    }

    @GetMapping("/getByMeetId/{meetId}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseMessage<MeetResponse> getByMeetId(@PathVariable Long meetId){
        return meetService.getMeetById(meetId);
    }

    @GetMapping("/getAllMeetByPage")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public Page<MeetResponse> getAllMeetByPage(
            @RequestParam(value = "page")int page,
            @RequestParam(value = "size") int size
    ){
        return meetService.getAllMeetByPage(page,size);
    }

    @GetMapping("/getAllMeetByAdvisorAsPage")
    @PreAuthorize("hasAnyAuthority('TEACHER')")
    public ResponseEntity<Page<MeetResponse>> getAllMeetByTeacher(HttpServletRequest httpServletRequest,
                                                       @RequestParam(value = "page")int page,
                                                       @RequestParam(value = "size")int size){
        return meetService.getAllMeetByTeacher(httpServletRequest,page,size);
    }

    @DeleteMapping("delete/{meetId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','TEACHER')")
    public ResponseMessage delete(@PathVariable Long meetId,HttpServletRequest httpServletRequest){
        return meetService.delete(meetId,httpServletRequest);
    }

    @GetMapping("/getAllMeetByStudent")
    @PreAuthorize("hasAnyAuthority('STUDENT')")
    public List<MeetResponse> getAllMeetByStudent(HttpServletRequest httpServletRequest){
        return meetService.getAllMeetByStudent(httpServletRequest);
    }
}
