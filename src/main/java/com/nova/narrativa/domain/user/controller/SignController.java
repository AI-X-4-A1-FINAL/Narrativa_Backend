package com.nova.narrativa.domain.user.controller;

import com.nova.narrativa.domain.user.dto.SignUp;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RequestMapping("/api/users")
@RestController
public class SignController {

    @PostMapping("/sign-up")
    public ModelAndView signUp(@RequestBody SignUp signUp) {
        System.out.println("signUp = " + signUp);


    }
}
