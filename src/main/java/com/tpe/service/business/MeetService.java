package com.tpe.service.business;

import com.tpe.entity.concretes.business.Meet;
import com.tpe.entity.concretes.user.User;
import com.tpe.entity.enums.RoleType;
import com.tpe.exception.BadRequestException;
import com.tpe.exception.ConflictException;
import com.tpe.exception.ResourceNotFoundException;
import com.tpe.payload.mappers.MeetMapper;
import com.tpe.payload.messages.ErrorMessages;
import com.tpe.payload.messages.SuccessMessages;
import com.tpe.payload.request.business.MeetRequest;
import com.tpe.payload.response.ResponseMessage;
import com.tpe.payload.response.business.MeetResponse;
import com.tpe.repository.business.MeetRepository;
import com.tpe.service.helper.MethodHelper;
import com.tpe.service.helper.PageableHelper;
import com.tpe.service.user.TeacherService;
import com.tpe.service.user.UserService;
import com.tpe.service.validator.DateTimeValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service //şuan sadede okunabilirlik için component yerine kullanılıyor
@RequiredArgsConstructor
public class MeetService {
    private final MeetRepository meetRepository;
    private final UserService userService;
    private final MethodHelper methodHelper;
    private final DateTimeValidator dateTimeValidator;
    private final MeetMapper meetMapper;
    private final PageableHelper pageableHelper;

    public ResponseMessage<MeetResponse> saveMeet(HttpServletRequest httpServletRequest, MeetRequest meetRequest) {

        String username = (String) httpServletRequest.getAttribute("username");
        User advisorTeacher = userService.getTeacherByUsername(username);
        methodHelper.checkAdvisor(advisorTeacher);
        dateTimeValidator.checkTimeWithException(meetRequest.getStartTime(),meetRequest.getStopTime());

        checkMeetConflict(advisorTeacher.getId(),meetRequest.getDate(),meetRequest.getStartTime(),meetRequest.getStopTime());

        for(Long studentId : meetRequest.getStudentIds()) {
            User student = methodHelper.isUserExist(studentId);
            methodHelper.checkRole(student, RoleType.STUDENT);
            checkMeetConflict(studentId,meetRequest.getDate(),meetRequest.getStartTime(),meetRequest.getStopTime());
        }

        List<User> students = userService.getStudentById(meetRequest.getStudentIds());
        Meet meet = meetMapper.mapMeetRequestToMeet(meetRequest);

        meet.setStudentList(students);
        meet.setAdvisoryTeacher(advisorTeacher);
        Meet savedMeet = meetRepository.save(meet);

        return ResponseMessage.<MeetResponse>builder()
                .message(SuccessMessages.MEET_SAVE)
                .object(meetMapper.mapMeetToMeetResponse(savedMeet))
                .httpStatus(HttpStatus.CREATED)
                .build();
    }

    private void checkMeetConflict(Long userId, LocalDate date, LocalTime startTime,LocalTime stopTime){
        List<Meet> meets;

        if(Boolean.TRUE.equals(userService.getUserByUserId(userId).getIsAdvisor())){
            meets = meetRepository.getByAdvisoryTeacher_IdEquals(userId);
        }else meets = meetRepository.findByStudentList_IdEquals(userId);

        for (Meet meet : meets) {
            LocalTime existingStartTime = meet.getStartTime();
            LocalTime existingStopTime = meet.getStopTime();

            if(meet.getDate().equals(date) &&
                    (
                            (startTime.isAfter(existingStartTime) && startTime.isBefore(existingStopTime)) ||
                                    (stopTime.isAfter(existingStartTime) && stopTime.isBefore(existingStopTime)) ||
                                    (startTime.isBefore(existingStartTime) && stopTime.isAfter(existingStopTime)) ||
                                    (startTime.equals(existingStartTime) || stopTime.equals(existingStopTime))
                    )
            ){
                throw new ConflictException(ErrorMessages.MEET_HOURS_CONFLICT);
            }
        }

    }

    public List<MeetResponse> getAll() {
        return meetRepository.findAll()
                .stream()
                .map(meetMapper::mapMeetToMeetResponse)
                .collect(Collectors.toList());
    }

