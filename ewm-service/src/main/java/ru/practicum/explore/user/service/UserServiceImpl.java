package ru.practicum.explore.user.service;

import io.micrometer.core.lang.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.error.exception.ConflictException;
import ru.practicum.explore.error.exception.NotFoundException;
import ru.practicum.explore.user.dto.NewUserRequest;
import ru.practicum.explore.user.dto.UserDto;
import ru.practicum.explore.user.mapper.UserMapper;
import ru.practicum.explore.user.model.User;
import ru.practicum.explore.user.repository.UserRepository;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest newUserRequest) {
        userRepository.findByName(newUserRequest.getName()).ifPresent((user) -> {
            throw new ConflictException("Пользователь с именем= " + newUserRequest.getName() + " уже существует!");
        });
        User user = UserMapper.toUser(newUserRequest);
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public List<UserDto> getListAllUsers(@Nullable List<Long> ids, Integer from, Integer size) {
        if (Objects.nonNull(ids)) {
            return UserMapper.toUserDto(userRepository.findAllById(ids));
        }
        Page<User> users = userRepository.findAll(PageRequest.of(from / size, size));
        return users.map(UserMapper::toUserDto).getContent();
    }

    @Override
    @Transactional
    public void deleteUserById(Long id) {
        getUserIfExists(id);
        userRepository.deleteById(id);
    }

    @Override
    public User findUserByIdForMapping(Long userId) {
       return getUserIfExists(userId);
    }

    private User getUserIfExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id %d не найден", userId)));
    }
}
