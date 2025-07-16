package taivs.project.security.service;

import taivs.project.entity.Role;
import taivs.project.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class UserDetailsImpl implements UserDetails {

    private final User user;

    public UserDetailsImpl(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        if (user.getUserRoles() != null) {
            user.getUserRoles().forEach(userRole -> {
                Role role = userRole.getRole();
                if (role != null) {

                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

                    if (role.getRolePermissions() != null) {
                        role.getRolePermissions().forEach(rolePermission -> {
                            if (rolePermission.getPermission() != null) {
                                authorities.add(new SimpleGrantedAuthority(rolePermission.getPermission().getName()));
                            }
                        });
                    }
                }
            });
        }

        return authorities;
    }
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getTel();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() == 1;
    }

}
