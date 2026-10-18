package org.suraksha.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.suraksha.entity.Role;
import org.suraksha.entity.User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * DataJpaTest repository slice test for UserRepository and RoleRepository.
 * Uses real JPA test context against embedded MySQL-compatible H2 database.
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryJpaTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("Persistence: Saves user, associates roles, and retrieves by normalized email")
    void testSaveAndFindByEmail() {
        Role role = roleRepository.save(new Role("ROLE_STUDENT", "Student role"));

        User user = new User("Vikram Roy", "vikram@suraksha.org", "$2a$10$hashedPasswordSample");
        user.addRole(role);
        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isNotNull();

        Optional<User> fetched = userRepository.findByEmail("vikram@suraksha.org");
        assertThat(fetched).isPresent();
        assertThat(fetched.get().getFullName()).isEqualTo("Vikram Roy");
        assertThat(fetched.get().getRoles()).hasSize(1);
        assertThat(fetched.get().getRoles().iterator().next().getName()).isEqualTo("ROLE_STUDENT");
    }

    @Test
    @DisplayName("Query: existsByEmail returns true for existing email, false for non-existent")
    void testExistsByEmail() {
        User user = new User("Sunita Rao", "sunita@suraksha.org", "$2a$10$hashedPasswordSample");
        userRepository.save(user);

        assertThat(userRepository.existsByEmail("sunita@suraksha.org")).isTrue();
        assertThat(userRepository.existsByEmail("nonexistent@suraksha.org")).isFalse();
    }

    @Test
    @DisplayName("Constraint: Duplicate email throws DataIntegrityViolationException on flush")
    void testDuplicateEmailConstraintViolation() {
        User user1 = new User("User One", "duplicate@suraksha.org", "hash1");
        userRepository.save(user1);
        entityManager.flush();

        User user2 = new User("User Two", "duplicate@suraksha.org", "hash2");
        assertThatThrownBy(() -> {
            userRepository.save(user2);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
