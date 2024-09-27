package com.tpe.payload.response.business;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ChooseLessonProgramWithId {

    @NotNull(message = "Please select lesson program")
    @Size(min = 1,message = "lessons must not be empty")
    private Set<Long> lessonProgramId;
}
