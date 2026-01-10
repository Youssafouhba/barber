package com.halaq.backend.core.security.service.impl;

import com.halaq.backend.core.security.entity.ModelPermissionUser;
import com.halaq.backend.core.security.entity.Role;
import com.halaq.backend.core.security.entity.RoleUser;
import com.halaq.backend.core.security.entity.User;
import com.halaq.backend.core.security.repository.criteria.core.UserCriteria;
import com.halaq.backend.core.security.repository.facade.core.UserDao;
import com.halaq.backend.core.security.repository.specification.core.UserSpecification;
import com.halaq.backend.core.security.service.facade.*;
import com.halaq.backend.core.service.AbstractServiceImpl;
import com.halaq.backend.core.util.ListUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class UserServiceImpl extends AbstractServiceImpl<User, UserCriteria, UserDao> implements UserService {

    @Autowired
    private  RoleUserService roleUserService;
    @Autowired
    private  ModelPermissionService modelPermissionService;
    @Autowired
    private  ActionPermissionService actionPermissionService;
    @Autowired
    private  ModelPermissionUserService modelPermissionUserService;
    @Autowired
    private  RoleService roleService;
    @Lazy
    @Autowired
    private PasswordEncoder bCryptPasswordEncoder;

    private static final String CHARACTERS = "0123456789";


    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class, readOnly = false)
    public User create(User t) {
        return createWithEnable(t, true);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class, readOnly = false)
    public User createAndDisable(User t) {
        return createWithEnable(t, false);
    }

    @Override
    @Cacheable(value = "users", key = "#email", unless = "#result == null")
    public User findByEmail(String email) {
        return dao.findByEmail(email);
    }

    @Override
    @Cacheable(value = "users", key = "#username", unless = "#result == null")
    public User findByUsername(String username) {
        if (username == null) return null;
        return dao.findByUsernameWithRoles(username);
    }

    @Override
    @Cacheable(value = "users", key = "#phone", unless = "#result == null")
    public User findByPhone(String phone) {
        return dao.findByPhone(phone);
    }

    @Override
    public User findByIdWithAllAssociations(Long id) {
        return dao.findByIdWithAllAssociations(id);
    }

    @Override
    public User findByPhoneWithRoles(String login) {
        return dao.findByPhoneWithRoles(login);
    }

    @Override
    public User findByEmailWithRoles(String login) {
        return dao.findByEmailWithRoles(login);
    }

    // UserServiceImpl.java
    @Override
    @Transactional
    public User updateStatusAndVerification(User user) {
        // 1. Récupérer l'utilisateur persistant pour s'assurer que le mot de passe
        // est chargé dans la session si besoin (si l'objet 'user' n'était pas attaché).
        User persistentUser = dao.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Transférer SEULEMENT les champs modifiés
        persistentUser.setEnabled(user.isEnabled());
        persistentUser.setLinkValidationCode(user.getLinkValidationCode());
        persistentUser.setExpirationLinkDate(user.getExpirationLinkDate());
        persistentUser.setStatus(user.getStatus());

        // 3. Sauvegarder (ne touchera pas le mot de passe car il n'est pas assigné/modifié ici)
        return dao.save(persistentUser);
    }

    public User createWithEnable(User user, boolean enable) {
        user.setPassword(encodePassword(user.getPassword(), user.getUsername()));
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEnabled(enable);
        user.setPasswordChanged(false);
        user.setCreatedAt(LocalDateTime.now());

        List<RoleUser> transientRoleUsers = new ArrayList<>(
                user.getRoleUsers() != null ? user.getRoleUsers() : Collections.emptyList()
        );
        user.setRoleUsers(null);
        User savedUser = super.create(user);
        if (!transientRoleUsers.isEmpty()) {
            List<String> roleNames = transientRoleUsers.stream()
                    .map(ru -> ru.getRole().getAuthority())
                    .collect(Collectors.toList());

            List<Role> persistentRoles = roleService.findByAuthoritiesIn(roleNames);
            Map<String, Role> roleMap = persistentRoles.stream()
                    .collect(Collectors.toMap(Role::getAuthority, Function.identity()));

            List<RoleUser> rolesToSave = new ArrayList<>();

            for (RoleUser transientRoleUser : transientRoleUsers) {
                Role persistentRole = roleMap.get(transientRoleUser.getRole().getAuthority());
                if (persistentRole != null) {
                    RoleUser newRoleUser = new RoleUser();
                    newRoleUser.setRole(persistentRole);
                    newRoleUser.setUserApp(savedUser); // Lier à l'entité managée
                    rolesToSave.add(newRoleUser);
                }
            }
            // 3. EFFECTUER LA SAUVEGARDE EN BATCH DES ROLEUSER EN UNE SEULE FOIS
            if (!rolesToSave.isEmpty()) {
                // Utiliser le DAO/Service du RoleUser pour sauver en lot (saveAll)
                // C'est ici que l'appel DB doit être unique pour tous les rôles.
                // Il est **CRUCIAL** d'utiliser une méthode qui appelle `JpaRepository.saveAll(iterable)`
                List<RoleUser> persistentRoleUsers = roleUserService.saveAll(rolesToSave);
                savedUser.setRoleUsers(persistentRoleUsers);
            }
        }
        return savedUser;
    }

    /**
     * Méthode d'aide pour garder la logique du mot de passe propre.
     */
    private String encodePassword(String password, String username) {
        if (password == null || password.trim().isEmpty()) {
            return bCryptPasswordEncoder.encode(username);
        }
        return bCryptPasswordEncoder.encode(password);
    }

    /**
     * Méthode d'aide pour gérer l'initialisation et la sauvegarde des permissions.
     */
    private void initializeAndSavePermissions(User savedUser) {
        List<ModelPermissionUser> permissions = modelPermissionUserService.initModelPermissionUser();
        if (permissions != null) {
            for (ModelPermissionUser permission : permissions) {
                permission.setUserApp(savedUser);
                modelPermissionUserService.create(permission);
            }
            savedUser.setModelPermissionUsers(permissions);
        }
    }

    /**
     * Méthode d'aide pour gérer l'assignation et la sauvegarde des rôles.
     */
    private void assignAndSaveRoles(List<RoleUser> roles, User savedUser) {
        if (roles != null && !roles.isEmpty()) {
            for (RoleUser roleUser : roles) {
                // S'assurer que l'entité Role est gérée par JPA en la récupérant.
                Role persistentRole = roleService.findByAuthority("ROLE_" + roleUser.getRole().getAuthority());
                if (persistentRole != null) {
                    roleUser.setRole(persistentRole);
                    roleUser.setUserApp(savedUser);
                    roleUserService.create(roleUser);
                }
            }
        }
    }


    @Transactional
    public void deleteAssociatedLists(Long id) {
        modelPermissionUserService.deleteByUserId(id);
        roleUserService.deleteByUserId(id);
    }


    public void updateWithAssociatedLists(User user) {
        if (user != null && user.getId() != null) {
            List<List<ModelPermissionUser>> resultModelPermissionUsers = modelPermissionUserService.getToBeSavedAndToBeDeleted(modelPermissionUserService.findByUserId(user.getId()), user.getModelPermissionUsers());
            modelPermissionUserService.delete(resultModelPermissionUsers.get(1));
            ListUtil.emptyIfNull(resultModelPermissionUsers.get(0)).forEach(e -> e.setUserApp(user));
            modelPermissionUserService.update(resultModelPermissionUsers.get(0), true);
            List<List<RoleUser>> resultRoleUsers = roleUserService.getToBeSavedAndToBeDeleted(roleUserService.findByUserId(user.getId()), user.getRoleUsers());
            roleUserService.delete(resultRoleUsers.get(1));
            ListUtil.emptyIfNull(resultRoleUsers.get(0)).forEach(e -> e.setUserApp(user));
            roleUserService.update(resultRoleUsers.get(0), true);
        }
    }


    public User findByReferenceEntity(User t) {
        return dao.findByEmail(t.getEmail());
    }

    public String generateCode(int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(index);
            sb.append(randomChar);
        }

        return sb.toString();
    }

    @Override
    public String cryptPassword(String value) {
        return value == null ? null : bCryptPasswordEncoder.encode(value);
    }

    @Override
    public boolean changePassword(String username, String newPassword) {
        User user = dao.findByUsername(username);
        if (user != null) {
            user.setPassword(cryptPassword(newPassword));
            user.setPasswordChanged(true);
            dao.save(user);
            return true;
        }
        return false;
    }

    @Override
    public User findByUsernameWithRoles(String username) {
        if (username == null)
            return null;
        return dao.findByUsernameWithRoles(username);
    }

    @Override
    public User findByUsernameOrEmailOrPhone(String login) {
        return dao.findByUsernameOrEmailOrPhone(login);
    }
    @Override
    @Transactional
    public int deleteByUsername(String username) {
        return dao.deleteByUsername(username);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Chercher par username (le plus rapide/préféré)
        User user = findByUsernameWithRoles(username);

        // 2. Si non trouvé, chercher par email
        if (user == null) {
            user = findByEmail(username); // Assurez-vous que cette méthode existe
        }

        // 3. Si toujours non trouvé, chercher par téléphone (moins commun pour le login)
        if (user == null) {
            user = findByPhone(username); // Assurez-vous que cette méthode existe
        }

        return user;
    }

    public void configure() {
        super.configure(User.class, UserSpecification.class);
    }

    public UserServiceImpl(UserDao dao) {
        super(dao);
    }

    public List<Role> findRolesByAuthorities(List<String> authorities) {
        if (authorities == null || authorities.isEmpty()) {
            return Collections.emptyList();
        }
        return roleService.findByAuthoritiesIn(authorities);
    }


}
