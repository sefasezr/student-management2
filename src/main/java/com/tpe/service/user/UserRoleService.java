package com.tpe.service.user;

import com.tpe.entity.concretes.user.UserRole;
import com.tpe.entity.enums.RoleType;
import com.tpe.exception.ResourceNotFoundException;
import com.tpe.payload.messages.ErrorMessages;
import com.tpe.repository.user.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;

    public UserRole getUserRole(RoleType roleType){
        return userRoleRepository.findByEnumRoleEquals(roleType).orElseThrow(
                ()-> new ResourceNotFoundException(ErrorMessages.ROLE_NOT_FOUND)
        );
    }

    public List<UserRole> getAllUserRole(){
        return userRoleRepository.findAll();
    }
}
