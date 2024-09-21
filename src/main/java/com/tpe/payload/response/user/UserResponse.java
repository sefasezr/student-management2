package com.tpe.payload.response.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tpe.payload.response.abstracts.BaseUserResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuperBuilder
public class UserResponse extends BaseUserResponse {


}
