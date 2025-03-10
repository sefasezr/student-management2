package com.tpe.service.business;

import com.tpe.entity.concretes.business.Lesson;
import com.tpe.exception.ConflictException;
import com.tpe.exception.ResourceNotFoundException;
import com.tpe.payload.mappers.LessonMapper;
import com.tpe.payload.messages.ErrorMessages;
import com.tpe.payload.messages.SuccessMessages;
import com.tpe.payload.request.business.LessonRequest;
import com.tpe.payload.response.ResponseMessage;
import com.tpe.payload.response.business.LessonResponse;
import com.tpe.repository.business.LessonRepository;
import com.tpe.service.helper.PageableHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final LessonMapper lessonMapper;
    private final PageableHelper pageableHelper;

    public ResponseMessage<LessonResponse> saveLesson(LessonRequest lessonRequest) {
        isLessonExistByLessonName(lessonRequest.getLessonName());

        Lesson savedLesson = lessonRepository.save(lessonMapper.mapLessonRequestToLesson(lessonRequest));
        return ResponseMessage.<LessonResponse>builder()
                .object(lessonMapper.mapLessonToLessonResponse(savedLesson))
                .message(SuccessMessages.LESSON_SAVE)
                .httpStatus(HttpStatus.CREATED)
                .build();
    }

    private boolean isLessonExistByLessonName(String lessonName){
        boolean lessonExist = lessonRepository.existsLessonByLessonNameEqualsIgnoreCase(lessonName);

        if(lessonExist){
            throw new ConflictException(String.format(ErrorMessages.LESSON_ALREADY_EXIST_WITH_LESSON_NAME, lessonName));
        } else {
            return false;
        }
    }

    public ResponseMessage deleteLesson(Long id) {

        isLessonExistById(id);
        lessonRepository.deleteById(id);
        //lessonRepository.delete(isLessonExistById(id));
        return ResponseMessage.builder()
                .message(SuccessMessages.LESSON_DELETE)
                .httpStatus(HttpStatus.OK)
                .build();
    }

    public Lesson isLessonExistById(Long id){
        return lessonRepository.findById(id).orElseThrow(()->
                new ResourceNotFoundException(String.format(ErrorMessages.NOT_FOUND_LESSON_MESSAGE, id)));
    }

    public ResponseMessage<LessonResponse> getLessonByLessonName(String lessonName) {
        if(lessonRepository.getLessonByLessonName(lessonName).isPresent()){
            return ResponseMessage.<LessonResponse>builder()
                    .message(SuccessMessages.LESSON_FOUND)
                    .object(lessonMapper.mapLessonToLessonResponse(lessonRepository.getLessonByLessonName(lessonName).get()))
                    .build();
        } else {
            return ResponseMessage.<LessonResponse>builder()
                    .message(String.format(ErrorMessages.NOT_FOUND_LESSON_MESSAGE, lessonName))
                    .build();
        }
    }

    public Page<LessonResponse> findLessonByPage(int page, int size, String sort, String type) {
        Pageable pageable = pageableHelper.getPageableWithProperties(page, size, sort, type);
        return lessonRepository.findAll(pageable).map(lessonMapper::mapLessonToLessonResponse);
    }

    public Set<Lesson> getLessonByLessonIdSet(Set<Long> idSet) {
        return idSet.stream() // Stream<Long>
                .map(this::isLessonExistById)
                .collect(Collectors.toSet());
    }

    public LessonResponse updateLessonById(Long lessonId, LessonRequest lessonRequest) {

        Lesson lesson = isLessonExistById(lessonId);
        // !!! requeste ders ismi degisti ise unique olmasi gerekiyor kontrolu
        if(
                !(lesson.getLessonName().equals(lessonRequest.getLessonName())) && // requestten gelen ders ismi DB deki ders isminden farkli ise
                        (lessonRepository.existsByLessonName(lessonRequest.getLessonName())) // Derived Query
        ) {
            throw new ConflictException(
                    String.format(ErrorMessages.ALREADY_REGISTER_LESSON_MESSAGE,lessonRequest.getLessonName()));
        }
        // !!! DTO --> POJO
        Lesson updatedLesson = lessonMapper.mapLessonRequestToUpdatedLesson(lessonId, lessonRequest);

        updatedLesson.setLessonPrograms(lesson.getLessonPrograms());

        Lesson savedLesson = lessonRepository.save(updatedLesson);

        return lessonMapper.mapLessonToLessonResponse(savedLesson);
    }

}
