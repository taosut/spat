package com.pcalouche.spat.restservices.api.service;

import com.pcalouche.spat.restservices.api.AbstractSpatServiceImpl;
import com.pcalouche.spat.restservices.api.dto.UserDto;
import com.pcalouche.spat.restservices.api.entity.User;
import com.pcalouche.spat.restservices.api.repository.RoleRepository;
import com.pcalouche.spat.restservices.api.repository.UserRepository;
import com.pcalouche.spat.restservices.security.util.SecurityUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class UserServiceImpl extends AbstractSpatServiceImpl implements UserService {
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserServiceImpl(ModelMapper modelMapper, UserRepository userRepository, RoleRepository roleRepository) {
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDto findByUsername(String username) {
        User user = userRepository.findByUsername(username);
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public List<UserDto> findAll() {
        List<UserDto> userDtos = new ArrayList<>();
        userRepository.findAll().forEach(user -> userDtos.add(modelMapper.map(user, UserDto.class)));
        return userDtos;
    }

    @Override
    public UserDto save(UserDto userDto) {
        // Don't allow saves of duplicate usernames.
        if (userDto.getId() == null && userRepository.findByUsername(userDto.getUsername()) != null) {
            throw new IllegalArgumentException(String.format("A user with a username of %s already exists", userDto.getUsername()));
        }

        User userToSave = modelMapper.map(userDto, User.class);
        // Retain password if saving an existing user
        Optional<User> existingUser;
        if (userDto.getId() != null) {
            existingUser = userRepository.findById(userDto.getId());
            if (existingUser.isPresent()) {
                existingUser.ifPresent(user1 -> userToSave.setPassword(user1.getPassword()));
            } else {
                userToSave.setPassword(SecurityUtils.PASSWORD_ENCODER.encode("password"));
            }
        }

        if (userToSave.getRoles().isEmpty()) {
            userToSave.setRoles(Stream.of(roleRepository.findByName("ROLE_USER"))
                    .collect(Collectors.toSet()));
        }

        return modelMapper.map(userRepository.save(userToSave), UserDto.class);
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}