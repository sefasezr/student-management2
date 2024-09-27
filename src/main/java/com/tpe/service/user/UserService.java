package com.tpe.service.user;

import com.tpe.entity.concretes.user.User;
import com.tpe.entity.concretes.user.UserRole;
import com.tpe.entity.enums.RoleType;
import com.tpe.exception.BadRequestException;
import com.tpe.exception.ConflictException;
import com.tpe.exception.ResourceNotFoundException;
import com.tpe.payload.mappers.UserMapper;
import com.tpe.payload.messages.ErrorMessages;
import com.tpe.payload.messages.SuccessMessages;
import com.tpe.payload.request.user.UserRequest;
import com.tpe.payload.request.user.UserRequestWithoutPassword;
import com.tpe.payload.response.ResponseMessage;
import com.tpe.payload.response.abstracts.BaseUserResponse;
import com.tpe.payload.response.user.UserResponse;
import com.tpe.repository.user.UserRepository;
import com.tpe.service.helper.MethodHelper;
import com.tpe.service.helper.PageableHelper;
import com.tpe.service.validator.UniquePropertyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UniquePropertyValidator uniquePropertyValidator;
    private final UserMapper userMapper;
    private final UserRoleService userRoleService;
    private final PasswordEncoder passwordEncoder;
    private final PageableHelper pageableHelper;
    private final MethodHelper methodHelper;

    public ResponseMessage<UserResponse> saveUser(UserRequest userRequest, String userRole) {

        // !!! Girilen username - ssn- phoneNumber-email unique mi kontrolu
        uniquePropertyValidator.checkDuplicate(
                userRequest.getUsername(),
                userRequest.getSsn(),
                userRequest.getPhoneNumber(),
                userRequest.getEmail()
        );

        // !!! DTO -- > POJO
        User user = userMapper.mapUserRequestToUser(userRequest);
        // !!! Rol bilgisi setleniyor
        if(userRole.equalsIgnoreCase(RoleType.ADMIN.name())){
            if(Objects.equals(userRequest.getUsername(), "Admin")){
                user.setBuilt_in(true);
            }
            user.setUserRole(userRoleService.getUserRole(RoleType.ADMIN));
        } else if (userRole.equalsIgnoreCase("Dean")) {
            user.setUserRole(userRoleService.getUserRole(RoleType.MANAGER));
        } else if (userRole.equalsIgnoreCase("ViceDean")) {
            user.setUserRole(userRoleService.getUserRole(RoleType.ASSISTANT_MANAGER));
        } else {
            throw  new ResourceNotFoundException(String.format(ErrorMessages.NOT_FOUND_USER_ROLE_MESSAGE, userRole));
        }
        //!!! password encode ediliyor
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // !!! advisor degeri setleniyor( Admin-Manager ve AsstManager larin advisor olma ihtimali yok)
        user.setIsAdvisor(Boolean.FALSE);

        User savedUser = userRepository.save(user);
        return ResponseMessage.<UserResponse>builder()
                .message(SuccessMessages.USER_CREATED)
                .object(userMapper.mapUserToUserResponse(savedUser))
                .build();

    }

    public Page<UserResponse> getUsersByPage(int page, int size, String sort, String type, String userRole) {

        Pageable pageable = pageableHelper.getPageableWithProperties(page, size, sort, type);
        return userRepository.findByUserByRole(userRole, pageable).map(userMapper::mapUserToUserResponse);
    }

    public ResponseMessage<BaseUserResponse> getUserById(Long userId) {

        BaseUserResponse baseUserResponse;
        User user = userRepository.findById(userId).orElseThrow(()->
                new ResourceNotFoundException(String.format(ErrorMessages.NOT_FOUND_USER_MESSAGE, userId)));

        // UserResponse --> Admin , Manager, Assistant_Manager
        // TeacherResponse --> Teacher
        // StudentResponse --> Student
        if(user.getUserRole().getRoleType() == RoleType.STUDENT){
            baseUserResponse = userMapper.mapUserToStudentResponse(user);
        } else if (user.getUserRole().getRoleType() == RoleType.TEACHER) {
            baseUserResponse = userMapper.mapUserToTeacherResponse(user);
        } else {
            baseUserResponse = userMapper.mapUserToUserResponse(user);
        }

        return ResponseMessage.<BaseUserResponse>builder()
                .message(SuccessMessages.USER_FOUND)
                .httpStatus(HttpStatus.OK)
                .object(baseUserResponse)
                .build();
    }
    // Not : deleteUser() **********************************************************
    public String deleteUserById(Long id, HttpServletRequest request) {
        // silinecek user var mi kontrolu
        User user = methodHelper.isUserExist(id); // silinmesi istenen user
        // metodu tetikleyen user role bilgisi aliniyor
        String userName = (String) request.getAttribute("username");
        User user2 = userRepository.findByUsername(userName); // silme islemini talep eden user
        // builtIn kontrolu
        if (Boolean.TRUE.equals(user.getBuilt_in())) { // condition : user.getBuilt_in()
            throw new ConflictException(ErrorMessages.NOT_PERMITTED_METHOD_MESSAGE);
            // MANAGER sadece Teacher, student, Assistant_Manager silebilir
        } else if (user2.getUserRole().getRoleType() == RoleType.MANAGER) {
            if (!(  (user.getUserRole().getRoleType() == RoleType.TEACHER) ||
                    (user.getUserRole().getRoleType() == RoleType.STUDENT) ||
                    (user.getUserRole().getRoleType() == RoleType.ASSISTANT_MANAGER) )) {
                throw new BadRequestException(ErrorMessages.NOT_PERMITTED_METHOD_MESSAGE);
            }
            // Mudur Yardimcisi sadece Teacher veya Student silebilir
        } else if (user2.getUserRole().getRoleType() == RoleType.ASSISTANT_MANAGER) {
            if (!((user.getUserRole().getRoleType() == RoleType.TEACHER) ||
                    (user.getUserRole().getRoleType() == RoleType.STUDENT))) {
                throw new BadRequestException(ErrorMessages.NOT_PERMITTED_METHOD_MESSAGE);
            }
        }
        userRepository.deleteById(id);
        return SuccessMessages.USER_DELETE;
    }
    public ResponseMessage<BaseUserResponse> updateUser(UserRequest userRequest, Long userId) {

        User user = methodHelper.isUserExist(userId);
        // !!! bulit_in kontrolu
        methodHelper.checkBuiltIn(user);
        //!!! update isleminde gelen request de unique olmasi gereken eski datalar hic degismedi ise
        // dublicate kontrolu yapmaya gerek yok :
        uniquePropertyValidator.checkUniqueProperties(user, userRequest);
        //!!! DTO --> POJO
        User updatedUser = userMapper.mapUserRequestToUpdatedUser(userRequest, userId);
        // !!! Password Encode
        updatedUser.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        updatedUser.setUserRole(user.getUserRole());
        User savedUser = userRepository.save(updatedUser);

        return ResponseMessage.<BaseUserResponse>builder()
                .message(SuccessMessages.USER_UPDATE_MESSAGE)
                .httpStatus(HttpStatus.OK)
                .object(userMapper.mapUserToUserResponse(savedUser))
                .build();
    }
    // Not: updateUserForUser() **********************************************************
    public ResponseEntity<String> updateUserForUsers(UserRequestWithoutPassword userRequest,
                                                     HttpServletRequest request) {
        String userName = (String) request.getAttribute("username");
        User user = userRepository.findByUsername(userName);

        // !!! bulit_in kontrolu
        methodHelper.checkBuiltIn(user);

        // !!! unique kontrolu
        uniquePropertyValidator.checkUniqueProperties(user, userRequest);
        // !!! DTO --> pojo donusumu burada yazildi
        user.setUsername(userRequest.getUsername());
        user.setBirthDay(userRequest.getBirthDay());
        user.setEmail(userRequest.getEmail());
        user.setPhoneNumber(userRequest.getPhoneNumber());
        user.setBirthPlace(userRequest.getBirthPlace());
        user.setGender(userRequest.getGender());
        user.setName(userRequest.getName());
        user.setSurname(userRequest.getSurname());
        user.setSsn(userRequest.getSsn());

        userRepository.save(user);

        String message = SuccessMessages.USER_UPDATE;

        return ResponseEntity.ok(message);
    }


    // Not : getByName() ***************************************************************
    public List<UserResponse> getUserByName(String name) {

        return userRepository.getUserByNameContaining(name)
                .stream()
                .map(userMapper::mapUserToUserResponse)
                .collect(Collectors.toList());
    }

    public long countAllAdmins(){
        return userRepository.countAdmin(RoleType.ADMIN);
    }

    public User getTeacherByUsername(String teacherUsername){
        return userRepository.findByUsername(teacherUsername);
    }

    public User getUserByUserId(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(()->new ResourceNotFoundException(ErrorMessages.NOT_FOUND_USER_MESSAGE));
    }

    public List<User> getStudentById(Long[]studentIds){
        return userRepository.findByIdsEquals(studentIds);
    }
}
