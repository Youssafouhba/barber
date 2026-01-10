package com.halaq.backend.core.security.entity;

import com.halaq.backend.core.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(
        name = "role_app_user_app",
        indexes = {
                @Index(name = "idx_role_user_user_app_id", columnList = "user_app_id"),
                @Index(name = "idx_role_user_role_id", columnList = "role_id")
        }
)
@SequenceGenerator(name = "role_user_seq", sequenceName = "role_user_seq", allocationSize = 50, initialValue = 1)
public class RoleUser extends BaseEntity {

    private Long id;
    private Role role;
    private User userApp;

    public RoleUser() {
        super();
    }

    public RoleUser(Role role, User user) {
        this.role = role;
        this.userApp = user;
    }

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_app_user_app_seq")
    @SequenceGenerator(name = "role_app_user_app_seq", sequenceName = "role_app_user_app_seq", allocationSize = 50, initialValue = 1)
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "fk_role_user_role"))
    public Role getRole() {
        return this.role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_app_id", foreignKey = @ForeignKey(name = "fk_role_user_user"))
    public User getUserApp() {
        return this.userApp;
    }

    public void setUserApp(User user) {
        this.userApp = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleUser roleUser = (RoleUser) o;
        return id != null && id.equals(roleUser.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}