    public ResponseMessage<MeetResponse> getMeetById(Long meetId) {
        return ResponseMessage.<MeetResponse>builder()
                .message(SuccessMessages.MEET_FOUND)
                .httpStatus(HttpStatus.OK)
                .object(meetMapper.mapMeetToMeetResponse(isMeetExistById(meetId)))
                .build();

    }
    public Meet isMeetExistById(Long meetId) {
        return meetRepository.findById(meetId)
                .orElseThrow(()-> new ResourceNotFoundException(String.format(ErrorMessages.MEET_NOT_FOUND_MESSAGE,meetId)));
    }

    public Page<MeetResponse> getAllMeetByPage(int page, int size) {
        Pageable pageable = pageableHelper.getPageableWithProperties(page,size);
        return meetRepository.findAll(pageable)
                .map(meetMapper::mapMeetToMeetResponse);
    }

    public ResponseEntity<Page<MeetResponse>> getAllMeetByTeacher(HttpServletRequest httpServletRequest,
                                                                  int page,
                                                                  int size){
        String userName = (String) httpServletRequest.getAttribute("username");
        User advisoryTeacher = userService.getTeacherByUsername(userName);
        methodHelper.checkAdvisor(advisoryTeacher);

        Pageable pageable = pageableHelper.getPageableWithProperties(page,size);
        return ResponseEntity.ok(meetRepository.findByAdvisoryTeacher_IdEquals(advisoryTeacher.getId(), pageable)
                .map(meetMapper::mapMeetToMeetResponse));
    }

    public ResponseMessage delete(Long meetId, HttpServletRequest httpServletRequest) {
        Meet meet = isMeetExistById(meetId);
        isTeacherControl(meet,httpServletRequest);
        meetRepository.deleteById(meetId);

        return ResponseMessage.builder()
                .message(SuccessMessages.MEET_DELETE)
                .httpStatus(HttpStatus.OK)
                .build();
    }

    private void isTeacherControl(Meet meet, HttpServletRequest httpServletRequest){
        String userName =(String)httpServletRequest.getAttribute("username");
        User teacher = methodHelper.isUserExistByUsername(userName);

        if(
                (teacher.getUserRole().getRoleType().equals(RoleType.TEACHER)) &&
                        !(meet.getAdvisoryTeacher().getId().equals(teacher.getId()))
        ){
            throw new BadRequestException(ErrorMessages.NOT_PERMITTED_METHOD_MESSAGE);
        }
    }


    public List<MeetResponse> getAllMeetByStudent(HttpServletRequest httpServletRequest) {
        String username = (String) httpServletRequest.getAttribute("username");
        User student = methodHelper.isUserExistByUsername(username);
        methodHelper.checkRole(student, RoleType.STUDENT);  // bu kontrole gerek yok ama yine de koydum

        return meetRepository.findByStudentList_IdEquals(student.getId())
                .stream()
                .map(meetMapper::mapMeetToMeetResponse)
                .collect(Collectors.toList());
    }

    public ResponseMessage<MeetResponse> updateMeet(MeetRequest meetRequest, Long meetId, HttpServletRequest httpServletRequest) {
        Meet meet = isMeetExistById(meetId);
        isTeacherControl(meet,httpServletRequest);
        dateTimeValidator.checkTimeWithException(meetRequest.getStartTime(),
                meetRequest.getStopTime());

        if(
                !(meet.getDate().equals(meetRequest.getDate()) &&
                  meet.getStartTime().equals(meetRequest.getStartTime()) &&
                  meet.getStopTime().equals(meetRequest.getStopTime())
                )
        ){
            // !!! Student icin cakisma kontrolu
            for(Long studentId : meetRequest.getStudentIds()){
                checkMeetConflict(studentId,
                        meetRequest.getDate(),
                        meetRequest.getStartTime(),
                        meetRequest.getStopTime());
            }
            // !!! Teacher icin cakisma kontrolu
            checkMeetConflict(meet.getAdvisoryTeacher().getId(),
                    meetRequest.getDate(),
                    meetRequest.getStartTime(),
                    meetRequest.getStopTime());
        }

        List<User> students = userService.getStudentById(meetRequest.getStudentIds());
        Meet updatedMeet = meetMapper.mapMeetUpdatedRequestToMeet(meetRequest,meetId);
        updatedMeet.setStudentList(students);
        updatedMeet.setAdvisoryTeacher(meet.getAdvisoryTeacher());

        Meet savedMeet = meetRepository.save(updatedMeet);

        return ResponseMessage.<MeetResponse>builder()
                .message(SuccessMessages.MEET_UPDATE)
                .httpStatus(HttpStatus.OK)
                .object(meetMapper.mapMeetToMeetResponse(savedMeet))
                .build();
    }
}
