package com.tpe.service.validator;

import com.tpe.exception.BadRequestException;
import com.tpe.payload.messages.ErrorMessages;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class DateTimeValidator {

    private boolean checkTime(LocalTime start, LocalTime stop){
        return start.isAfter(stop) || start.equals(stop);
    }
    public void checkTimeWithException(LocalTime start, LocalTime stop){
        if(checkTime(start, stop)){
            throw new BadRequestException(ErrorMessages.TIME_NOT_VALID_MESSAGE);
        }
    }
}
