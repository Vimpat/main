package org.scoalaonline.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoalaonline.api.DTO.RegisterForm;
import org.scoalaonline.api.exception.role.RoleNotFoundException;
import org.scoalaonline.api.exception.user.*;
import org.scoalaonline.api.model.Role;
import org.scoalaonline.api.model.User;
import org.scoalaonline.api.repository.RoleRepository;
import org.scoalaonline.api.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Contains the User related logic needed for the API
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService implements ServiceInterface<User>, UserDetailsService {
  private static final String DEFAULT_ROLE = "ROLE_STUDENT";

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Retrieves User entry with given username
   * Creates Spring Security UserDetails based on User entry
   * @param username
   * @return UserDetails object
   * @throws UsernameNotFoundException
   */
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByUsername(username).orElseThrow(
      () -> new UsernameNotFoundException("Method loadUserByUsername: User not found")
    );

    log.info("User {} found in te database", username);

    Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
    user.getRoles().forEach(role -> {
      authorities.add(new SimpleGrantedAuthority(role.getName()));
    });

    return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);
  }

  /**
   * Retrieves a list of all User entries found in the DB
   * @return the list of User entries
   */
  @Override
  public List<User> getAll() {
    return userRepository.findAll();
  }

  /**
   * Retrieves one User entry with the given id from the DB
   * or throws an error if no entry with that id is found.
   * @param id - id of the User entry
   * @return the User entry
   * @throws UserNotFoundException
   */
  @Override
  public User getOneById(String id) throws UserNotFoundException {
    return userRepository.findById(id).orElseThrow(
      () -> new UserNotFoundException("Method getOneById: User not found")
    );
  }

  /**
   * Retrieves one User entry with the given username from the DB
   * or throws an error if no entry with that username is found.
   * @param username - username of the User entry
   * @return the User entry
   * @throws UserNotFoundException
   */
  public User getOneByUsername(String username) throws UserNotFoundException{
    log.info("Fetching user {}", username);
    return userRepository.findByUsername(username).orElseThrow(
      () -> new UserNotFoundException("Method getOneByUsername: User not found")
    );
  }

  /**
   * Retrieves a list of all User entries found in the DB who have the Role given by roleName
   * @param roleName - name of the Role entry
   * @throws RoleNotFoundException
   * @return the list of User entries
   */
  public List<User> getAllByRole(String roleName) {
    List<User> users = userRepository.findAllByRolesContaining(roleName);
    return users;
  }

  /**
   * Adds a User entry in the DB based on the received object.
   * @param entry the User entry.
   * @throws UserInvalidNameException
   * @throws UserInvalidUsernameException
   * @throws UserInvalidPasswordException
   * @throws UserInvalidRolesException
   * @return the User object that has been saved in the DB
   */
  @Override
  public User add(User entry) throws UserInvalidNameException,
    UserInvalidUsernameException,
    UserInvalidPasswordException,
    UserInvalidRolesException {
    User userToSave = new User();

    if(entry.getName() != null && !entry.getName().equals(""))
      userToSave.setName(entry.getName());
    else
      throw new UserInvalidNameException("Method add: Name field can't be null.");

    if(entry.getUsername() != null && !entry.getUsername().equals(""))
      userToSave.setUsername(entry.getUsername());
    else
      throw new UserInvalidUsernameException("Method add: Username field can't be null.");

    if(entry.getPassword() != null && !entry.getPassword().equals(""))
      userToSave.setPassword(passwordEncoder.encode(entry.getPassword()));
    else
    throw new UserInvalidPasswordException("Method add: Password field can't be null.");

    if(entry.getRoles() != null && !entry.getRoles().isEmpty())
      userToSave.setRoles(entry.getRoles());
    else
      throw new UserInvalidRolesException("Method add: Roles field can't be null.");

    return userRepository.save(userToSave);
  }

  /**
   * Adds a User entry in the DB based on the received RegisterForm.
   * Adds ROLE_STUDENT to the list of roles
   * @param entry the User entry.
   * @throws UserInvalidNameException
   * @throws UserInvalidUsernameException
   * @throws UserUsernameAlreadyUsedException
   * @throws UserInvalidPasswordException
   * @throws RoleNotFoundException
   * @return the User object that has been saved in the DB
   */
  public User register(RegisterForm entry) throws UserInvalidNameException,
    UserInvalidUsernameException,
    UserUsernameAlreadyUsedException,
    UserInvalidPasswordException,
    RoleNotFoundException {
    User userToSave = new User();

    if(entry.getName() != null && !entry.getName().equals(""))
      userToSave.setName(entry.getName());
    else
      throw new UserInvalidNameException("Method register: Name field can't be null.");

    if(entry.getUsername() != null && !entry.getUsername().equals("")){
      if (!userRepository.existsByUsername(entry.getUsername())) {
        userToSave.setUsername(entry.getUsername());
      } else {
        throw new UserUsernameAlreadyUsedException("Method register: Username is already used.");
      }
    }
    else
      throw new UserInvalidUsernameException("Method register: Username field can't be null.");

    if(entry.getPassword() != null && !entry.getPassword().equals(""))
      userToSave.setPassword(passwordEncoder.encode(entry.getPassword()));
    else
      throw new UserInvalidPasswordException("Method register: Password field can't be null.");

    userToSave.setRoles(new ArrayList<>());

    addRoleToUser(userToSave, DEFAULT_ROLE);

    return userRepository.save(userToSave);
  }

  /**
   * Updates the User entry with the given id based on the received object.
   * Throws an exception if no entry with that id was found.
   * @param id - the id of the entry to update
   * @param entry the User entry.
   * @throws UserNotFoundException
   * @throws UserInvalidNameException
   * @throws UserUsernameNotAllowedException
   * @throws UserInvalidPasswordException
   * @throws UserInvalidRolesException
   * @return the User object saved in the DB
   */
  @Override
  public User update(String id, User entry) throws UserNotFoundException,
    UserInvalidNameException,
    UserUsernameNotAllowedException,
    UserInvalidPasswordException,
    UserInvalidRolesException{
    User userToUpdate = userRepository.findById(id).orElseThrow(
      () -> new UserNotFoundException("Method update: User not found")
    );

    if(entry.getName() != null && !entry.getName().equals("")) {
      userToUpdate.setName(entry.getName());
    } else {
      throw new UserInvalidNameException("Method update: Name Field Can't Be Null");
    }

    log.info(String.valueOf(userToUpdate.getUsername().equals(entry.getUsername())));
    if(entry.getUsername() != null && !entry.getUsername().equals("") && !userToUpdate.getUsername().equals(entry.getUsername()))
      throw new UserUsernameNotAllowedException("Method update: Can not change username.");

    if(entry.getPassword() != null && !entry.getPassword().equals(""))
      userToUpdate.setPassword(passwordEncoder.encode(entry.getPassword()));
    else
      throw new UserInvalidPasswordException("Method update: Password field can't be null.");

    if(entry.getRoles() != null && !entry.getRoles().isEmpty())
      userToUpdate.setRoles(entry.getRoles());
    else
      throw new UserInvalidRolesException("Method update: Roles field can't be null.");

    return userRepository.save(userToUpdate);
  }

  /**
   * Deletes the User entry with the given id or throws an exception if no
   * entry with that id can be found
   * @param id - id of the User entry
   * @throws UserNotFoundException
   */
  @Override
  public void delete(String id) throws UserNotFoundException {
    if(userRepository.findById(id).isPresent())
      userRepository.deleteById(id);
    else
      throw new UserNotFoundException("Method delete: User Not Found");
  }

  /**
   * Retrieves one Role entry with the given roleName from the DB
   * Add the Role to User entry given by user
   * or throws an error if no entry with that roleName is found.
   * @param user - User entry
   * @param roleName - name of the Role entry
   * @throws RoleNotFoundException
   */
  public void addRoleToUser(User user, String roleName) throws RoleNotFoundException {
    Role role = roleRepository.findByName(roleName).orElseThrow(
      () -> new RoleNotFoundException("Method addRoleToUser: Role not found")
    );
    log.info("Adding role {} to user {}", role.getName(), user.getName());
    user.getRoles().add(role);
    userRepository.save(user);
  }
}
