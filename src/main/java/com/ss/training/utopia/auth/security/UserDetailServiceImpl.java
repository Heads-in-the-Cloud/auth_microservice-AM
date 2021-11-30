package com.ss.training.utopia.auth.security;

import com.ss.training.utopia.auth.dao.UserDao;
import com.ss.training.utopia.auth.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

    private final UserDao dao;

    public UserDetailServiceImpl(UserDao dao) {
        this.dao = dao;
    }

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        // convert from User to UserDetails
        Optional<User> user= dao.findByUsername(userName);
        user.orElseThrow(() -> new UsernameNotFoundException("not found" + userName));
        return new UserDetailsImpl(user.get());
    }

}
