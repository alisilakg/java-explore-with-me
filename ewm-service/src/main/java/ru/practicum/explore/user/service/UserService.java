package ru.practicum.explore.user.service;

import ru.practicum.explore.user.dto.NewUserRequest;
import ru.practicum.explore.user.dto.UserDto;
import ru.practicum.explore.user.model.User;

import java.util.List;

public interface UserService {
    UserDto createUser(NewUserRequest newUserRequest);

    List<UserDto> getListAllUsers(List<Long> ids, Integer from, Integer size);

    void deleteUserById(Long id);

    User findUserByIdForMapping(Long initiatorId);
}
