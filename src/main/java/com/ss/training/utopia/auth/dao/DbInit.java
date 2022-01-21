package com.ss.training.utopia.auth.dao;

import com.ss.training.utopia.auth.entity.Role;
import com.ss.training.utopia.auth.entity.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Always ensure that there is both an 'admin' user and an 'ADMIN' role present in the database
 */
@Service
public class DbInit implements CommandLineRunner {

    // repos
    private final UserDao dao;
    private final RoleDao rdao;
    private final PasswordEncoder passwordEncoder;
    public DbInit(UserDao dao, RoleDao rdao, PasswordEncoder passwordEncoder) {
        this.dao = dao;
        this.rdao = rdao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        // ensure roles always exist
        Optional<Role> adminOpt = rdao.findByRoleName("ADMIN");
        Optional<Role> agentOpt = rdao.findByRoleName("AGENT");
        Optional<Role> userOpt = rdao.findByRoleName("USER");
        Role admin;
        // admin
        if (adminOpt.isEmpty())
            admin = rdao.save(Role.builder().id(3).roleName("ADMIN").build());
        else
            admin = adminOpt.get();
        // agent
        if (agentOpt.isEmpty())
            rdao.save(Role.builder().id(2).roleName("AGENT").build());
        // user
        if (userOpt.isEmpty())
            rdao.save(Role.builder().id(1).roleName("USER").build());

        // ensure admin user exists
        if (!dao.existsByEmail("admin@foo.bar")) {
            User adminCreate = User.builder()
                .email("admin@foo.bar")
                .familyName("bar")
                .givenName("foo")
                .username("admin")
                .password(passwordEncoder.encode("pass"))
                .phone("+1 (800) 888-8888")
                .role(admin)
                .build();
            dao.save(adminCreate);
        }
    }
}
