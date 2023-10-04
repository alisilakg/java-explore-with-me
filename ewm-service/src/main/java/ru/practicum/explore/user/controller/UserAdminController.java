package ru.practicum.explore.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.user.dto.NewUserRequest;
import ru.practicum.explore.user.dto.UserDto;
import ru.practicum.explore.user.service.UserService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.explore.validation.ValidationGroups.Create;

import java.util.List;

@RestController
@RequestMapping(path = "/admin/users")
@Validated
@RequiredArgsConstructor
@Slf4j
public class UserAdminController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Validated(Create.class) @RequestBody NewUserRequest newUserRequest) {
        log.info("Получен POST-запрос к эндпоинту: '/admin/users' на создание пользователя");
        return userService.createUser(newUserRequest);
    }

    @GetMapping
    public List<UserDto> getAllUsers(@RequestParam(required = false) List<Long> ids,
                                     @PositiveOrZero
                                     @RequestParam(defaultValue = "0") Integer from,
                                     @Positive
                                     @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получен GET-запрос к эндпоинту: '/admin/users' на получение всех пользователей");
        return userService.getListAllUsers(ids, from, size);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        log.info("Получен DELETE-запрос к эндпоинту: '/admin/users/{id}' на удаление пользователя с ID={}", id);
        userService.deleteUserById(id);
    }

}
